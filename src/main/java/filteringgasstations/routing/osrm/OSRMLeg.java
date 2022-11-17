package filteringgasstations.routing.osrm;

/**
 * To cleanly parse the OSRM response we need a class which represents a leg, not sure what it represents. Not needed elsewhere
 */
public class OSRMLeg {
    private Object[] steps;
    private String summary;
    private double weight;
    private double duration;
    private double distance;
}
