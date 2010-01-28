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

import java.io.File;
import java.util.List;
import java.util.Map;

/** Interface for getting status messages. */
public interface Surveyor {
    /**
     * Get the current status.
     *
     * @return Current status.
     */
    Map<String, CondensedStatus> getStatusMap();

    /**
     * Set configuration of surveyor
     *
     * @param restStatusUrls      List of URLs to get status from.
     * @param ignoredMessagesFile File to store list of ignored messages in.
     */
    void setConfiguration(List<String> restStatusUrls,
                          File ignoredMessagesFile);

    /**
     * Mark a message as handled, thus removing it from the list of currently
     * unhandled log messages.
     *
     * @param applicationName Name of application with message.
     * @param message         The message to mark as handled.
     */
    void markHandled(String applicationName, String message);

    /**
     * Mark a message as one that should never be shwon again.
     *
     * @param applicationName Name of application with message.
     * @param message         The message never to show again.
     */
    void notAgain(String applicationName, String message);
}
