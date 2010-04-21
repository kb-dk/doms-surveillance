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

package dk.statsbiblioteket.doms.surveillance.log4jappender;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.statsbiblioteket.doms.domsutil.surveyable.Status;
import dk.statsbiblioteket.doms.domsutil.surveyable.Surveyable;
import dk.statsbiblioteket.util.qa.QAInfo;

/**
 * Class that instantiates the Log Registry singleton when constructed, and
 * exposes it's surveyable methods.
 */
@QAInfo(author = "kfc",
        reviewers = "jrg",
        level = QAInfo.Level.NORMAL,
        state = QAInfo.State.QA_NEEDED)
public class LogRegistrySurveyableSingleton implements Surveyable {
    /** The singleton. */
    private final LogRegistry singleton;

    /** Log for this class. */
    private final Log log = LogFactory.getLog(getClass());

    /** Instantiate the singleton. */
    public LogRegistrySurveyableSingleton() {
        log.trace("Enter LogRegistrySurveyableSingleton()");
        singleton = LogRegistryFactory.getLogRegistry();
    }

    /**
     * Delegate the call to the same method in the singleton.
     *
     * @param time delegated.
     * @return Output from delegated method.
     */
    public Status getStatusSince(long time) {
        log.trace("Enter getStatusSince(" + time + ")");
        return singleton.getStatusSince(time);
    }

    /**
     * Delegate the call to the same method in the singleton.
     *
     * @return Output from delegated method.
     */
    public Status getStatus() {
        log.trace("Enter getStatus()");
        return singleton.getStatus();
    }
}
