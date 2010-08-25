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

import junit.framework.TestCase;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import dk.statsbiblioteket.doms.domsutil.surveyable.Severity;

/** Test the LogAppender */
public class LogRegistryAppenderTest extends TestCase {
    public void testAppend() {
        Logger logger = Logger.getLogger(LogRegistryAppenderTest.class);
        logger.log(Level.WARN, "Oh noes!");
        LogRegistry ls = LogRegistryFactory.getLogRegistry();
        assertEquals(
                "Should have one logged message", 1,
                ls.getSurveyable(ls.listSurveyables().iterator().next()).getStatus().getMessages().size());
        assertTrue(
                "Should be a log message",
                ls.getSurveyable(ls.listSurveyables().iterator().next()).getStatus().getMessages().get(0).isLogMessage());
        assertTrue(
                "Should contain the log statement",
                ls.getSurveyable(ls.listSurveyables().iterator().next()).getStatus().getMessages().get(0).getMessage().contains(
                        "Oh noes!"));
        assertEquals(
                "Should be yellow", Severity.YELLOW,
                ls.getSurveyable(ls.listSurveyables().iterator().next()).getStatus().getMessages().get(0).getSeverity());
        assertEquals(
                "Should have right name", "LogRegistry",
                ls.getSurveyable(ls.listSurveyables().iterator().next()).getStatus().getName());
    }
}
