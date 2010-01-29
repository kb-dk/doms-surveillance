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

/** Factory for getting the surveyor singleton. */
@QAInfo(author = "kfc",
        reviewers = "jrg",
        level = QAInfo.Level.NORMAL,
        state = QAInfo.State.QA_OK)
public class SurveyorFactory {
    /** Default implentation class. */
    private static final String DEFAULT_IMPLEMENTATION
            = RestSurveyor.class.getName();

    /** Logger for this class. */
    private static Log log = LogFactory.getLog(SurveyorFactory.class);

    /** The surveyor singleton instance. */
    private static Surveyor surveyor;

    /**
     * Get the surveyor singleton instance. As this produces a singleton,
     * a new instance will only be generated on the first call, after this the
     * same instance will be returned. If the configuration that defines the
     * implementing class is changed, though, a new instance of the new class
     * will be produced. This method is synchronized.
     *
     * @return Surveyor singleton instance.
     *
     * @throws SurveyorInstantiationException on trouble instantiating the
     * singleton.
     */
    public static synchronized Surveyor getSurveyor()
            throws SurveyorInstantiationException {
        log.trace("Enter getSurveyor");
        //TODO: Make implementation configurable
        String implementation = DEFAULT_IMPLEMENTATION;
        if ((surveyor == null)
                || !surveyor.getClass().getName().equals(implementation)) {
            try {
                Class surveyorClass = Class.forName(implementation);
                surveyor = (Surveyor) surveyorClass.newInstance();
                log.debug("Initiated surveyor class '"
                        + implementation + "'");
            } catch (Exception e) {
                throw new SurveyorInstantiationException(
                        "Cannot instantiate Surveyor class '"
                        + implementation + "': " + e.getMessage(), e);
            }
        }
        return surveyor;
    }
}
