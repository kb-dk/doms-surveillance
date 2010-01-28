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

import javax.servlet.ServletConfig;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/** Class that exposes log messages as surveyable messages over REST. */
@Path("/")
public class FedoraStatusService implements Surveyable {
    /** The application name for what is being surveyed. */
    private static final String APPLICATION_NAME = "fedora";
    /** Logger for this class. */
    private Log log = LogFactory.getLog(getClass());
    /** The web service context. Injected by the web service. */
    @Context
    private ServletConfig config;
    /** Parameter name for Fedora URL. */
    private static final String FEDORA_URL_PARAMETER
            = "dk.statsbiblioteket.doms.surveyor.fedorasurveyor.fedoraUrl";
    /** Parameter name for Fedora user name. */
    private static final String FEDORA_USER_PARAMETER
            = "dk.statsbiblioteket.doms.surveyor.fedorasurveyor.fedoraUser";
    /** Parameter name for Fedora password. */
    private static final String FEDORA_PASSWORD_PARAMETER
            = "dk.statsbiblioteket.doms.surveyor.fedorasurveyor.fedoraPassword";
    /** Read parameter for Fedora URL. */
    private String fedoraUrl;
    /** Read parameter for Fedora user. */
    private String fedoraUser;
    /** Read parameter for Fedora password. */
    private String fedoraPassword;

    @GET
    @Path("getStatusSince/{date}")
    @Produces("application/xml")
    /** Returns the current status of Fedora. On trouble communicating with
     * Fedora, a status reporting this is generated.
     *
     * This method acts as fault barrier for communication with Fedora.
     *
     * @param time Only messages strictly after the given date are returned.
     * @return A status containing list of log messages.
     */
    public Status getStatusSince(@PathParam("date") long time) {
        log.trace("enter getStatusSince(" + time + ")");
        initialize();

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
        ArrayList<StatusMessage> list = new ArrayList<StatusMessage>();
        list.add(statusMessage);
        return new Status(APPLICATION_NAME, list);
    }

    @GET
    @Path("getStatus")
    @Produces("application/xml")
    /** Returns all log messages received.
     *
     * @return A status containing list of log messages.
     */
    public Status getStatus() {
        log.trace("enter getStatus()");
        return getStatusSince(0L);
    }

    /** Initialise the web service by reading the parameters. */
    private synchronized void initialize() {
        String fedoraUrl = config.getInitParameter(FEDORA_URL_PARAMETER);
        if (!this.fedoraUrl.equals(fedoraUrl)) {
            this.fedoraUrl = fedoraUrl;
            log.info("Setting parameter fedoraUrl to '" + fedoraUrl + "'");
        }
        String fedoraUser = config.getInitParameter(FEDORA_USER_PARAMETER);
        if (!this.fedoraUser.equals(fedoraUser)) {
            this.fedoraUser = fedoraUser;
            log.info("Setting parameter fedoraUser to '" + fedoraUser + "'");
        }
        String fedoraPassword = config
                .getInitParameter(FEDORA_PASSWORD_PARAMETER);
        if (!this.fedoraPassword.equals(fedoraPassword)) {
            this.fedoraPassword = fedoraPassword;
            log.info("Setting parameter fedoraPassword to '" + fedoraPassword
                    + "'");
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
        FedoraClient fedoraClient = new FedoraClient(fedoraUrl, fedoraUser,
                                                     fedoraPassword);
        fedoraClient.getAPIA().describeRepository();
        RepositoryInfo description = fedoraClient.getAPIA()
                .describeRepository();
        fedoraClient.getAPIA()
                .getObjectProfile(description.getSamplePID(), null);
        new URL(description.getSampleSearchURL()).openConnection();
        return new StatusMessage(descriptionToStatus(description),
                                 StatusMessage.Severity.GREEN,
                                 System.currentTimeMillis(), false);
    }

    /**
     * Converts a repository description to a string.
     *
     * @param description The repository description
     * @return A string with all information from the repository description.
     */
    private String descriptionToStatus(RepositoryInfo description) {
        if (description == null) {
            throw new IllegalArgumentException(
                    "Parameter 'RepositoryInfo description'"
                            + " should not be null");
        }
        return "AdminEmailList: '"
                + Arrays.toString(description.getAdminEmailList()) + "'<br>\n"
                + "DefaultExportFormat: '"
                + description.getDefaultExportFormat() + "'<br>\n"
                + "OAINamespace: '" + description.getOAINamespace() + "'<br>\n"
                + "RepositoryBaseURL: '" + description.getRepositoryBaseURL()
                + "'<br>\n" + "RepositoryName: '"
                + description.getRepositoryName() + "'<br>\n"
                + "RepositoryPIDNamespace: '"
                + description.getRepositoryPIDNamespace() + "'<br>\n"
                + "RepositoryVersion: '" + description.getRepositoryVersion()
                + "'<br>\n" + "RetainPIDs: '"
                + Arrays.toString(description.getRetainPIDs()) + "'<br>\n"
                + "SampleAccessURL: '" + description.getSampleAccessURL()
                + "'<br>\n" + "SampleOAIIdentifier: '"
                + description.getSampleOAIIdentifier() + "'<br>\n"
                + "SampleOAIURL: '" + description.getSampleOAIURL() + "'<br>\n"
                + "SamplePID: '" + description.getSamplePID() + "'<br>\n"
                + "AdminEmailList: '" + description.getSampleSearchURL() + "'";
    }

}