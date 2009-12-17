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

import dk.statsbiblioteket.doms.surveillance.status.Status;
import dk.statsbiblioteket.doms.surveillance.status.StatusMessage;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A system status. */
public class CondensedStatus {
    private String name;
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
     * Initialise the status.
     *
     * @param name     The name of what is being surveyed.
     * @param messages List of status messages.
     */
    public CondensedStatus(String name, List<CondensedStatusMessage> messages) {
        this(name);
        for (CondensedStatusMessage message : messages) {
            addMessage(message);
        }
    }

    public CondensedStatus(Status status) {
        this(status.getName());
        for (StatusMessage message : status.getMessages()) {
            addMessage(message);
        }
    }

    /**
     * A list of status messages.
     *
     * @return A list of status messages.
     */
    public Collection<CondensedStatusMessage> getMessages() {
        return messages.values();
    }

    /**
     * Name of what is being surveyed.
     *
     * @return Name of what is being surveyed.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addMessage(StatusMessage message) {
        if (messages.containsKey(message.getMessage())) {
            CondensedStatusMessage oldMessage = messages
                    .get(message.getMessage());
            oldMessage.update(message);
        } else {
            messages.put(
                    message.getMessage(), new CondensedStatusMessage(message));
        }
    }

    public void addMessage(CondensedStatusMessage message) {
        if (messages.containsKey(message.getMessage())) {
            CondensedStatusMessage oldMessage = messages
                    .get(message.getMessage());
            oldMessage.update(message);
        } else {
            messages.put(message.getMessage(), message);
        }
    }

    public void removeLogMessage(String message) {
        if (messages.get(message) != null && messages.get(message)
                .isLogMessage()) {
            messages.remove(message);
        }
    }
}
