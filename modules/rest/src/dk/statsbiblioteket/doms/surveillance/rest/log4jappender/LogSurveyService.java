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
package dk.statsbiblioteket.doms.surveillance.rest.log4jappender;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.statsbiblioteket.doms.surveillance.status.Status;
import dk.statsbiblioteket.doms.surveillance.status.StatusMessage;
import dk.statsbiblioteket.doms.surveillance.status.Surveyable;
import dk.statsbiblioteket.util.qa.QAInfo;

import javax.servlet.ServletConfig;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.util.Collections;
import java.util.List;

/**
 * Class that exposes log messages from a log registry as surveyable messages
 * over REST.
 */
@Path("/")
@QAInfo(author = "kfc",
        reviewers = "jrg",
        level = QAInfo.Level.NORMAL,
        state = QAInfo.State.QA_OK)
public class LogSurveyService implements Surveyable {
    /** REST web service servlet configuration. Injected at runtime. */
    @Context
    private ServletConfig config;

    /** Logger for this class. */
    private Log log = LogFactory.getLog(getClass());

    @GET
    @Path("getStatusSince/{date}")
    @Produces("application/xml")
    /**
     * Returns all log messages received since the given date.
     * This method works as a fault barrier, and converts exceptions to status
     * messages.
     *
     * @param time Only messages strictly after the given date are returned.
     * @return A status containing list of log messages.
     */
    public Status getStatusSince(@PathParam("date") long time) {
        log.trace("Enter getStatusSince(" + time + ")");
        try {
            return LogRegistryFactory.getLogRegistry().getStatusSince(time);
        } catch (Exception e) {
            return createStatusFromException(e);
        }
    }

    @GET
    @Path("getStatus")
    @Produces("application/xml")
    /**
     * Returns all log messages received.
     * This method works as a fault barrier, and converts exceptions to status
     * messages.
     *
     * @return A status containing list of log messages.
     */
    public Status getStatus() {
        log.trace("Enter getStatus()");
        try {
            return LogRegistryFactory.getLogRegistry().getStatus();
        } catch (Exception e) {
            return createStatusFromException(e);
        }
    }

    /**
     * Creates a status message from exception.
     *
     * @param e The exception.
     * @return A status message.
     */
    private Status createStatusFromException(Exception e) {
        log.trace("Enter createStatusFromException(" + e + ")");
        String message =
                "Unable to expose log messages from registry using REST: "
                        + e.getClass().getName() + ": " + e.getMessage();
        String serviceName = config == null ? "Unknown service"
                : config.getServletName();
        List<StatusMessage> messages = Collections.singletonList(
                new StatusMessage(message, StatusMessage.Severity.RED,
                                  System.currentTimeMillis(), false));
        log.warn(message, e);
        return new Status(serviceName, messages);
    }
}
