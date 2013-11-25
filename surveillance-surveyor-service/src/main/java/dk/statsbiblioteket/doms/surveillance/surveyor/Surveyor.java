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

import dk.statsbiblioteket.util.qa.QAInfo;

import java.util.Map;

/** Interface for getting status messages. */
@QAInfo(author = "kfc",
        reviewers = "jrg",
        comment = "Needs review on diff from revision 265",
        level = QAInfo.Level.NORMAL,
        state = QAInfo.State.QA_NEEDED)
public interface Surveyor {
    /**
     * Get the current status.
     *
     * @return Current status, described in a map from name to condensed system
     *         status. Never null.
     */
    Map<String, CondensedStatus> getStatusMap();

    /**
     * Mark a message as handled, thus removing it from the list of currently
     * unhandled log messages.
     *
     * @param applicationName Name of application with message. Never null.
     * @param message         The message to mark as handled. Never null.
     */
    void markHandled(String applicationName, String message);

    /**
     * Mark a message as one that should never be shown again.
     *
     * @param applicationName Name of application with message. Never null.
     * @param message         The message never to show again. Never null.
     */
    void notAgain(String applicationName, String message);
}
