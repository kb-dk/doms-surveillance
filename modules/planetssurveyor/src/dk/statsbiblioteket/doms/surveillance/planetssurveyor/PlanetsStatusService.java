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

package dk.statsbiblioteket.doms.surveillance.planetssurveyor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.statsbiblioteket.util.qa.QAInfo;
import dk.statsbiblioteket.doms.domsutil.surveyable.Surveyable;
import dk.statsbiblioteket.doms.domsutil.surveyable.Status;
import dk.statsbiblioteket.doms.domsutil.surveyable.StatusMessage;
import dk.statsbiblioteket.doms.domsutil.surveyable.Severity;
import dk.statsbiblioteket.doms.webservices.ConfigCollection;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;
import java.net.MalformedURLException;

//import eu.planets_project.services.datatypes.ServiceDescription;
//import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.identify.Identify;
//import eu.planets_project.services.identify.IdentifyResult;
import eu.planets_project.services.validate.Validate;
//import eu.planets_project.services.validate.ValidateResult;


/** Class that exposes planets status as surveyable. */
@QAInfo(author = "jrg",
        reviewers = "",
        level = QAInfo.Level.NORMAL,
        state = QAInfo.State.QA_NEEDED)
public class PlanetsStatusService implements Surveyable {
    /** The application name for what is being surveyed. */
    private static final String APPLICATION_NAME = "Planets";

    /** Logger for this class. */
    private Log log = LogFactory.getLog(getClass());

    /** Prefix for parameter names in web.xml */
    private static final String PARAMETER_PACKAGENAME_PREFIX
            = "dk.statsbiblioteket.doms.surveillance.planetssurveyor";

    /** Parameter name for Planets Jhove validation WSDL */
    private static final String JHOVE_VALIDATION_WSDL_PARAMETER
            = PARAMETER_PACKAGENAME_PREFIX + ".jhoveValidationWdsl";

    /** Parameter name for Planets Jhove identification WSDL */
    private static final String JHOVE_IDENTIFICATION_WSDL_PARAMETER
            = PARAMETER_PACKAGENAME_PREFIX + ".jhoveIdentificationWdsl";

    /** Parameter name for Planets Droid identification WSDL */
    private static final String DROID_IDENTIFICATION_WSDL_PARAMETER
            = PARAMETER_PACKAGENAME_PREFIX + ".droidIdentificationWdsl";

    /** WSDL for Planets Jhove validation - parameter from web.xml */
    private final String jhoveValidationWdsl;

    /** WSDL for Planets Jhove identification - parameter from web.xml */
    private final String jhoveIdentificationWdsl;

    /** WSDL for Planets Droid identification - parameter from web.xml */
    private final String droidIdentificationWdsl;


    /**
     * Initialise the surveyable by reading the parameters.
     */
    public PlanetsStatusService() {
        log.trace("Enter PlanetsStatusService()");
        Properties configuration = ConfigCollection.getProperties();

        jhoveValidationWdsl
                = configuration.getProperty(JHOVE_VALIDATION_WSDL_PARAMETER);
        log.info("Setting parameter jhoveValidationWdsl to '"
                + jhoveValidationWdsl + "'");

        jhoveIdentificationWdsl = configuration.getProperty(
                JHOVE_IDENTIFICATION_WSDL_PARAMETER);
        log.info("Setting parameter jhoveIdentificationWdsl to '"
                + jhoveIdentificationWdsl + "'");

        droidIdentificationWdsl = configuration.getProperty(
                DROID_IDENTIFICATION_WSDL_PARAMETER);
        log.info("Setting parameter droidIdentificationWdsl to '"
                + droidIdentificationWdsl + "'");
    }
    

    /**
     * Behaves exactly like getStatus().
     *
     * @param time Ignored.
     * @return A realtime status of Planets.
     * @see #getStatus
     */
    public Status getStatusSince(long time) {
        log.trace("Enter getStatusSince(" + time + ")");
        return getStatus();
    }


    /**
     * Returns the current status of Planets. On trouble communicating with
     * Planets, a status reporting this is generated.
     *
     * This method acts as fault barrier for communication with Planets.
     *
     * @return A realtime status of Planets.
     */
    public Status getStatus() {
        log.trace("Enter getStatus()");
        List<StatusMessage> list = new ArrayList<StatusMessage>();
        StatusMessage statusMessage;
        Status status;

        try {
            statusMessage = getJhoveValidatorStatus();
        } catch (Exception e) {
            statusMessage = new StatusMessage();
            statusMessage.setMessage("Unable to communicate with Jhove "
                    + "validator: " + e.getClass().getName() + ": "
                    + e.getMessage());
            statusMessage.setSeverity(Severity.RED);
            statusMessage.setTime(System.currentTimeMillis());
            statusMessage.setLogMessage(false);
        }
        list.add(statusMessage);

        try {
            statusMessage = getJhoveIdentifierStatus();
        } catch (Exception e) {
            statusMessage = new StatusMessage();
            statusMessage.setMessage("Unable to communicate with Jhove "
                    + "identifier: " + e.getClass().getName() + ": "
                    + e.getMessage());
            statusMessage.setSeverity(Severity.RED);
            statusMessage.setTime(System.currentTimeMillis());
            statusMessage.setLogMessage(false);
        }
        list.add(statusMessage);

        try {
            statusMessage = getDroidIdentifierStatus();
        } catch (Exception e) {
            statusMessage = new StatusMessage();
            statusMessage.setMessage("Unable to communicate with Droid "
                    + "identifier: " + e.getClass().getName() + ": "
                    + e.getMessage());
            statusMessage.setSeverity(Severity.RED);
            statusMessage.setTime(System.currentTimeMillis());
            statusMessage.setLogMessage(false);
        }
        list.add(statusMessage);

        status = new Status();
        status.setName(APPLICATION_NAME);
        status.getMessages().addAll(list);
        return status;
    }


