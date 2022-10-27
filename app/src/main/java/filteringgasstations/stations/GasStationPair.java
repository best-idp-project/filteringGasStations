package filteringgasstations.stations;

public class GasStationPair {
    OverpassGasStation firstStation;
    OverpassGasStation secondStation;
    Double airDistance; // meters
    Double drivingDistance; // meters
    Double drivingTime; // seconds

    public GasStationPair(OverpassGasStation firstStation, OverpassGasStation secondStation, Double airDistance) {
        this.firstStation = firstStation;
        this.secondStation = secondStation;
        this.airDistance = airDistance;
    }

    @Override
    public String toString() {
        return "\"" + firstStation.id + "\"," +
                "\"" + secondStation.id + "\"," +
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

    public OverpassGasStation getFirstStation() {
        return firstStation;
    }

    public OverpassGasStation getSecondStation() {
        return secondStation;
    }

    public Double getAirDistance() {
        return airDistance;
    }
}
