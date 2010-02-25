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

import fedora.client.FedoraClient;
import fedora.server.types.gen.RepositoryInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.statsbiblioteket.doms.surveillance.status.Status;
import dk.statsbiblioteket.doms.surveillance.status.StatusMessage;
import dk.statsbiblioteket.doms.surveillance.status.Surveyable;
import dk.statsbiblioteket.doms.webservices.ConfigCollection;
import dk.statsbiblioteket.util.qa.QAInfo;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/** Class that exposes fedora status as surveyable messages over REST. */
@QAInfo(author = "kfc",
        reviewers = "jrg",
        comment = "Needs review on diff from revision 265",
        level = QAInfo.Level.NORMAL,
        state = QAInfo.State.QA_NEEDED)
@Path("/")
public class FedoraStatusService implements Surveyable {
    /** The application name for what is being surveyed. */
    private static final String APPLICATION_NAME = "fedora";

    /** Logger for this class. */
    private Log log = LogFactory.getLog(getClass());

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
    private String fedoraUrl = "http://localhost:8080/fedora";

    /** Read parameter for Fedora user. */
    private String fedoraUser = "fedoraAdmin";

    /** Read parameter for Fedora password. */
    private String fedoraPassword = "fedoraAdminPass";

    /**
     * Behaves exactly like getStatus().
     *
     * @param time Ignored.
     * @return A realtime status of Fedora.
     * @see #getStatus
     */
    @GET
    @Path("getStatusSince/{date}")
    @Produces("application/xml")
    public Status getStatusSince(@PathParam("date") long time) {
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
    @GET
    @Path("getStatus")
    @Produces("application/xml")
    public Status getStatus() {
        log.trace("Enter getStatus()");
        List<StatusMessage> list = new ArrayList<StatusMessage>();
        StatusMessage statusMessage;

        try {
            statusMessage = getFedoraStatus();
        } catch (Exception e) {
            statusMessage = new StatusMessage(
                    "Unable to communicate with Fedora: "
                            + e.getClass().getName() + ": " + e.getMessage(),
                    StatusMessage.Severity.RED, System.currentTimeMillis(),
                    false);
        }
        list.add(statusMessage);

        return new Status(APPLICATION_NAME, list);
    }

    /**
     * Initialise the web service by reading the parameters.
     * This method acts as fault barrier for configuration problems.
     */
    @PostConstruct
    private synchronized void initialize() {
        log.trace("Enter initialize()");
        try {
            Properties configuration = ConfigCollection.getProperties();
            String fedoraUrl = configuration.getProperty(FEDORA_URL_PARAMETER);
            String fedoraUser = configuration
                    .getProperty(FEDORA_USER_PARAMETER);
            String fedoraPassword = configuration
                    .getProperty(FEDORA_PASSWORD_PARAMETER);

            if (this.fedoraUrl == null || !this.fedoraUrl.equals(fedoraUrl)) {
                this.fedoraUrl = fedoraUrl;
                log.info("Setting parameter fedoraUrl to '" + fedoraUrl + "'");
            }
            if (this.fedoraUser == null || !this.fedoraUser
                    .equals(fedoraUser)) {
                this.fedoraUser = fedoraUser;
                log.info(
                        "Setting parameter fedoraUser to '" + fedoraUser + "'");
            }
            if (this.fedoraPassword == null || !this.fedoraPassword
                    .equals(fedoraPassword)) {
                this.fedoraPassword = fedoraPassword;
                log.info(
                        "Setting parameter fedoraPassword to '" + fedoraPassword
                                + "'");
            }
        } catch (Exception e) {
            log.error("Error during configuration of Fedora Surveyor", e);
        }
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
                .getObjectProfile(description.getSamplePID(), null);
        //Done in order to provoke exception on trouble
        new URL(description.getSampleSearchURL()).openConnection();
        return new StatusMessage(descriptionToStatus(description),
                                 StatusMessage.Severity.GREEN,
                                 System.currentTimeMillis(), false);
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