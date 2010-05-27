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

package dk.statsbiblioteket.doms.surveillance.logregistrywebservice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.statsbiblioteket.doms.domsutil.surveyable.Status;
import dk.statsbiblioteket.doms.surveillance.log4jappender.LogRegistryFactory;
import dk.statsbiblioteket.util.qa.QAInfo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * Expose a log registry over REST.
 */
@Path("/")
@QAInfo(author = "kfc",
        reviewers = "jrg",
        level = QAInfo.Level.NORMAL,
        state = QAInfo.State.QA_NEEDED)
public class LogRegistryService {
    /** Log for this class. */
    private final Log log = LogFactory.getLog(getClass());

    /**
     * Return contents of named log registry.
     *
     * @param name The log registry to expose
     * @param time Only return messages strictly newer than this date. Measured
     * in milliseconds since 1970-01-01 00:00:00Z
     *
     * @return Contents of the log registry. May be empty, but never null.
     */
    @GET
    @Path("appender/{name}/getStatusSince/{time}")
    @Produces("application/xml")
    public Status getStatusSince(
            @PathParam("name") String name, @PathParam("time") long time) {
        log.trace("Enter getStatusSince('" + name + "', '" + time + "')");
        return LogRegistryFactory.getLogRegistry().getSurveyable(name)
                .getStatusSince(time);
    }

    /**
     * Return contents of named log registry.
     *
     * @param name The log registry to expose
     *
     * @return Contents of the log registry. May be empty, but never null.
     */
    @GET
    @Path("appender/{name}/getStatus")
    @Produces("application/xml")
    public Status getStatus(@PathParam("name") String name) {
        log.trace("Enter getStatus('" + name + "')");
        return LogRegistryFactory.getLogRegistry().getSurveyable(name)
                .getStatus();
    }

    /**
     * Return list of appenders
     *
     * @return List of names of appenders.
     */
    @GET
    @Path("listAppenders")
    @Produces("application/xml")
    public Iterable<String> listAppenders() {
        log.trace("Enter listAppenders()");
        return LogRegistryFactory.getLogRegistry().listSurveyables();
    }
}
