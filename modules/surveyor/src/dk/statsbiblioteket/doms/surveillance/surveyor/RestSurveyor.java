package dk.statsbiblioteket.doms.surveillance.surveyor;

import com.sun.jersey.api.client.Client;
import dk.statsbiblioteket.doms.surveillance.status.Status;
import dk.statsbiblioteket.doms.surveillance.status.StatusMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A surveyor that calls specified REST URLs to get status. */
public class RestSurveyor implements Surveyor {
    /** Currently stored state, for keeping log messages until handled. */
    private Map<String, Status> currentStatus;
    /** Newest message time from last time we queried a given URL. */
    private Map<String, Long> newestStatus = new HashMap<String, Long>();
    /** List of REST URLs to query. */
    private List<String> restStatusUrls;

    /**
     * Set list of REST status URLs to query.
     *
     * @param restStatusUrls URLs to query.
     */
    public synchronized void setRestStatusUrls(List<String> restStatusUrls) {
        this.restStatusUrls = restStatusUrls;
    }

    /**
     * Get a list of statuses by querying some REST urls, and merging them with
     * previously known unhandled log messages.
     *
     * @return A map of statuses from name to status.
     */
    public synchronized Map<String, Status> getStatusMap() {
        Map<String, Status> result = new HashMap<String, Status>();

        if (currentStatus != null) {
            for (Status status : currentStatus.values()) {
                List<StatusMessage> logMessages
                        = new ArrayList<StatusMessage>();
                for (StatusMessage statusMessage : status.getMessages()) {
                    if (statusMessage.isLogMessage()) {
                        logMessages.add(statusMessage);
                    }
                }
                if (!logMessages.isEmpty()) {
                    result.put(
                            status.getName(),
                            new Status(status.getName(), logMessages));
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
            Status s2 = result.get(status.getName());
            if (s2 == null) {
                result.put(status.getName(), status);
            } else {
                List<StatusMessage> messages = new ArrayList<StatusMessage>(
                        status.getMessages());
                messages.addAll(s2.getMessages());
                result.put(
                        status.getName(), new Status(
                                status.getName(), messages));
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