    /**
     * Asks the Jhove validator webservice for its name, as a test of whether
     * the webservice is up and running. Creates a status message based on the
     * outcome.
     *
     * @return The status of the Planets Jhove validator webservice.
     */
    private StatusMessage getJhoveValidatorStatus() {
        StatusMessage statusMessage = new StatusMessage();
        URL url;
        String message;
        Severity severity;

        try {
            url = new URL(jhoveValidationWdsl);
        } catch (MalformedURLException e) {
            statusMessage.setMessage("Unable to communicate with Jhove"
                    + " validator webservice: " + e.getClass().getName() + ": "
                    + e.getMessage());
            statusMessage.setSeverity(Severity.RED);
            statusMessage.setTime(System.currentTimeMillis());
            statusMessage.setLogMessage(false);
            return statusMessage;
        }

        // 1st arg namespace URI, 2nd arg service name from WSDL
        QName qname = new QName("http://planets-project.eu/services",
                "Validate");                     

        try {
            Service service = Service.create(url, qname);
            Validate jhoveValidator = service.getPort(Validate.class);

            //String nameResponse =
            jhoveValidator.describe().getName();

            message = "Jhove validator webservice running";
            severity = Severity.GREEN;
        } catch (Exception e) {
            statusMessage.setMessage("Unable to communicate with Jhove"
                    + " validator webservice: " + e.getClass().getName() + ": "
                    + e.getMessage());
            statusMessage.setSeverity(Severity.RED);
            statusMessage.setTime(System.currentTimeMillis());
            statusMessage.setLogMessage(false);
            return statusMessage;
        }

        statusMessage.setMessage(message);
        statusMessage.setSeverity(severity);
        statusMessage.setTime(System.currentTimeMillis());
        statusMessage.setLogMessage(false);
        return statusMessage;
    }


    /**
     * Asks the Jhove identifier webservice for its name, as a test of whether
     * the webservice is up and running. Creates a status message based on the
     * outcome.
     *
     * @return The status of the Planets Jhove identifier webservice.
     */
    private StatusMessage getJhoveIdentifierStatus() {
        StatusMessage statusMessage = new StatusMessage();
        URL url;
        String message;
        Severity severity;

        try {
            url = new URL(jhoveIdentificationWdsl);
        } catch (MalformedURLException e) {
            statusMessage.setMessage("Unable to communicate with Jhove"
                    + " identifier webservice: " + e.getClass().getName() + ": "
                    + e.getMessage());
            statusMessage.setSeverity(Severity.RED);
            statusMessage.setTime(System.currentTimeMillis());
            statusMessage.setLogMessage(false);
            return statusMessage;
        }

        // 1st arg namespace URI, 2nd arg service name from WSDL
        QName qname = new QName("http://planets-project.eu/services",
                "Identify");

        try {
            Service service = Service.create(url, qname);
            Identify jhoveIdentifier = service.getPort(Identify.class);
            
            //String nameResponse =
            jhoveIdentifier.describe().getName();

            message = "Jhove identifier webservice running";
            severity = Severity.GREEN;
        } catch (Exception e) {
            statusMessage.setMessage("Unable to communicate with Jhove"
                    + " identifier webservice: " + e.getClass().getName() + ": "
                    + e.getMessage());
            statusMessage.setSeverity(Severity.RED);
            statusMessage.setTime(System.currentTimeMillis());
            statusMessage.setLogMessage(false);
            return statusMessage;
        }

        statusMessage.setMessage(message);
        statusMessage.setSeverity(severity);
        statusMessage.setTime(System.currentTimeMillis());
        statusMessage.setLogMessage(false);
        return statusMessage;
    }


    /**
     * Asks the Droid webservice for its name, as a test of whether the
     * webservice is up and running. Creates a status message based on the
     * outcome.
     *
     * @return The status of the Planets Droid webservice.
     */
    private StatusMessage getDroidIdentifierStatus() {
        StatusMessage statusMessage = new StatusMessage();
        URL url;
        String message;
        Severity severity;

        try {
            url = new URL(droidIdentificationWdsl);
        } catch (MalformedURLException e) {
            statusMessage.setMessage("Unable to communicate with Droid"
                    + " webservice: " + e.getClass().getName() + ": "
                    + e.getMessage());
            statusMessage.setSeverity(Severity.RED);
            statusMessage.setTime(System.currentTimeMillis());
            statusMessage.setLogMessage(false);
            return statusMessage;
        }

        // 1st arg namespace URI, 2nd arg service name from WSDL
        QName qname = new QName("http://planets-project.eu/services",
                "Identify");

        try {
            Service service = Service.create(url, qname);
            Identify droidIdentifier = service.getPort(Identify.class);

            //String nameResponse =
            droidIdentifier.describe().getName();

            message = "Droid webservice running";
            severity = Severity.GREEN;
        } catch (Exception e) {
            statusMessage.setMessage("Unable to communicate with Droid"
                    + " webservice: " + e.getClass().getName() + ": "
                    + e.getMessage());
            statusMessage.setSeverity(Severity.RED);
            statusMessage.setTime(System.currentTimeMillis());
            statusMessage.setLogMessage(false);
            return statusMessage;
        }

        statusMessage.setMessage(message);
        statusMessage.setSeverity(severity);
        statusMessage.setTime(System.currentTimeMillis());
        statusMessage.setLogMessage(false);
        return statusMessage;
    }


    
}
