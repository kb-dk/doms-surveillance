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

package dk.statsbiblioteket.doms.surveillance.log4jappender;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.spi.LoggingEvent;

import dk.statsbiblioteket.doms.domsutil.surveyable.Status;
import dk.statsbiblioteket.doms.domsutil.surveyable.StatusMessage;
import dk.statsbiblioteket.doms.webservices.ConfigCollection;
import dk.statsbiblioteket.util.qa.QAInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * A log survey that caches log messages for later inspection.
 *
 * Note that all methods in this class are synchronized. This may affect
 * performance if your logging level for what you register in the class is
 * too broad.
 */
@QAInfo(author = "kfc",
        reviewers = "jrg",
        comment = "Needs review on diff from revision 265",
        level = QAInfo.Level.NORMAL,
        state = QAInfo.State.QA_NEEDED)
public class CachingLogRegistry implements LogRegistry {
    /** At most this many log messages are kept in the registry. */
    private int maxNumberOfMessagesKeptByLog
            = DEFAULT_MAX_NUMBER_OF_MESSAGES_KEPT_BY_LOG;

    /** A sorted map from timestamp of log message to list of actual messages. */
    private NavigableMap<Long, Collection<StatusMessage>> logStatusMessages
            = new TreeMap<Long, Collection<StatusMessage>>();

    /** The name of what is being surveyed. */
    private String name = "";

    /** The logger for this class. */
    private Log log = LogFactory.getLog(getClass());

    /** Read paramters from configuration, and initialize caching log registry. */
    public CachingLogRegistry() {
        log.trace("Enter CachingLogEntry()");
        configure();
    }

    /** Read configuration. This method acts as fault barrier */
    private void configure() {
        log.trace("Enter configure()");
        try {
            String configValue = ConfigCollection.getProperties()
                    .getProperty(NUMBEROFMESSAGES_CONFIGURATION_PARAMETER);
            if (configValue != null && !configValue.equals("")) {
                int configIntValue = Integer.parseInt(configValue);
                if (configIntValue != maxNumberOfMessagesKeptByLog) {
                    maxNumberOfMessagesKeptByLog = configIntValue;
                    log.info("Setting number of messages kept by registry to "
                            + maxNumberOfMessagesKeptByLog);
                }
            }
        } catch (Exception e) {
            log.warn("Error while configuring appender."
                    + " Falling back to default values.", e);
        }
    }

    /**
     * Returns all log messages received since the given date.
     *
     * @param time Only messages strictly after the given date are returned.
     * @return A status containing list of log messages.
     */
    public synchronized Status getStatusSince(long time) {
        log.trace("Enter getStatusSince(" + time + ")");
        Collection<Collection<StatusMessage>> listCollection = logStatusMessages
                .subMap(time, false, Long.MAX_VALUE, true).values();
        Collection<StatusMessage> statusMessages
                = new ArrayList<StatusMessage>();
        for (Collection<StatusMessage> collection : listCollection) {
            statusMessages.addAll(collection);
        }
        Status status = new Status();
        status.setName(name);
        status.getMessages().addAll(statusMessages);
        return status;
    }

    /**
     * Returns all log messages received.
     *
     * @return A status containing list of log messages.
     */
    public synchronized Status getStatus() {
        log.trace("Enter getStatus()");
        return getStatusSince(0l);
    }

    /**
     * Register a message for later inspection.
     *
     * @param event The log message to register. Should never be null.
     * @throws IllegalArgumentException if event is null.
     */
    public synchronized void registerMessage(LoggingEvent event) {
        Collection<StatusMessage> collection;

        // Check parameters
        if (event == null) {
            throw new IllegalArgumentException(
                    "Parameter event must not be null");
        }

        // Ensure the log doesn't grow too huge
        if (logStatusMessages.size() > maxNumberOfMessagesKeptByLog - 1) {
            long earliestTimeStamp = logStatusMessages.firstKey();
            logStatusMessages.remove(earliestTimeStamp);
        }

        // Register it
        collection = logStatusMessages.get(event.getTimeStamp());
        if (collection == null) {
            collection = new ArrayList<StatusMessage>();
            logStatusMessages.put(event.getTimeStamp(), collection);
        }
        collection.add(new LogStatusMessage(event));
    }

    /**
     * Sets the name of what is being surveyed.
     *
     * @param name The name. Should never be null.
     */
    public synchronized void setName(String name) {
        log.trace("Enter setName('" + name + "')");
        if (name == null) {
            throw new IllegalArgumentException(
                    "Parameter name must not be null");
        }
        this.name = name;
    }
}
