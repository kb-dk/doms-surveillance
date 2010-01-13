/* $Id$
 * $Revision$
 * $Date$
 * $Author$
 *
 * The DOMS project.
 * Copyright (C) 2007-2009  The State and University Library
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package dk.statsbiblioteket.doms.surveillance.surveyor;

import com.sun.jersey.api.client.Client;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.statsbiblioteket.doms.surveillance.status.Status;
import dk.statsbiblioteket.doms.surveillance.status.StatusMessage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** A surveyor that calls specified REST URLs to get status. */
public class RestSurveyor implements Surveyor {
    /** Currently stored state, for keeping log messages until handled. */
    private Map<String, CondensedStatus> currentStatus
            = new HashMap<String, CondensedStatus>();
    /** Newest message time from last time we queried a given URL. */
    private Map<String, Long> newestStatus = new HashMap<String, Long>();
    /** List of REST URLs to query. */
    private List<String> restStatusUrls = new ArrayList<String>();
    /** Set of messages to ignore. */
    private Map<String, Set<String>> ignoredMessages
            = new HashMap<String, Set<String>>();
    /** File containing ignored strings */
    public File ignoredMessagesFile = new File("ignored.txt");
    /** Logger for this class. */
    private final Log log = LogFactory.getLog(Surveyor.class);

    /** Initialise this surveyor. */
    public RestSurveyor() {
        log.info("Starting surveyor");
    }

    /**
     * Set configuration.
     *
     * @param restStatusUrls      REST status URLs to query.
     * @param ignoredMessagesFile File to store list of ignored messages in.
     */
    public synchronized void setConfiguration(List<String> restStatusUrls,
                                              File ignoredMessagesFile) {
        log.trace("enter setConfiguration('" + restStatusUrls + "', '"
                + ignoredMessagesFile + "')");
        if (!restStatusUrls.equals(this.restStatusUrls)) {
            log.info("Setting list of surveyed REST status URLs to '"
                    + restStatusUrls + "'");
            this.restStatusUrls = restStatusUrls;
        }
        if (!this.ignoredMessagesFile.equals(ignoredMessagesFile)) {
            log.info("Setting file with list of ignored messages to '"
                    + ignoredMessagesFile + "'");
            this.ignoredMessagesFile = ignoredMessagesFile;
            readIgnoredMessagesFromFile(ignoredMessagesFile);
        }
    }

    /**
     * Mark a message as handled, thus removing it from the list of currently
     * unhandled log messages.
     *
     * @param applicationName Name of application with message.
     * @param message         The message to mark as handled.
     */
    public synchronized void markHandled(String applicationName,
                                         String message) {
        log.trace("enter markHandled('" + applicationName + "', '" + message
                + "')");
        CondensedStatus status = currentStatus.get(applicationName);
        if (status != null) {
            log.debug("Log message ('" + applicationName + "', '" + message
                    + "') marked as handled");
            status.removeLogMessage(message);
        }
    }

    /**
     * Mark a message as one that should never be shwon again.
     *
     * @param applicationName Name of application with message.
     * @param message         The message never to show again.
     */
    public synchronized void notAgain(String applicationName, String message) {
        log.debug("Log message ('" + applicationName + "', '" + message
                + "') will never be shown again");
        Set<String> ignored = ignoredMessages.get(applicationName);
        if (ignored == null) {
            ignored = new HashSet<String>();
            ignoredMessages.put(applicationName, ignored);
        }
        ignored.add(message);
        markHandled(applicationName, message);
        appendIgnoredMessageToFile(applicationName, message);
    }

    /**
     * Get a list of statuses by querying some REST urls, and merging them with
     * previously known unhandled log messages.
     *
     * @return A map of statuses from name to status.
     */
    public synchronized Map<String, CondensedStatus> getStatusMap() {
        log.trace("enter getStatusMap()");
        Map<String, CondensedStatus> result
                = new HashMap<String, CondensedStatus>();
        //Keep only non-ignored log messages
        updateResultFromOldStatus(result);
        //Query REST-URLS for more messages
        Client c = Client.create();
        for (String statusUrl : restStatusUrls) {
            //Find time of newest currently known log message from that URL
            Long newest = newestStatus.get(statusUrl);
            if (newest == null) {
                newest = 0l;
            }
            //Get status from REST
            Status restStatus = getStatusFromRest(c, statusUrl, newest);
            //Add condensed status to result if not already there
            CondensedStatus status = result.get(restStatus.getName());
            if (status == null) {
                status = new CondensedStatus(restStatus.getName());
                result.put(restStatus.getName(), status);
            }
            //Filter status by list of ignored messages
            Set<String> ignored = ignoredMessages.get(restStatus.getName());
            for (StatusMessage message : restStatus.getMessages()) {
                if (ignored == null || !ignored.contains(
                        message.getMessage())) {
                    status.addMessage(message);
                }
            }
            //Remember the newest time of messages
            for (StatusMessage message : restStatus.getMessages()) {
                if (newest < message.getTime()) {
                    newestStatus.put(statusUrl, message.getTime());
                }
            }
        }
        //Remember result
        currentStatus = result;
        log.trace("exit getStatusMap()");
        return result;
    }

