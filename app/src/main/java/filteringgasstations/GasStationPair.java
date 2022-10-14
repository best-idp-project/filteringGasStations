package filteringgasstations;

public class GasStationPair {
    String idFirstStation;
    String idSecondStation;
    Double airDistance;
    Double drivingDistance;
    Double drivingTime;

    public GasStationPair(String idFirstStation, String idSecondStation, Double airDistance) {
        this.idFirstStation = idFirstStation;
        this.idSecondStation = idSecondStation;
        this.airDistance = airDistance;
    }

    @Override
    public String toString() {
        return "GasStationPair{" +
                "idFirstStation='" + idFirstStation + '\'' +
                ", idSecondStation='" + idSecondStation + '\'' +
                ", airDistance=" + airDistance +
                ", drivingDistance=" + drivingDistance +
                ", drivingTime=" + drivingTime +
                '}';
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
