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
package dk.statsbiblioteket.doms.surveillance.status;

import dk.statsbiblioteket.util.qa.QAInfo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** A datastructure for system status. */
@QAInfo(author = "kfc",
        reviewers = "jrg",
        level = QAInfo.Level.NORMAL,
        state = QAInfo.State.QA_OK)
@XmlRootElement
public class Status {
    /** The name of what is being surveyed. */
    private String name;

    /** A list of status messages. */
    private List<StatusMessage> messages;

    /** Default no-args constructor. */
    private Status() {
        name = "";
        messages = new ArrayList<StatusMessage>();
    }

    /**
     * Initialise the tuple.
     *
     * @param name     The name of what is being surveyed.
     * @param messages The list of status messages.
     */
    public Status(String name, Collection<StatusMessage> messages) {
        this.name = name;
        this.messages = new ArrayList<StatusMessage>(messages);
    }

    /**
     * A list of status messages.
     *
     * @return A list of status messages.
     */
    @XmlElement
    public List<StatusMessage> getMessages() {
        return messages;
    }

    /**
     * Name of what is being surveyed.
     *
     * @return Name of what is being surveyed.
     */
    @XmlElement
    public String getName() {
        return name;
    }

    /**
     * Set the list of status messages.
     *
     * @param messages A list of status messages.
     */
    public void setMessages(List<StatusMessage> messages) {
        this.messages = messages;
    }

    /**
     * Set the name of what is being surveyed.
     *
     * @param name Name of what is being surveyed.
     */
    public void setName(String name) {
        this.name = name;
    }


}
