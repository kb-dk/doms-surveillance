<%@ page import="dk.statsbiblioteket.doms.surveillance.status.Status" %>
<%@ page import="dk.statsbiblioteket.doms.surveillance.status.StatusMessage" %>
<%@ page
        import="dk.statsbiblioteket.doms.surveillance.surveyor.Surveyor" %>
<%@ page
        import="dk.statsbiblioteket.doms.surveillance.surveyor.SurveyorFactory" %>
<%@ page import="org.apache.log4j.Level" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html
PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
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
        Surveyor surveyor = SurveyorFactory.getSurveyor();
        ServletConfig context = pageContext.getServletConfig();
        String restUrlParameter = context.getInitParameter(
                "dk.statsbiblioteket.doms.surveillance.surveyor.urls");
        Enumeration parameterNames = context.getInitParameterNames();
        System.out.println("Hello");
        while (parameterNames.hasMoreElements()) {
            String name = (String) parameterNames.nextElement();
            System.out.println(
                    "Parameter: " + name + " Value: " + context
                            .getInitParameter(name));
        }

        Logger.getLogger("dk.statsbiblioteket.doms.surveyor.surveillance.Test")
                .log(
                        Level.FATAL, "Oh noes!!!!");

        List<String> restUrls = Arrays.asList(restUrlParameter.split(";"));
        surveyor.setRestStatusUrls(restUrls);
        Map<String, Status> statusMap = surveyor.getStatusMap();
        for (Status status : statusMap.values()) {
    %>
    <tr>
        <td colspan="4" style="background-color: #AAAAAA"><strong><%= status
                .getName() %>
        </strong></td>
    </tr>
    <%
        for (StatusMessage statusMessage : status.getMessages()) {
    %>
    <tr>
        <td><img
                src="<%= statusMessage.getSeverity().toString().toLowerCase() %>.jpg"
                alt="<%= statusMessage.getSeverity() %>"/></td>
        <td><%= statusMessage.getMessage() %>
        </td>
        <td style="background-color: #888888"><%= new Date(
                statusMessage.getTime()) %>
        </td>
        <%
            if (statusMessage.isLogMessage()) {
        %>
        <%-- TODO: Make buttons functional --%>
        <td><input type="submit" name="handle0" value="Handled"/><br/><input
                type="checkbox" name="notagain0"/>Don't show again
        </td>
        <%
            }
        %>
    </tr>
    <%
            }
        }
    %>
    </tbody>
</table>
</body>
</html>