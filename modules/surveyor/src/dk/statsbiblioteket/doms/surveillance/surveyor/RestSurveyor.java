/*
 * $Id$
 * $Revision$
 * $Date$
 * $Author$
 *
 * The DOMS project.
 * Copyright (C) 2007-2010  The State and University Library
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import dk.statsbiblioteket.doms.domsutil.surveyable.Severity;
import dk.statsbiblioteket.doms.domsutil.surveyable.Status;
import dk.statsbiblioteket.doms.domsutil.surveyable.StatusMessage;
import dk.statsbiblioteket.doms.webservices.ConfigCollection;
import dk.statsbiblioteket.util.qa.QAInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A surveyor that calls specified REST URLs to get status.
 *
 * This is configurable with parameters:
 * <code>dk.statsbiblioteket.doms.surveillance.surveyor.urls</code>
 * defining list of REST status URLs to monitor (default is empty), and
 * <code>dk.statsbiblioteket.doms.surveillance.surveyor.ignoredMessagesFile</code>
 * defining the file used to persist list of ignored messages (default is
 * "ignored.txt"). 
 *
 * This class is synchronized on all public methods.
 */
@QAInfo(author = "kfc",
        reviewers = "jrg",
        comment = "Needs review on diff from revision 265",
        level = QAInfo.Level.NORMAL,
        state = QAInfo.State.QA_NEEDED)
public class RestSurveyor implements Surveyor {
    /** The package prefix for parameter names. */
    private static final String CONFIGURATION_PACKAGE_NAME
            = "dk.statsbiblioteket.doms.surveillance.surveyor";

    /** Parameter for URLS for surveyor. */
    public static final String URLS_CONFIGURATION_PARAMETER
            = CONFIGURATION_PACKAGE_NAME + ".urls";

    /** Parameter for file with ignored messages for surveyor. */
    public static final String IGNOREFILE_CONFIGURATION_PARAMETER
            = CONFIGURATION_PACKAGE_NAME + ".ignoredMessagesFile";

    /**
     * Currently stored state, for keeping log messages until handled. Maps
     * from application name to status for that application.
     */
    private Map<String, CondensedStatus> currentStatus
            = new HashMap<String, CondensedStatus>();

    /** Newest message time from last time we queried a given URL. */
    private Map<String, Long> newestStatusTime = new HashMap<String, Long>();

    /** List of REST URLs to query. */
    private List<String> restStatusUrls = new ArrayList<String>();

    /**
     * Map of messages to ignore. Map from application name to set of ignored
     * messages.
     */
    private Map<String, Set<String>> ignoredMessages
            = new HashMap<String, Set<String>>();

    /** File containing ignored strings */
    private File ignoredMessagesFile = new File(DEFAULT_IGNORED_MESSAGES_PATH);

    /** Logger for this class. */
    private final Log log = LogFactory.getLog(Surveyor.class);

    /** Default configuration for ignored messages file. */
    private static final String DEFAULT_IGNORED_MESSAGES_PATH = "ignored.txt";

    /** Initialise this surveyor. */
    public RestSurveyor() {
        log.info("Starting surveyor");
        readConfiguration();
    }

