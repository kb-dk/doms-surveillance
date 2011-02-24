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

import dk.statsbiblioteket.doms.domsutil.surveyable.StatusMessage;
import dk.statsbiblioteket.util.qa.QAInfo;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/** A data structure for a surveyed application. This data structure contains
 * messages in a condensed form, where each textually identical message is
 * treated as one entry.
 */
@QAInfo(author = "kfc",
        reviewers = "jrg",
        level = QAInfo.Level.NORMAL,
        state = QAInfo.State.QA_OK)
public class CondensedStatus {
    /** Name of what is being surveyed. */
    private String name;

    /** Map of messages, mapping from message to more info about message. */
    private Map<String, CondensedStatusMessage> messages;

    /**
     * Initialise the status.
     *
     * @param name The name of what is being surveyed.
     */
    public CondensedStatus(String name) {
        this.name = name;
        this.messages = new LinkedHashMap<String, CondensedStatusMessage>();
    }

    /**
     * Get a list of status messages.
     *
     * @return A list of status messages.
     */
    public Collection<CondensedStatusMessage> getMessages() {
        return messages.values();
    }

    /**
     * Get name of what is being surveyed.
     *
     * @return Name of what is being surveyed.
     */
    public String getName() {
        return name;
    }

    /**
     * Add a status message to this condensed status. If a condensed status
     * message with the same textual content already exists, it will be updated
     * with information from the status message. Otherwise, a new condensed
     * status message will be added. See the references for how a condensed
     * status message relates to the given status message.
     * Note that data isn't necessarily copied, so any changes to the internal
     * structure of the parameter may affect this datastructure and vice versa.
     *
     * @param message The message to add. Should never be null.
     * @see CondensedStatusMessage#CondensedStatusMessage(StatusMessage)
     * @see CondensedStatusMessage#update(StatusMessage)
     */
    public void addMessage(StatusMessage message) {
        if (messages.containsKey(message.getMessage())) {
            CondensedStatusMessage oldMessage = messages.get(
                    message.getMessage());
            oldMessage.update(message);
        } else {
            messages.put(message.getMessage(), new CondensedStatusMessage(
                    message));
        }
    }

    /**
     * Add a condensed status message to this condensed status. If a condensed
     * status message with the same textual content already exists, it will be
     * updated with information from the status message. Otherwise, this
     * condensed status will be added. See the reference for how a condensed
     * status is updated.
     * Note that data isn't necessarily copied, so any changes to the internal
     * structure of the parameter may affect this datastructure and vice versa.
     *
     * @param message The message to add. Should never be null.
     * @see CondensedStatusMessage#update(CondensedStatusMessage)
     */
    public void addMessage(CondensedStatusMessage message) {
        if (messages.containsKey(message.getMessage())) {
            CondensedStatusMessage oldMessage = messages.get(
                    message.getMessage());
            oldMessage.update(message);
        } else {
            messages.put(message.getMessage(), message);
        }
    }

    /**
     * Remove condensed log status messages with the given textual content.
     * Used for removing a log message from the status when it is no longer
     * relevant.
     * Non-log messages cannot be removed, since they represent a real-time
     * status.
     *
     * @param message The textual content of the message to remove.
     */
    public void removeLogMessage(String message) {
        if ((messages.get(message) != null)
                && messages.get(message).isLogMessage()) {
            messages.remove(message);
        }
    }
}
