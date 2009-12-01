/* $Id$
 * $Revision$
 * $Date$
 * $Author$
 *
 * The DOMS project.
 * Copyright (C) 2007-2009  The State and University Library
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
 */
package dk.statsbiblioteket.doms.surveillance.rest.log4jappender;

import dk.statsbiblioteket.doms.surveillance.status.Status;
import dk.statsbiblioteket.doms.surveillance.status.Surveyable;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/** Class that exposes log messages as surveyable messages over REST. */
@Path("/")
public class LogSurveyService implements Surveyable {
    @GET
    @Path("getStatusSince/{date}")
    @Produces("application/xml")
    /** Returns all log messages received since the given date.
     *
     * @param time Only messages strictly after the given date are returned.
     * @return A status containing list of log messages.
     */
    public Status getStatusSince(@PathParam("date") long time) {
        return LogSurveyFactory.getLogSurvey().getStatusSince(time);
    }

    @GET
    @Path("getStatus")
    @Produces("application/xml")
    /** Returns all log messages received.
     *
     * @return A status containing list of log messages.
     */
    public Status getStatus() {
        return LogSurveyFactory.getLogSurvey().getStatus();
    }
}
