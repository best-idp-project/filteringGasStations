package filteringgasstations;

public class GasStationPair {
    String idFirstStation;
    String idSecondStation;
    Double airDistance; // meters
    Double drivingDistance; // meters
    Double drivingTime; // seconds

    public GasStationPair(String idFirstStation, String idSecondStation, Double airDistance) {
        this.idFirstStation = idFirstStation;
        this.idSecondStation = idSecondStation;
        this.airDistance = airDistance;
    }

    @Override
    public String toString() {
        return "\"" + idFirstStation + "\"," +
                "\"" + idSecondStation + "\"," +
                airDistance + "," +
                drivingDistance + "," +
                drivingTime;
    }

    public Double getDrivingDistance() {
        return drivingDistance;
    }

    public void setDrivingDistance(Double drivingDistance) {
        this.drivingDistance = drivingDistance;
    }

    public Double getDrivingTime() {
        return drivingTime;
    }

    public void setDrivingTime(Double drivingTime) {
        this.drivingTime = drivingTime;
    }


}
