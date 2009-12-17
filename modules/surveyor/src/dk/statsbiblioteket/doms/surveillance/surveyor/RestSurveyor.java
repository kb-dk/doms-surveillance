package dk.statsbiblioteket.doms.surveillance.surveyor;

import com.sun.jersey.api.client.Client;

import dk.statsbiblioteket.doms.surveillance.status.Status;
import dk.statsbiblioteket.doms.surveillance.status.StatusMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** A surveyor that calls specified REST URLs to get status. */
public class RestSurveyor implements Surveyor {
    /** Currently stored state, for keeping log messages until handled. */
    private Map<String, CondensedStatus> currentStatus
            = new HashMap<String, CondensedStatus>();
    /** Newest message time from last time we queried a given URL. */
    private Map<String, Long> newestStatus = new HashMap<String, Long>();
    /** List of REST URLs to query. */
    private List<String> restStatusUrls = new ArrayList<String>();

    //TODO: Persist this.
    /** Set of messages to ignore. */
    private Map<String, Set<String>> ignoredMessages
            = new HashMap<String, Set<String>>();

    /**
     * Set list of REST status URLs to query.
     *
     * @param restStatusUrls URLs to query.
     */
    public synchronized void setRestStatusUrls(List<String> restStatusUrls) {
        this.restStatusUrls = restStatusUrls;
    }

    /**
     * Mark a message as handled, thus removing it from the list of currently
     * unhandled log messages.
     *
     * @param applicationName Name of application with message.
     * @param message         The message to mark as handled.
     */
    public synchronized void markHandled(String applicationName,
                                         String message) {
        CondensedStatus status = currentStatus.get(applicationName);
        if (status != null) {
            status.removeLogMessage(message);
        }
    }

    /**
     * Mark a message as one that should never be shwon again.
     *
     * @param applicationName Name of application with message.
     * @param message         The message never to show again.
     */
    public synchronized void notAgain(String applicationName, String message) {
        CondensedStatus status = currentStatus.get(applicationName);
        Set<String> ignored = ignoredMessages.get(applicationName);
        if (ignored == null) {
            ignored = new HashSet<String>();
            ignoredMessages.put(applicationName, ignored);
        }
        ignored.add(message);
        markHandled(applicationName, message);
    }

    /**
     * Get a list of statuses by querying some REST urls, and merging them with
     * previously known unhandled log messages.
     *
     * @return A map of statuses from name to status.
     */
    public synchronized Map<String, CondensedStatus> getStatusMap() {
        Map<String, CondensedStatus> result
                = new HashMap<String, CondensedStatus>();

        if (currentStatus != null) {
            for (CondensedStatus status : currentStatus.values()) {
                List<CondensedStatusMessage> logMessages
                        = new ArrayList<CondensedStatusMessage>();
                Set<String> ignored = ignoredMessages.get(status.getName());
                for (CondensedStatusMessage statusMessage : status
                        .getMessages()) {
                    if (statusMessage.isLogMessage()) {
                        if (ignored != null && !ignored
                                .contains(statusMessage.getMessage())) {
                            logMessages.add(statusMessage);
                        }
                    }
                }
                if (!logMessages.isEmpty()) {
                    result.put(
                            status.getName(),
                            new CondensedStatus(status.getName(), logMessages));
                }
            }
        }

        //TODO: Handle exceptions in REST communication
        Client c = Client.create();
        for (String statusUrl : restStatusUrls) {
            Long newest = newestStatus.get(statusUrl);
            if (newest == null) {
                newest = 0l;
            }
            String queryUrl;
            if (statusUrl.contains("{date}")) {
                queryUrl = statusUrl.replace("{date}", Long.toString(newest));
            } else {
                queryUrl = statusUrl;
            }
            Status status = c.resource(queryUrl).get(Status.class);
            Set<String> ignored = ignoredMessages.get(status.getName());
            CondensedStatus s2 = result.get(status.getName());
            if (s2 == null) {
                s2 = new CondensedStatus(status);
                result.put(status.getName(), s2);
            }
            for (StatusMessage message : status.getMessages()) {

                if (ignored != null && !ignored
                        .contains(message.getMessage())) {
                    s2.addMessage(message);
                }
            }

            for (StatusMessage message : status.getMessages()) {
                if (newest < message.getTime()) {
                    newestStatus.put(statusUrl, message.getTime());
                }
            }
        }
        currentStatus = result;
        return result;
    }
}
