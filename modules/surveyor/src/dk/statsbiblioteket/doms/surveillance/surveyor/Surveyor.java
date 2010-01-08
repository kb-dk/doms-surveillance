package dk.statsbiblioteket.doms.surveillance.surveyor;

import java.io.File;
import java.util.List;
import java.util.Map;

/** Interface for getting status messages. */
public interface Surveyor {
    /**
     * Get the current status.
     *
     * @return Current status.
     */
    Map<String, CondensedStatus> getStatusMap();

    /**
     * Set configuration of surveyor
     *
     * @param restStatusUrls      List of URLs to get status from.
     * @param ignoredMessagesFile File to store list of ignored messages in.
     */
    void setConfiguration(List<String> restStatusUrls,
                          File ignoredMessagesFile);

    /**
     * Mark a message as handled, thus removing it from the list of currently
     * unhandled log messages.
     *
     * @param applicationName Name of application with message.
     * @param message         The message to mark as handled.
     */
    void markHandled(String applicationName, String message);

    /**
     * Mark a message as one that should never be shwon again.
     *
     * @param applicationName Name of application with message.
     * @param message         The message never to show again.
     */
    void notAgain(String applicationName, String message);
}
