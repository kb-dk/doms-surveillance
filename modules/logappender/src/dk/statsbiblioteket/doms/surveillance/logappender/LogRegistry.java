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

package dk.statsbiblioteket.doms.surveillance.logappender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.apache.log4j.spi.LoggingEvent;

import dk.statsbiblioteket.doms.domsutil.surveyable.Surveyable;
import dk.statsbiblioteket.util.qa.QAInfo;

/** Interface for a surveyable log message registry.
 * Implementations may be configured by the parameter:
 * <code>dk.statsbiblioteket.doms.surveillance.rest.log4jappender.numberOfMessages</code> 
 * */
@QAInfo(author = "kfc",
        reviewers = "jrg",
        comment = "Needs review on diff from revision 265",
        level = QAInfo.Level.NORMAL,
        state = QAInfo.State.QA_NEEDED)
public interface LogRegistry {
    /** The package prefix for parameter names. */
    String CONFIGURATION_PACKAGE_NAME
            = "dk.statsbiblioteket.doms.surveillance.logappender";
    /** Parameter for now many log messages are kept in the registry. */
    String NUMBEROFMESSAGES_CONFIGURATION_PARAMETER
            = CONFIGURATION_PACKAGE_NAME + ".numberOfMessages";
    /** At most this many log messages are by default kept in the registry. */
    int DEFAULT_MAX_NUMBER_OF_MESSAGES_KEPT_BY_LOG = 1000;

    /**
     * Register a message for later inspection.
     *
     * @param appender The name of the appender to register in.
     * @param event The log message to register.
     */
    public void registerMessage(String appender, LoggingEvent event);

    /**
     * Register a message for later inspection.
     *
     * @param appender The name of the appender to register in.
     * @param event The log message to register.
     */
    public void registerMessage(String appender, ILoggingEvent event);

    /**
     * List surveyables with registered content.
     * @return List of names of surveyables.
     */
    public Iterable<String> listSurveyables();

    /**
     * Get surveyable for given appender.
     *
     * @param appender The name of the appender.
     * @return A surveyable that exposes registered messages for that appender.
     */
    public Surveyable getSurveyable(String appender);
}