    /**
     * Get status from a REST URL.
     * This method serves as fault barrier for REST calls. All exceptions are
     * caught and turned into a status message.
     *
     * @param restClient The REST client to use for REST communication
     * @param statusUrl  The URL to query for status. Any occurence of "{date}"
     *                   will be repalced with the given timestamp, to allow querying only for
     *                   messages after a given time.
     * @param timestamp  Date to insert in URL in place of "{date}". Also used as
     *                   timestamp for error messages.
     * @return The status returned from the query URL, or a status reporting the
     *         error in any other case. Never null.
     */
    private Status getStatusFromRest(Client restClient, String statusUrl,
                                     Long timestamp) {
        log.trace("enter getStatusFromRest('" + restClient + "','" + statusUrl
                + "','" + timestamp + "')");
        //Initialise URL
        String queryUrl;
        if (statusUrl.contains("{date}")) {
            queryUrl = statusUrl.replace("{date}", Long.toString(timestamp));
        } else {
            queryUrl = statusUrl;
        }

        //Query REST
        Status restStatus;
        try {
            log.debug("REST status query for URL '" + statusUrl + "'");
            restStatus = restClient.resource(queryUrl).get(Status.class);
        } catch (Exception e) {
            log.debug(
                    "Cannot get status for REST status URL '" + statusUrl + "'",
                    e);
            //On exceptions, create false status about trouble
            String name;
            if (currentStatus.get(statusUrl) != null) {
                name = currentStatus.get(statusUrl).getName();
            } else {
                name = statusUrl;
            }
            restStatus = new Status(name, Arrays.asList(new StatusMessage(
                    "Unable to communicate with service: " + e.getMessage(),
                    StatusMessage.Severity.RED, timestamp, false)));
        }
        return restStatus;
    }

    /**
     * Insert into the given map any non-ignored log messages from the currently
     * known status.
     *
     * @param result The map to update with the current non-iognored log
     *               messages. The map maps from status name to condensed status
     *               of that name.
     */
    private void updateResultFromOldStatus(
            Map<String, CondensedStatus> result) {
        log.trace("enter updateResultFromOldStatus('" + result + "')");
        if (currentStatus != null) {
            for (CondensedStatus oldStatus : currentStatus.values()) {
                CondensedStatus newStatus = new CondensedStatus(
                        oldStatus.getName());
                Set<String> ignored = ignoredMessages.get(oldStatus.getName());
                for (CondensedStatusMessage statusMessage : oldStatus
                        .getMessages()) {
                    if (statusMessage.isLogMessage()) {
                        if (ignored == null || !ignored.contains(
                                statusMessage.getMessage())) {
                            newStatus.addMessage(statusMessage);
                        }
                    }
                }
                result.put(newStatus.getName(), newStatus);
            }
        }
    }

    /**
     * Initialise the map of ignored messages from backing file.
     *
     * @param ignoredMessagesFile The file that contains the map of ignored
     *                            messages in the line-based format
     *                            applicationname;message
     */
    private void readIgnoredMessagesFromFile(File ignoredMessagesFile) {
        log.trace("readIgnoredMessagesFromFile('" + ignoredMessagesFile + "')");
        ignoredMessages.clear();
        if (ignoredMessagesFile.isFile()) {
            BufferedReader fr = null;
            String s;
            try {
                try {
                    fr = new BufferedReader(new FileReader(
                            ignoredMessagesFile));
                    s = fr.readLine();
                    while (s != null) {
                        s = fr.readLine();
                        if (!s.trim().isEmpty() && s.indexOf(';') > 0) {
                            String key = s.substring(0, s.indexOf(';'));
                            String value = s.substring(s.indexOf(';') + 1);
                            value = value.replaceAll("\\\\n", "\n");
                            Set<String> values = ignoredMessages.get(key);
                            if (values == null) {
                                values = new HashSet<String>();
                                ignoredMessages.put(key, values);
                            }
                            values.add(value);
                        }
                    }
                } finally {
                    if (fr != null) {
                        fr.close();
                    }
                }
            } catch (IOException e) {
                log.warn("Unable to read from file of ignored messages."
                        + "Ignoring rest of file.", e);
            }
        }
    }

    private void appendIgnoredMessageToFile(String applicationName,
                                            String message) {
        log.trace("appendIgnoredMessageToFile('" + applicationName + "', '"
                + message + "')");
        try {
            PrintWriter pw = null;
            try {
                pw = new PrintWriter(new BufferedWriter(new FileWriter(
                        ignoredMessagesFile, true)));
                pw.println(applicationName + ";" + message.replaceAll("\n",
                                                                      "\\\\n"));
            } finally {
                if (pw != null) {
                    pw.close();
                }
            }
        } catch (IOException e) {
            log.warn("Unable to write ignored message to file of ignored"
                    + " messages", e);
        }
    }
}
