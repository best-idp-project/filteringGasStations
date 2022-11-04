package filteringgasstations.database.models;

import filteringgasstations.routing.Route;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "osrm_caches")
@Table
public class OSRMCache implements Route {
    private String id;
    private double drivingTime;
    private double drivingDistance;

    public OSRMCache(String id, double drivingTime, double drivingDistance) {
        this.id = id;
        this.drivingTime = drivingTime;
        this.drivingDistance = drivingDistance;
    }

    public OSRMCache() {
    }

    public void setId(String id) {
        this.id = id;
    }

    @Id
    public String getId() {
        return id;
    }

    public double getDrivingTime() {
        return drivingTime;
    }

    public void setDrivingTime(double drivingTime) {
        this.drivingTime = drivingTime;
    }

    public double getDrivingDistance() {
        return drivingDistance;
    }

    public void setDrivingDistance(double drivingDistance) {
        this.drivingDistance = drivingDistance;
    }
}
