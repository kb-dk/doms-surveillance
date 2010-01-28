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

/** The interface of something that offers status messages for surveillance. */
@QAInfo(author = "kfc",
        reviewers = "jrg",
        level = QAInfo.Level.NORMAL,
        state = QAInfo.State.QA_OK)
public interface Surveyable {
    /**
     * Get all status messages newer than the given time.
     * An application should use the newest timestamp in the given messages
     * from the last call as input to this method next time it calls it, to
     * ensure not losing messages.
     *
     * @param time Only get messages strictly newer than this timestamp. The
     * timestamp is measured in milliseconds since 1970-01-01 00:00:00.000Z.
     * @return List of status messages. May be empty, but never null.
     */
    public Status getStatusSince(long time);

    /**
     * Get all status messages. This behaves exactly like
     * getMessagesSince().
     *
     * @return List of status messages. May be empty, but never null.
     *
     * @see #getStatusSince(long)
     */
    public Status getStatus();

}
