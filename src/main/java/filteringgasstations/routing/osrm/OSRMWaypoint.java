package filteringgasstations.routing.osrm;

/**
 * To cleanly parse the OSRM response we need a class which represents a waypoint. Not needed elsewhere
 */
public class OSRMWaypoint {

    private String hint;
    private double distance;
    private String name;
    private double[] location;
}
