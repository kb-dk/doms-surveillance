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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.statsbiblioteket.doms.webservices.ConfigCollection;
import dk.statsbiblioteket.util.qa.QAInfo;

/** Factory for getting the log registry singleton.
 * The choice of singleton is defined by configuration parameter
 * <code>dk.statsbiblioteket.doms.surveillance.rest.log4jappender.registryClass</code>.
 * Default is dk.statsbiblioteket.doms.surveillance.surveyor.RestSurveyor.
 * */
@QAInfo(author = "kfc",
        reviewers = "jrg",
        comment = "Needs review on diff from revision 265",
        level = QAInfo.Level.NORMAL,
        state = QAInfo.State.QA_NEEDED)
public class LogRegistryFactory {
    /** The package prefix for parameter names. */
    private static final String CONFIGURATION_PACKAGE_NAME
            = "dk.statsbiblioteket.doms.surveillance.rest.logappender";

    /** Parameter for class for log registry. */
    public static final String REGISTRYCLASS_CONFIGURATION_PARAMETER
            = CONFIGURATION_PACKAGE_NAME + ".registryClass";

    /** Default implementation class. */
    private static final String DEFAULT_IMPLEMENTATION
            = CachingLogRegistry.class.getName();

    /** Logger for this class. */
    private static Log log = LogFactory.getLog(LogRegistryFactory.class);

    /** The log registry singleton instance. */
    private static LogRegistry logRegistry;

    /**
     * Get the log registry singleton instance. As this produces a singleton,
     * a new instance will only be generated on the first call, after this the
     * same instance will be returned. If the configuration that defines the
     * implementing class is changed, though, a new instance of the new class
     * will be produced. This method is synchronized.
     *
     * @return Log registry singleton instance.
     *
     * @throws LogRegistryInstantiationException on trouble instantiating the
     * singleton.
     */
    public static synchronized LogRegistry getLogRegistry()
            throws LogRegistryInstantiationException {
        log.trace("Enter getLogRegistry()");

        String implementation = ConfigCollection.getProperties().getProperty(
                REGISTRYCLASS_CONFIGURATION_PARAMETER);
        if (implementation == null || implementation.equals("")) {
            implementation = DEFAULT_IMPLEMENTATION;
        }
        if ((logRegistry == null)
                || !logRegistry.getClass().getName().equals(implementation)) {
            log.info("Initializing log registry class '" + implementation
                    + "'");
            try {
                Class logRegistryClass = Class.forName(implementation);
                logRegistry = (LogRegistry) logRegistryClass.newInstance();
            } catch (Exception e) {
                throw new LogRegistryInstantiationException(
                        "Cannot instantiate LogRegistry class '"
                                + implementation + "': " + e.getMessage(), e);
            }
        }
        return logRegistry;
    }
}
