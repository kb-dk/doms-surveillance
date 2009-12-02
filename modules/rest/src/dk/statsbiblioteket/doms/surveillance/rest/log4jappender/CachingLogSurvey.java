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

import dk.statsbiblioteket.doms.surveillance.status.Status;
import dk.statsbiblioteket.doms.surveillance.status.StatusMessage;
import org.apache.log4j.spi.LoggingEvent;

import java.util.NavigableMap;
import java.util.TreeMap;

/** A log survey that caches log messages for later inspection. */
public class CachingLogSurvey implements LogSurvey {
    private NavigableMap<Long, StatusMessage> logstatusmessages
            = new TreeMap<Long, StatusMessage>();
    private String name;

    /**
     * Returns all log messages received since the given date.
     *
     * @param time Only messages strictly after the given date are returned.
     * @return A status containing list of log messages.
     */
    public Status getStatusSince(long time) {
        return new Status(
                name, logstatusmessages.subMap(
                        time, false, Long.MAX_VALUE, true).values());
    }

    /**
     * Returns all log messages received.
     *
     * @return A status containing list of log messages.
     */
    public Status getStatus() {
        return getStatusSince(0l);
    }

    /**
     * Register a message for later inspection.
     *
     * @param event The log message to register.
     */
    public void registerMessage(LoggingEvent event) {
        logstatusmessages.put(
                event.getTimeStamp(), new LogStatusMessage(event));
        //TODO: Ensure the log doesn't grow too huge
    }

    /**
     * Sets the name.
     *
     * @param name The name.
     */
    public void setName(String name) {
        this.name = name;
    }
}
