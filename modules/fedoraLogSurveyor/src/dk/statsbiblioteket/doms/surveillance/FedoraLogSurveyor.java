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

package dk.statsbiblioteket.doms.surveillance;

import dk.statsbiblioteket.doms.domsutil.surveyable.Status;
import dk.statsbiblioteket.doms.domsutil.surveyable.Surveyable;
import dk.statsbiblioteket.doms.surveillance.logappender.LogRegistryFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by IntelliJ IDEA.
 * User: abr
 * Date: Dec 9, 2010
 * Time: 1:28:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class FedoraLogSurveyor implements Surveyable {

        /** The name this status reports. */
    private static final String SURVEYABLE_NAME = "Doms Central";

    /** Log for this class. */
    private final Log log = LogFactory.getLog(getClass());

    public static final String LOGAPPENDERNAME = "DOMS";

    /**
     *
     */
    public Status getStatusSince(long l) {
        log.trace("Enter getStatusSince(" + l + ")");

        Surveyable surveyer = LogRegistryFactory.getLogRegistry().getSurveyable(LOGAPPENDERNAME);
        if (surveyer != null){
            return surveyer.getStatusSince(l);
        } else {
            return null;
        }
/*

        Status status = new Status();
        StatusMessage statusMessage = new StatusMessage();

        statusMessage.setLogMessage(false);
        statusMessage.setSeverity(Severity.GREEN);
        statusMessage.setTime(System.currentTimeMillis());
        statusMessage.setMessage("Running");
        status.setName(SURVEYABLE_NAME);
        status.getMessages().add(statusMessage);
        return status;
*/
    }

    /**
     * Reports exactly the same as getStatusSince(0L).
     * @return Status.
     */
    public Status getStatus() {
        log.trace("Enter getStatus()");

        Surveyable surveyer = LogRegistryFactory.getLogRegistry().getSurveyable(LOGAPPENDERNAME);
        if (surveyer != null){
            return surveyer.getStatus();
        } else {
            return null;
        }
    }
}

