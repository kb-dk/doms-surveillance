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

import dk.statsbiblioteket.doms.webservices.configuration.ConfigCollection;
import dk.statsbiblioteket.util.qa.QAInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.xml.DOMConfigurator;

import java.io.File;

/** Factory for getting the surveyor singleton.
 * The choice of singleton is defined by configuration parameter
 * <code>dk.statsbiblioteket.doms.surveillance.surveyor.surveyorClass</code>.
 * Default is dk.statsbiblioteket.doms.surveillance.surveyor.RestSurveyor. */
@QAInfo(author = "kfc",
        reviewers = "jrg",
        comment = "Needs review on diff from revision 265",
        level = QAInfo.Level.NORMAL,
        state = QAInfo.State.QA_NEEDED)
public class SurveyorFactory {
    /** The package prefix for parameter names. */
    private static final String CONFIGURATION_PACKAGE_NAME
            = "dk.statsbiblioteket.doms.surveillance.surveyor";

    /** Parameter for class for surveyor. */
    public static final String SURVEYORCLASS_CONFIGURATION_PARAMETER
            = CONFIGURATION_PACKAGE_NAME + ".surveyorClass";

    /** Default implementation class. */
    private static final String DEFAULT_IMPLEMENTATION
            = WebServiceSurveyor.class.getName();

    /** Logger for this class. */
    private static Log log = LogFactory.getLog(SurveyorFactory.class);

    /** The surveyor singleton instance. */
    private static Surveyor surveyor;


    static {
       String log4jconfigLocation
               = ConfigCollection.getProperties().getProperty(
               SurveyorFactory.class.getPackage().getName()+".log4jconfig");
       if (log4jconfigLocation != null){
           File configFile = new File(log4jconfigLocation);
           if (configFile.canRead()){
               DOMConfigurator.configure(configFile.getAbsolutePath());
           } else {
               // The file could not be found, either because the path is not
               // an absolute path or because it does not exist. Now try
               // locating it within the WAR file before giving up.
               configFile = new File(ConfigCollection
                       .getServletContext().getRealPath(log4jconfigLocation));
               DOMConfigurator.configure(configFile.getAbsolutePath());
           }
       } else {
           log.error("Failed to load log4jconfig parameter");
       }
   }


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
        String implementation = ConfigCollection.getProperties().getProperty(
                SURVEYORCLASS_CONFIGURATION_PARAMETER);
        if (implementation == null || implementation.equals("")) {
            implementation = DEFAULT_IMPLEMENTATION;
        }
        if ((surveyor == null)
                || !surveyor.getClass().getName().equals(implementation)) {
            log.info("Initializing surveyor class '" + implementation + "'");
            try {
                Class surveyorClass = Class.forName(implementation);
                surveyor = (Surveyor) surveyorClass.newInstance();
            } catch (Exception e) {
                throw new SurveyorInstantiationException(
                        "Cannot instantiate Surveyor class '"
                        + implementation + "': " + e.getMessage(), e);
            }
        }
        return surveyor;
    }
}
