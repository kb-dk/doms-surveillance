package dk.statsbiblioteket.doms.surveillance.surveyor;

import dk.statsbiblioteket.doms.surveillance.status.Status;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: kfc
 * Date: Dec 1, 2009
 * Time: 2:42:31 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Surveyor {
    Map<String, Status> getStatusMap();
}