    /**
     * Set configuration.
     *
     * This method will read the configuration values, and do initialization
     * based on this.
     *
     * @see #URLS_CONFIGURATION_PARAMETER
     * @see #IGNOREFILE_CONFIGURATION_PARAMETER
     */
    private synchronized void readConfiguration() {
        log.trace("Enter readConfiguration()");

        //Read configuration
        String restUrlParameter
                = ConfigCollection.getProperties().getProperty(
                URLS_CONFIGURATION_PARAMETER);
        String ignoredMessagesPath = ConfigCollection.getProperties()
                .getProperty(IGNOREFILE_CONFIGURATION_PARAMETER);
        List<String> restStatusUrls;
        File ignoredMessagesFile;

        //Initialize status urls
        if (restUrlParameter == null || restUrlParameter.equals("")) {
            restStatusUrls = Collections.emptyList();
        } else {
            restStatusUrls = Arrays.asList(restUrlParameter.split(";"));
        }
        if (!restStatusUrls.equals(this.restStatusUrls)) {
            log.info("Setting list of surveyed REST status URLs to '"
                    + restStatusUrls + "'");
            this.restStatusUrls = restStatusUrls;
        }

        //Initialize file with list of ignored messages.
        if (ignoredMessagesPath == null || ignoredMessagesPath.equals("")) {
            ignoredMessagesPath = DEFAULT_IGNORED_MESSAGES_PATH;
        }
        ignoredMessagesFile = new File(ignoredMessagesPath);
        if (!ignoredMessagesFile.getAbsoluteFile().getParentFile().isDirectory() ||
                (ignoredMessagesFile.exists() && !ignoredMessagesFile.isFile())) {
            log.warn("Configuration for file of ignored messages '"
                    + ignoredMessagesPath
                    + "' does not denote a valid file."
                    + " Falling back to default.");
            ignoredMessagesPath = DEFAULT_IGNORED_MESSAGES_PATH;
            ignoredMessagesFile = new File(ignoredMessagesPath);
        }
        if (!this.ignoredMessagesFile.equals(ignoredMessagesFile)) {
            log.info("Setting file with list of ignored messages to '"
                    + ignoredMessagesFile + "'");
            this.ignoredMessagesFile = ignoredMessagesFile;
            readIgnoredMessagesFromFile();
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
        log.trace("Enter markHandled('" + applicationName + "', '" + message
                + "')");
        CondensedStatus status = currentStatus.get(applicationName);
        if (status != null) {
            log.debug("Log message ('" + applicationName + "', '" + message
                    + "') marked as handled");
            status.removeLogMessage(message);
        }
    }

    /**
     * Mark a message as one that should never be shown again.
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
     * @return A map of statuses from application name to status.
     */
    public synchronized Map<String, CondensedStatus> getStatusMap() {
        log.trace("Enter getStatusMap()");
        Map<String, CondensedStatus> result
                = new HashMap<String, CondensedStatus>();
        Client c;
        //Keep only non-ignored log messages
        updateResultFromOldStatus(result);
        //Query REST-URLS for more messages
        c = Client.create();
        for (String statusUrl : restStatusUrls) {
            //Find time of newest currently known log message from that URL
            Long newest = newestStatusTime.get(statusUrl);
            if (newest == null) {
                newest = 0L;
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
                if (ignored == null
                        || !ignored.contains(message.getMessage())) {
                    status.addMessage(message);
                }
            }
            //Remember the newest time of messages
            for (StatusMessage message : restStatus.getMessages()) {
                if (newest < message.getTime()) {
                    newestStatusTime.put(statusUrl, message.getTime());
                }
            }
        }
        //Remember result
        currentStatus = result;
        log.trace("Exit getStatusMap()");
        return result;
    }

    /**
     * Get status from a REST URL.
     * This method serves as fault barrier for REST calls. All exceptions are
     * caught and turned into a status message.
     *
     * @param restClient The REST client to use for REST communication
     * @param statusUrl  The URL to query for status. Any occurence of "{date}"
     *                   will be replaced with the given timestamp, to allow
     *                   querying only for messages after a given time.
     * @param timestamp  Date to insert in URL in place of "{date}". Also used
     *                   as timestamp for error messages.
     * @return The status returned from the query URL, or a status reporting the
     *         error in any other case. Never null.
     */
    private Status getStatusFromRest(Client restClient, String statusUrl,
                                     Long timestamp) {
        log.trace("Enter getStatusFromRest('" + restClient + "','" + statusUrl
                + "','" + timestamp + "')");
        //Initialise URL
        String queryUrl;
        Status restStatus;

        if (statusUrl.contains("{date}")) {
            queryUrl = statusUrl.replace("{date}", Long.toString(timestamp));
        } else {
            queryUrl = statusUrl;
        }

        //Query REST
        try {
            log.debug("REST status query for URL '" + queryUrl + "'");
            restStatus = restClient.resource(queryUrl).get(Status.class);
        } catch (Exception e) {
            Status status = new Status();
            StatusMessage statusMessage = new StatusMessage();

            log.debug(
                    "Cannot get status for REST status URL '" + queryUrl + "'",
                    e);
            //On exceptions, create a status with information about trouble
            if (currentStatus.get(statusUrl) != null) {
                status.setName(currentStatus.get(statusUrl).getName());
            } else {
                status.setName(statusUrl);
            }

            statusMessage.setMessage("Unable to communicate with service: "
                    + e.getMessage());
            statusMessage.setSeverity(Severity.RED);
            statusMessage.setTime(timestamp);
            statusMessage.setLogMessage(false);

            status.getMessages().addAll(Arrays.asList(statusMessage));

            restStatus = status;
        }
        return restStatus;
    }

    /**
     * Insert into the given map any non-ignored log messages from the currently
     * known status.
     *
     * @param result The map to update with the current non-ignored log
     *               messages. The map maps from status name to condensed status
     *               of that name.
     */
    private void updateResultFromOldStatus(
            Map<String, CondensedStatus> result) {
        log.trace("Enter updateResultFromOldStatus('" + result + "')");
        if (currentStatus == null) {
            return;
        }
        for (CondensedStatus oldStatus : currentStatus.values()) {
            CondensedStatus newStatus = new CondensedStatus(
                    oldStatus.getName());
            Set<String> ignored = ignoredMessages.get(oldStatus.getName());
            for (CondensedStatusMessage statusMessage : oldStatus
                    .getMessages()) {
                if (statusMessage.isLogMessage()) {
                    if (ignored == null || !ignored
                            .contains(statusMessage.getMessage())) {
                        newStatus.addMessage(statusMessage);
                    }
                }
            }
            result.put(newStatus.getName(), newStatus);
        }
    }

    /**
     * Initialise the map of ignored messages from backing file. The file
     * contains the map of ignored messages in the line-based format
     * <code>applicationname;message</code>
     * Newlines are replaced with \n.
     */
    private void readIgnoredMessagesFromFile() {
        log.trace("ReadIgnoredMessagesFromFile()");
        ignoredMessages.clear();
        if (!ignoredMessagesFile.isFile()) {
            return;
        }
        BufferedReader fr = null;
        String s;
        try {
            try {
                fr = new BufferedReader(
                        new FileReader(ignoredMessagesFile));
                s = fr.readLine();
                while (s != null) {
                    s = fr.readLine();
                    if (!s.trim().isEmpty() && s.indexOf(';') > 0) {
                        String key = s.substring(0, s.indexOf(';'));
                        String value = s.substring(s.indexOf(';') + 1);
                        Set<String> values = ignoredMessages.get(key);
                        value = value.replaceAll("\\\\n", "\n");
                        if (values == null) {
                            values = new HashSet<String>();
                            ignoredMessages.put(key, values);
                        }
                        values.add(value);
                    } else {
                        log.warn("Read malformed line '" + s + "' from file '"
                                + ignoredMessagesFile + "'");
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

    /**
     * Append an ignored message to backing file. The format is
     * <code>applicationname;message</code>
     * Newlines are replaced with \n.
     * @param applicationName Application name.
     * @param message Message.
     */
    private void appendIgnoredMessageToFile(String applicationName,
                                            String message) {
        log.trace(
                "Enter AppendIgnoredMessageToFile('" + applicationName + "', '"
                        + message + "')");
        try {
            PrintWriter pw = null;
            try {
                //Open the file for appending
                pw = new PrintWriter(new BufferedWriter(
                        new FileWriter(ignoredMessagesFile, true)));
                pw.println(applicationName + ";" + message
                        .replaceAll("\n", "\\\\n"));
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
