package filteringgasstations.routing;

/**
 * A single route cached in the db
 */
public class CachedRoute implements Route {

    double drivingTime;
    double drivingDistance;

    public CachedRoute(double drivingTime, double drivingDistance) {
        this.drivingTime = drivingTime;
        this.drivingDistance = drivingDistance;
    }

    @Override
    public double getDrivingTime() {
        return drivingTime;
    }

    @Override
    public double getDrivingDistance() {
        return drivingDistance;
    }
}
