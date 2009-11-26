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
import java.util.Date;

/** The interface for a status message for a surveyed application. */
@XmlRootElement
public interface StatusMessage {
    /** Severity defined in a classic traffic-light fashion. */
    enum Severity {
        GREEN, RED, YELLOW
    }

    /**
     * Get the text for the status message. May be formatted in HTML.
     *
     * @return The message. Never null.
     */
    @XmlElement
    public String getMessage();

    /**
     * Get the severity of the message.
     *
     * @return The severity. Never null.
     */
    @XmlElement
    public Severity getSeverity();

    /**
     * Get the time for this message. For log messages, this is the time it
     * was logged. For status messages, this is the first time this state
     * was true.
     *
     * @return The time this message was first generated. Never null.
     */
    @XmlElement
    public Date getTime();

    /**
     * Whether this message is about the immediate state of the system, or
     * some logged message.
     *
     * @return true if the message is a log message, false if it is a
     *         message about the current state. Never null.
     */
    @XmlElement
    public boolean isLogMessage();
}
