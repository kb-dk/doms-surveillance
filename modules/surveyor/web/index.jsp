<%@ page import="dk.statsbiblioteket.doms.surveillance.status.Status" %>
<%@ page import="dk.statsbiblioteket.doms.surveillance.status.StatusMessage" %>
<%@ page
        import="dk.statsbiblioteket.doms.surveillance.surveyor.SurveyorFactory" %>
<%@ page import="java.util.Date" %>
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
        Map<String, Status> statusMap = SurveyorFactory.getSurveyor()
                .getStatusMap();
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