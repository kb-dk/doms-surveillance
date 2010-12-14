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

package dk.statsbiblioteket.doms.surveillance.fedorasurveyor;

import dk.statsbiblioteket.doms.domsutil.surveyable.Severity;
import dk.statsbiblioteket.doms.domsutil.surveyable.Status;
import dk.statsbiblioteket.doms.domsutil.surveyable.StatusMessage;
import dk.statsbiblioteket.doms.domsutil.surveyable.Surveyable;
import dk.statsbiblioteket.doms.webservices.ConfigCollection;
import dk.statsbiblioteket.util.qa.QAInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.xml.DOMConfigurator;
import org.fcrepo.client.FedoraClient;
import org.fcrepo.server.types.gen.RepositoryInfo;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/** Class that exposes fedora status as surveyable. */
@QAInfo(author = "kfc",
        reviewers = "jrg",
        comment = "Needs review on diff from revision 265",
        level = QAInfo.Level.NORMAL,
        state = QAInfo.State.QA_NEEDED)
public class FedoraStatusService implements Surveyable {
    /** The application name for what is being surveyed. */
    private static final String APPLICATION_NAME = "Fedora";

    /** Logger for this class. */
    private static Log log = LogFactory.getLog(FedoraStatusService.class);

    /** Prefix for parameter names. */
    private static final String PARAMETER_PACKAGENAME_PREFIX
            = "dk.statsbiblioteket.doms.surveillance.fedorasurveyor";

    /** Parameter name for Fedora URL. */
    private static final String FEDORA_URL_PARAMETER
            = PARAMETER_PACKAGENAME_PREFIX + ".fedoraUrl";

    /** Parameter name for Fedora user name. */
    private static final String FEDORA_USER_PARAMETER
            = PARAMETER_PACKAGENAME_PREFIX + ".fedoraUser";

    /** Parameter name for Fedora password. */
    private static final String FEDORA_PASSWORD_PARAMETER
            = PARAMETER_PACKAGENAME_PREFIX + ".fedoraPassword";

    /** Read parameter for Fedora URL. */
    private final String fedoraUrl;

    /** Read parameter for Fedora user. */
    private final String fedoraUser;

    /** Read parameter for Fedora password. */
    private final String fedoraPassword;


    static {
       String log4jconfigLocation
               = ConfigCollection.getProperties().getProperty(
               FedoraStatusService.class.getPackage().getName()+".log4jconfig");
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
     * Initialise the surveyable by reading the parameters.
     */
    public FedoraStatusService() {
        log.trace("Enter FedoraStatusService()");
        Properties configuration = ConfigCollection.getProperties();

        fedoraUrl = configuration.getProperty(FEDORA_URL_PARAMETER);
        log.info("Setting parameter fedoraUrl to '" + fedoraUrl + "'");
        fedoraUser = configuration.getProperty(FEDORA_USER_PARAMETER);
        log.info("Setting parameter fedoraUser to '" + fedoraUser + "'");
        fedoraPassword = configuration.getProperty(FEDORA_PASSWORD_PARAMETER);
        log.info("Setting parameter fedoraPassword to '" + fedoraPassword
                + "'");
    }

    /**
     * Behaves exactly like getStatus().
     *
     * @param time Ignored.
     * @return A realtime status of Fedora.
     * @see #getStatus
     */
    public Status getStatusSince(long time) {
        log.trace("Enter getStatusSince(" + time + ")");
        return getStatus();
    }

    /**
     * Returns the current status of Fedora. On trouble communicating with
     * Fedora, a status reporting this is generated.
     *
     * This method acts as fault barrier for communication with Fedora.
     *
     * @return A realtime status of Fedora.
     */
    public Status getStatus() {
        log.trace("Enter getStatus()");
        List<StatusMessage> list = new ArrayList<StatusMessage>();
        StatusMessage statusMessage;

        try {
            statusMessage = getFedoraStatus();
        } catch (Exception e) {
            statusMessage = new StatusMessage();
            statusMessage.setMessage("Unable to communicate with Fedora: "
                            + e.getClass().getName() + ": " + e.getMessage());
            statusMessage.setSeverity(Severity.RED);
            statusMessage.setTime(System.currentTimeMillis());
            statusMessage.setLogMessage(false);
        }
        list.add(statusMessage);

        Status status = new Status();
        status.setName(APPLICATION_NAME);
        status.getMessages().addAll(list);
        return status;
    }

    /**
     * Try to communicate with Fedora using the SOAP API and FedoraClient.
     * This method will call describeRepository, and try to get the object
     * with Sample PID and connect to the Sample Search URL.
     *
     * @return A status containing everything from DescribeRepository.
     *
     * @throws Exception On any trouble communicating with Fedora.
     */
    private StatusMessage getFedoraStatus() throws Exception {
        log.trace("Enter getFedoraStatus()");
        FedoraClient fedoraClient = new FedoraClient(fedoraUrl, fedoraUser,
                                                     fedoraPassword);
        RepositoryInfo description = fedoraClient.getAPIA()
                .describeRepository();

        fedoraClient.getAPIA()
                .getObjectProfile("fedora-system:ContentModel-3.0", null);
        //Done in order to provoke exception on trouble
        new URL(description.getSampleSearchURL()).openConnection();
        StatusMessage statusMessage = new StatusMessage();
        statusMessage.setMessage(descriptionToStatus(description));
        statusMessage.setSeverity(Severity.GREEN);
        statusMessage.setTime(System.currentTimeMillis());
        statusMessage.setLogMessage(false);
        return statusMessage;
    }

    /**
     * Converts a repository description to a string.
     *
     * @param description The repository description. Should never be null.
     * @return A string with all information from the repository description.
     * This is formatted in HTML.
     * @throws IllegalArgumentException On null parameter
     */
    private String descriptionToStatus(RepositoryInfo description) {
        log.trace("Enter descriptionToStatus(...)");
        if (description == null) {
            throw new IllegalArgumentException(
                    "Parameter 'RepositoryInfo description'"
                            + " should not be null");
        }
        return "AdminEmailList: '"
                + Arrays.toString(description.getAdminEmailList()) + "'<br />\n"
                + "DefaultExportFormat: '"
                + description.getDefaultExportFormat() + "'<br />\n"
                + "OAINamespace: '"
                + description.getOAINamespace() + "'<br />\n"
                + "RepositoryBaseURL: '"
                + description.getRepositoryBaseURL() + "'<br />\n"
                + "RepositoryName: '"
                + description.getRepositoryName() + "'<br />\n"
                + "RepositoryPIDNamespace: '"
                + description.getRepositoryPIDNamespace() + "'<br />\n"
                + "RepositoryVersion: '"
                + description.getRepositoryVersion() + "'<br />\n"
                + "RetainPIDs: '"
                + Arrays.toString(description.getRetainPIDs()) + "'<br />\n"
                + "SampleAccessURL: '"
                + description.getSampleAccessURL() + "'<br />\n"
                + "SampleOAIIdentifier: '"
                + description.getSampleOAIIdentifier() + "'<br />\n"
                + "SampleOAIURL: '"
                + description.getSampleOAIURL() + "'<br />\n"
                + "SamplePID: '"
                + description.getSamplePID() + "'<br />\n"
                + "SampleSearchURL: '" 
                + description.getSampleSearchURL() + "'";
    }

}