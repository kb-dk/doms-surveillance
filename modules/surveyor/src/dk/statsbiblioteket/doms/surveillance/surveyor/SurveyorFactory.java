package dk.statsbiblioteket.doms.surveillance.surveyor;

/** Get a surveyor. */
public class SurveyorFactory {
    private static Surveyor surveyor;

    public static Surveyor getSurveyor() {
        if (surveyor == null) {
            //TODO: Make implementation configurable
            surveyor = new RestSurveyor();
        }
        return surveyor;
    }
}
