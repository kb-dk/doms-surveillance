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

import dk.statsbiblioteket.doms.surveillance.rest.Status;
import dk.statsbiblioteket.doms.surveillance.rest.StatusMessage;
import dk.statsbiblioteket.doms.surveillance.rest.StatusTuple;
import org.apache.log4j.spi.LoggingEvent;

import java.util.Date;
import java.util.NavigableMap;
import java.util.TreeMap;

/** A log survey that caches log messages for later inspection. */
public class CachingLogSurvey implements LogSurvey {
    private NavigableMap<Date, StatusMessage> logstatusmessages
            = new TreeMap<Date, StatusMessage>();
    private String name;

    /**
     * Returns all log messages received since the given date.
     *
     * @param time Only messages strictly after the given date are returned.
     * @return A status containing list of log messages.
     */
    public Status getMessagesSince(Date time) {
        return new StatusTuple(
                name, logstatusmessages.subMap(
                        time, false, new Date(Long.MAX_VALUE), true).values());
    }

    /**
     * Returns all log messages received.
     *
     * @return A status containing list of log messages.
     */
    public Status getMessages() {
        return getMessagesSince(new Date(0l));
    }

    /**
     * Register a message for later inspection.
     *
     * @param event The log message to register.
     */
    public void registerMessage(LoggingEvent event) {
        logstatusmessages.put(
                new Date(event.getTimeStamp()),
                new LogStatusMessageTuple(event));
        //TODO: Ensure the log doesn't grow too huge
    }
}
