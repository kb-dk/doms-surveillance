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

import dk.statsbiblioteket.doms.surveillance.status.StatusMessage;
import dk.statsbiblioteket.util.qa.QAInfo;

/** A status message for a surveyed application. */
@QAInfo(author = "kfc",
        reviewers = "jrg",
        level = QAInfo.Level.NORMAL,
        state = QAInfo.State.QA_OK)
public class CondensedStatusMessage {
    /** The textual message. */
    private String message;

    /** The severity of the message. */
    private StatusMessage.Severity severity;

    /** The first time this message occured. */
    private long firstTime;

    /** The last time this message occured. */
    private long lastTime;

    /** Whether this is a log message or a realtime status. */
    private boolean logMessage;

    /** The number of times this message was produced. */
    private int number;

    /**
     * Initialise this condensed status message from a single status message.
     * This will set the message, severity and logMessage values to the same
     * as the status message; firstdate and lastdate to the date of the status
     * message and number to 1.
     *
     * @param statusMessage The status message to initialise with.
     */
    public CondensedStatusMessage(StatusMessage statusMessage) {
        this.message = statusMessage.getMessage();
        this.severity = statusMessage.getSeverity();
        this.firstTime = statusMessage.getTime();
        this.lastTime = statusMessage.getTime();
        this.number = 1;
        this.logMessage = statusMessage.isLogMessage();
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
     * Get the severity of the message. If this is condensed from several
     * messages, this is the greatest (most severe) severity.
     *
     * @return The severity.
     */
    public StatusMessage.Severity getSeverity() {
        return severity;
    }

    /**
     * Get the earliest time for this message.
     *
     * @return The earliest time this message was generated. Never null.
     */
    public long getFirstTime() {
        return firstTime;
    }

    /**
     * Get the latest time for this message.
     *
     * @return The latest time this message was generated. Never null.
     */
    public long getLastTime() {
        return lastTime;
    }

    /**
     * The number of times this message was generated.
     *
     * @return The number of times this message was generated.
     */
    public int getNumber() {
        return number;
    }

    /**
     * Returns whether this is a logged message.
     *
     * @return Whether this is a logged message.
     */
    public boolean isLogMessage() {
        return logMessage;
    }

    /**
     * Update the condensed status message with information from a new status
     * message. The new status message MUST have the same textual message as
     * the current textual message. The following will be updated:
     * <ul>
     * <li>The firstdate and lastdate are updated from the given message
     * <li>The severity is updated to the greatest severity
     * <li>Log message is updated to be only if both are log messages
     * <li>If this is a log message, the number is increased with one.
     * </ul>
     *
     * @param statusMessage The message to update with.
     * @throws IllegalArgumentException if statusMessage is null or the
     *                                  message is different.
     */
    public void update(StatusMessage statusMessage) {
        if (message == null || !message.equals(statusMessage.getMessage())) {
            throw new IllegalArgumentException(
                    "Can only update a condensed status message, with a status "
                            + "message containing the same textual message. "
                            + "Condensed status message: '" + message + "', "
                            + "New status message: '"
                            + statusMessage.getMessage() + "'");
        }
        if (firstTime > statusMessage.getTime()) {
            firstTime = statusMessage.getTime();
        }
        if (lastTime < statusMessage.getTime()) {
            lastTime = statusMessage.getTime();
        }
        if (severity.ordinal() < statusMessage.getSeverity().ordinal()) {
            severity = statusMessage.getSeverity();
        }
        logMessage &= statusMessage.isLogMessage();
        if (logMessage) {
            ++number;
        } else {
            number = 1;
        }
    }

    /**
     * Update the condensed status message with information from another
     * condensed status message. The new status message MUST have the same
     * textual message as the current textual message. The following will be
     * updated:
     * <ul>
     * <li>The firstdate and lastdate are updated from the given message
     * <li>The severity is updated to the greatest severity
     * <li>Log message is updated to be only if both are log messages
     * <li>If this is a log message, the number is the sum of numbers.
     * </ul>
     *
     * @param statusMessage The message to update with.
     * @throws IllegalArgumentException if statusMessage is null or the
     *                                  message is different.
     */
    public void update(CondensedStatusMessage statusMessage) {
        if (message == null || !message.equals(statusMessage.getMessage())) {
            throw new IllegalArgumentException(
                    "Can only update a condensed status message, with a status "
                            + "message containing the same textual message. "
                            + "Condensed status message: '" + message + "', "
                            + "New status message: '"
                            + statusMessage.getMessage() + "'");
        }
        if (firstTime > statusMessage.getFirstTime()) {
            firstTime = statusMessage.getFirstTime();
        }
        if (lastTime < statusMessage.getLastTime()) {
            lastTime = statusMessage.getLastTime();
        }
        if (severity.ordinal() < statusMessage.getSeverity().ordinal()) {
            severity = statusMessage.getSeverity();
        }
        logMessage &= statusMessage.isLogMessage();
        if (logMessage) {
            number += statusMessage.getNumber();
        } else {
            number = 1;
        }
    }
}
