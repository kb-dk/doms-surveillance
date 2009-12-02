package dk.statsbiblioteket.doms.surveillance.surveyor;

import dk.statsbiblioteket.doms.surveillance.status.Status;

import java.util.List;
import java.util.Map;

/** Interface for getting status messages. */
public interface Surveyor {
    /**
     * Get the current status.
     *
     * @return Current status.
     */
    Map<String, Status> getStatusMap();

    /**
     * Set list of URLs to get status from.
     *
     * @param restStatusUrls List of URLs to get status from.
     */
    void setRestStatusUrls(List<String> restStatusUrls);
}
