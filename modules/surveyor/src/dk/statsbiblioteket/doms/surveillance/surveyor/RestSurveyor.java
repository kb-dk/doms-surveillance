package dk.statsbiblioteket.doms.surveillance.surveyor;

import com.sun.jersey.api.client.Client;
import dk.statsbiblioteket.doms.surveillance.status.Status;
import dk.statsbiblioteket.doms.surveillance.status.StatusMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: kfc
 * Date: Dec 1, 2009
 * Time: 2:42:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class RestSurveyor implements Surveyor {
    public Map<String, Status> getStatusMap() {
        Map<String, Status> result = new HashMap<String, Status>();

        Client c = Client.create();
        List<String> statusUrls = getStatusUrls();
        for (String statusUrl : statusUrls) {
            Status s = c.resource(statusUrl).get(Status.class);
            Status s2 = result.get(s.getName());
            if (s2 == null) {
                result.put(s.getName(), s);
            } else {
                List<StatusMessage> messages = new ArrayList<StatusMessage>(
                        s.getMessages());
                messages.addAll(s2.getMessages());
                result.put(s.getName(), new Status(s.getName(), messages));
            }
        }
        return result;
    }

    /** @return  */
    public List<String> getStatusUrls() {
        //TODO: Get URLs from configuration
        ArrayList<String> strings = new ArrayList<String>();
        strings.add(
                "http://localhost:8080/doms-surveillance-surveyor-0.0.1/logsurveyservice/getStatus");
        return strings;
    }
}
