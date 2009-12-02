package dk.statsbiblioteket.doms.surveillance.surveyor;

/**
 * Created by IntelliJ IDEA.
 * User: kfc
 * Date: Dec 1, 2009
 * Time: 2:38:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class SurveyorFactory {
    private static Surveyor surveyor;

    public static Surveyor getSurveyor() {
        if (surveyor == null) {
            surveyor = new RestSurveyor();
        }
        return surveyor;
    }
}
