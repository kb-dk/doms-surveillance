<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html
PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%--
  ~ $Id$
  ~ $Revision$
  ~ $Date$
  ~ $Author$
  ~
  ~ The DOMS project.
  ~ Copyright (C) 2007-2010  The State and University Library
  ~
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements.  See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership.  The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License.  You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>
<%@ page
        import="dk.statsbiblioteket.doms.surveillance.surveyor.CondensedStatus,
                dk.statsbiblioteket.doms.surveillance.surveyor.CondensedStatusMessage,
                dk.statsbiblioteket.doms.surveillance.surveyor.Surveyor,
                dk.statsbiblioteket.doms.surveillance.surveyor.SurveyorServletUtils,
                java.net.URLEncoder,
                java.util.Date,
                java.util.Map" pageEncoding="UTF-8" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
    <title>Surveillance</title>
</head>
<body>
<h1>DOMS Surveillance</h1>
<table>
    <thead>
    <tr>
        <th>Status</th>
        <th>Message</th>
        <th>Date</th>
        <th>Handle</th>
    </tr>
    </thead>
    <tbody>
    <%
        // Note: The fault barrier in this case is the Tomcat 500 error page.
        // Initialize surveyor with configuration from servlet
        Surveyor surveyor = SurveyorServletUtils.initializeSurveyor(config);

        // Read and handle request parameters
        SurveyorServletUtils.handlePostedParameters(request, surveyor);

        // Get and present status
        Map<String, CondensedStatus> statusMap = surveyor.getStatusMap();
        // For each application being surveyed
        for (CondensedStatus status : statusMap.values()) {
    %>
    <form action="" method="post">
        <input type="hidden" name="applicationname"
               value="<%= URLEncoder.encode(status.getName(), "UTF-8")%>"/>
        <tr>
            <td colspan="4" style="background-color: #AAAAAA"><strong><%= status
                    .getName() %>
            </strong></td>
        </tr>
        <%
            // For all messages registered for the given application
            for (CondensedStatusMessage statusMessage : status.getMessages()) {
        %>
        <tr>
            <td><img
                    src="<%= request.getContextPath() + "/" + statusMessage.getSeverity().toString().toLowerCase() %>.jpg"
                    alt="<%= statusMessage.getSeverity() %>"/>
            </td>
            <td><%= statusMessage.getMessage() %>
            </td>
            <td style="background-color: #888888">
                <%
                    if (statusMessage.getNumber() > 1) {
                %>
                <%= new Date(statusMessage.getFirstTime()) %><br/>
                - <%= new Date(statusMessage.getLastTime()) %><br/>
                (<%= statusMessage.getNumber() %> times)
                <%
                } else {
                %>
                <%= new Date(statusMessage.getFirstTime()) %>
                <%
                    }
                %>
            </td>
            <%
                if (statusMessage.isLogMessage()) {
            %>
            <td><input type="submit"
                       name="handle:<%= statusMessage.getMessage().replaceAll("\\\"", "&quot;") %>"
                       value="Handled"/><br/>
                <input type="checkbox"
                       name="notagain"
                       value="<%= statusMessage.getMessage().replaceAll("\\\"", "&quot;") %>"/>
                Don't show again
            </td>
            <%
                }
            %>
        </tr>
        <%
            }
        %>
    </form>
    <%
        }
    %>
    </tbody>
</table>
</body>
</html>
