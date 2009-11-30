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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** A system status. */
@XmlRootElement
public class Status {
    private final String name;
    private final List<StatusMessage> messages;

    /** Default no-args constructor. */
    private Status() {
        name = "";
        messages = Collections.emptyList();
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
}
