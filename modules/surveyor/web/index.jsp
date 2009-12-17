<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html
PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page
        import="dk.statsbiblioteket.doms.surveillance.surveyor.CondensedStatus,
                dk.statsbiblioteket.doms.surveillance.surveyor.CondensedStatusMessage,
                dk.statsbiblioteket.doms.surveillance.surveyor.Surveyor,
                dk.statsbiblioteket.doms.surveillance.surveyor.SurveyorFactory,
                java.net.URLEncoder,
                java.util.Arrays,
                java.util.Date,
                java.util.List,
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
        Surveyor surveyor = SurveyorFactory.getSurveyor();
        String restUrlParameter = config.getInitParameter(
                "dk.statsbiblioteket.doms.surveillance.surveyor.urls");

        request.setCharacterEncoding("UTF-8");
        String applicationName = request.getParameter("applicationname");
        if (applicationName != null) {
            Map<String, String[]> parameters = request.getParameterMap();
            for (String key : parameters.keySet()) {
                if (key.startsWith("handle:") && Arrays.equals(
                        new String[]{"Handled"}, parameters.get(key))) {
                    surveyor.markHandled(
                            applicationName, key.substring("handle:".length()));
                }
            }
            Thread.sleep(1);
            if (request.getParameter("notagain") != null) {
                surveyor.notAgain(
                        applicationName, request.getParameter("notagain"));
            }
        }
        List<String> restUrls = Arrays.asList(restUrlParameter.split(";"));
        surveyor.setRestStatusUrls(restUrls);
        Map<String, CondensedStatus> statusMap = surveyor.getStatusMap();
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
            for (CondensedStatusMessage statusMessage : status.getMessages()) {
        %>
        <tr>
            <td><img
                    src="<%= request.getContextPath() + "/" + statusMessage.getSeverity().toString().toLowerCase() %>.jpg"
                    alt="<%= statusMessage.getSeverity() %>"/></td>
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
                       value="Handled"/><br/><input
                    type="checkbox" name="notagain"
                    value="<%= statusMessage.getMessage().replaceAll("\\\"", "&quot;") %>"/>Don't
                show again
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