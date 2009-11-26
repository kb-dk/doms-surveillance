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

/** Factory for getting the log survey singleton. */
public class LogSurveyFactory {
    /** The log survey singleton. */
    static LogSurvey instance;

    /**
     * Get the log survey instance.
     *
     * @return The log survey instance.
     */
    public static LogSurvey getLogSurvey() {
        if (instance == null) {
            //TODO: Allow setting the implementation to something different.
            instance = new CachingLogSurvey();
        }
        return instance;
    }

}
