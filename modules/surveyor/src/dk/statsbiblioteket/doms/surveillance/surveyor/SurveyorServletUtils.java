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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.statsbiblioteket.util.qa.QAInfo;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utility methods for initializing a surveyor from a servlet configuration,
 * and handling servlet requests.
 */
@QAInfo(author = "kfc",
        reviewers = "jrg",
        level = QAInfo.Level.NORMAL,
        state = QAInfo.State.QA_OK)
public class SurveyorServletUtils {
    /** The package prfix for parameter names. */
    private static final String CONFIGURATION_PACKAGE_NAME
            = "dk.statsbiblioteket.doms.surveillance.surveyor";

    /** Parameter for URLS for surveyor. */
    private static final String URLS_CONFIGURATION_PARAMETER
            = CONFIGURATION_PACKAGE_NAME + ".urls";

    /** Parameter for file with ignored messages for surveyor. */
    private static final String IGNOREFILE_CONFIGURATION_PARAMETER
            = CONFIGURATION_PACKAGE_NAME + ".ignoredMessagesFile";

    /** The logger for this class. */
    private static Log log = LogFactory.getLog(SurveyorServletUtils.class);

    /**
     * Initialize a surveyor, reading configuration parameters from the given
     * servlet configuration.
     *
     * @param config Configuration to read parameters for.
     * @return The initialized surveyor.
     */
    public static Surveyor initializeSurveyor(ServletConfig config) {
        log.trace("Enter initializeSurveyor()");
        //Read configuration
        String restUrlParameter = config.getInitParameter(
                URLS_CONFIGURATION_PARAMETER);
        List<String> restUrls;
        if (restUrlParameter == null || restUrlParameter.equals("")) {
            restUrls = Collections.emptyList();
        } else {
            restUrls = Arrays.asList(restUrlParameter.split(";"));
        }
        String ignoredMessagesPath = config.getInitParameter(
                IGNOREFILE_CONFIGURATION_PARAMETER);
        if (ignoredMessagesPath == null || ignoredMessagesPath.equals("")) {
            ignoredMessagesPath = "ignored.txt";
        }

        //Initialise surveyor
        Surveyor surveyor = SurveyorFactory.getSurveyor();
        surveyor.setConfiguration(restUrls, new File(ignoredMessagesPath));
        return surveyor;
    }

    /**
     * Handle actions given a servlet request on a surveyor.
     * Will handle requests to mark a log message as handled, and requests
     * never to show a given message again.
     * @param request The request containing the parameters.
     * @param surveyor The surveyor to call the actions on.
     */
    public static void handlePostedParameters(HttpServletRequest request,
                                               Surveyor surveyor) {
        log.trace("Enter handlePostedParameters()");
        String applicationName;

        try {
            request.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            //UTF-8 must be supported as per spec.
            throw new Error("UTF-8 unsupported by JVM", e);
        }

        applicationName = request.getParameter("applicationname");
        if (applicationName != null) {
            Map<String, String[]> parameters = request.getParameterMap();
            for (String key : parameters.keySet()) {
                if (key.startsWith("handle:") && Arrays.equals(
                        new String[]{"Handled"}, parameters.get(key))) {
                    surveyor.markHandled(
                            applicationName, key.substring("handle:".length()));
                }
            }
            if (request.getParameter("notagain") != null) {
                surveyor.notAgain(
                        applicationName, request.getParameter("notagain"));
            }
        }
    }
}
