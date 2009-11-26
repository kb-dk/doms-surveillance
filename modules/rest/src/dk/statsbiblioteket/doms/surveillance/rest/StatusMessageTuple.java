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
package dk.statsbiblioteket.doms.surveillance.rest;

import java.util.Date;

/** A status message tuple. */
public class StatusMessageTuple implements StatusMessage {
    private final String message;
    private final Severity severity;
    private final Date time;
    private final boolean logMessage;

    /**
     * Initialise the tuple.
     *
     * @param message    The message.
     * @param severity   The severity.
     * @param time       The time.
     * @param logMessage The log message.
     */
    public StatusMessageTuple(String message, Severity severity, Date time,
                              boolean logMessage) {
        this.message = message;
        this.severity = severity;
        this.time = time;
        this.logMessage = logMessage;
    }

    /**
     * Get the text for the status message. Only trivially reformatted in HTML.
     *
     * @return The message. Never null.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the severity of the message.
     *
     * @return The severity.
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * Get the time for this message.
     *
     * @return The time this message was generated. Never null.
     */
    public Date getTime() {
        return time;
    }

    /**
     * Returns whether this is a logged message.
     *
     * @return Whether this is a logged message.
     */
    public boolean isLogMessage() {
        return logMessage;
    }
}
