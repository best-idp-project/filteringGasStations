package filteringgasstations.routing.osrm;

import filteringgasstations.routing.Route;

import java.util.Arrays;

public class OSRMResponse implements Route {

    private String code;
    private OSRMRoute[] routes;
    private OSRMWaypoint[] waypoints;

    @Override
    public double getDrivingTime() {
        return Arrays.stream(routes).findFirst().map(OSRMRoute::getDuration).orElse(Double.POSITIVE_INFINITY);
    }

    @Override
    public double getDrivingDistance() {
        return Arrays.stream(routes).findFirst().map(OSRMRoute::getDistance).orElse(Double.POSITIVE_INFINITY);
    }
}
