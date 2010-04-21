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

package dk.statsbiblioteket.doms.surveillance.log4jappender;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import dk.statsbiblioteket.doms.webservices.ConfigCollection;
import dk.statsbiblioteket.util.qa.QAInfo;

import java.util.Properties;

/**
 * Log4J appender that registers log messages in a registry.
 *
 * The configuration parameter MaxNumberOfMessages will, if present, be
 * delegated to the log registry by setting the parameter
 * <code>dk.statsbiblioteket.doms.surveillance.rest.log4jappender.numberOfMessages</code>
 */
@QAInfo(author = "kfc",
        reviewers = "jrg",
        comment = "Needs review on diff from revision 265",
        level = QAInfo.Level.NORMAL,
        state = QAInfo.State.QA_NEEDED)
public class LogRegistryAppender extends AppenderSkeleton {
    /** Used to configure max number of messages. */
    private int maxNumberOfMessages;

    /** Create new instance. */
    public LogRegistryAppender() {
        super();
    }

    /**
     * This method is called by the log4j framework by introspection if the
     * maxNumberOfMessages property is set. The value may then be read by
     * the log registry.
     *
     * @param maxNumberOfMessages Max number of messages to store in cache.
     */
    private void setMaxNumberOfMessages(int maxNumberOfMessages) {
        Properties p = new Properties();
        p.setProperty(LogRegistry.NUMBEROFMESSAGES_CONFIGURATION_PARAMETER,
                      Integer.toString(maxNumberOfMessages));
        ConfigCollection.addContextConfig(p);
        this.maxNumberOfMessages = maxNumberOfMessages;
    }

    /**
     * Used by the log4j introspection framework, this method will return the
     * value of the parameter maxNumberOfMessages, as set in the log4j
     * configuration file.
     *
     * @return Max number of messages. 0 if not initialized.
     */
    private int getMaxNumberOfMessages() {
        return maxNumberOfMessages;
    }

    /**
     * Cache the event, for later inspection by the surveyor.
     *
     * @param event The event to cache.
     */
    protected void append(LoggingEvent event) {
        LogRegistryFactory.getLogRegistry().setName(getName());
        LogRegistryFactory.getLogRegistry().registerMessage(event);
    }

    /**
     * Release any resources allocated within the appender such as file
     * handles, network connections, etc.
     *
     * Currently does nothing in this implementation.
     */
    public void close() {
        // Does nothing
    }

    /** This appender requires layout. */
    public boolean requiresLayout() {
        return true;
    }
}
