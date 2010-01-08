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
package dk.statsbiblioteket.doms.surveillance.rest.log4jappender;

import org.apache.log4j.spi.LoggingEvent;

import dk.statsbiblioteket.doms.surveillance.status.Status;
import dk.statsbiblioteket.doms.surveillance.status.StatusMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NavigableMap;
import java.util.TreeMap;

/** A log survey that caches log messages for later inspection. */
public class CachingLogSurvey implements LogSurvey {
    private static final int MAX_NUMBER_OF_MESSAGES_KEPT_BY_LOG = 1000;

    private NavigableMap<Long, Collection<StatusMessage>> logStatusMessages
            = new TreeMap<Long, Collection<StatusMessage>>();
    private String name;

    /**
     * Returns all log messages received since the given date.
     *
     * @param time Only messages strictly after the given date are returned.
     * @return A status containing list of log messages.
     */
    public synchronized Status getStatusSince(long time) {
        Collection<Collection<StatusMessage>> listCollection = logStatusMessages
                .subMap(time, false, Long.MAX_VALUE, true).values();
        Collection<StatusMessage> statusMessages
                = new ArrayList<StatusMessage>();
        for (Collection<StatusMessage> collection : listCollection) {
            statusMessages.addAll(collection);
        }
        return new Status(name, statusMessages);
    }

    /**
     * Returns all log messages received.
     *
     * @return A status containing list of log messages.
     */
    public synchronized Status getStatus() {
        return getStatusSince(0l);
    }

    /**
     * Register a message for later inspection.
     *
     * @param event The log message to register.
     */
    public synchronized void registerMessage(LoggingEvent event) {
        // Ensure the log doesn't grow too huge
        if (logStatusMessages.size() > MAX_NUMBER_OF_MESSAGES_KEPT_BY_LOG - 1) {
            long earliestTimeStamp = logStatusMessages.firstKey();
            logStatusMessages.remove(earliestTimeStamp);
        }

        // Log it
        Collection<StatusMessage> collection;
        collection = logStatusMessages.get(event.getTimeStamp());
        if (collection == null) {
            collection = new ArrayList<StatusMessage>();
            logStatusMessages.put(event.getTimeStamp(), collection);
        }
        collection.add(new LogStatusMessage(event));
    }

    /**
     * Sets the name.
     *
     * @param name The name.
     */
    public synchronized void setName(String name) {
        this.name = name;
    }
}
