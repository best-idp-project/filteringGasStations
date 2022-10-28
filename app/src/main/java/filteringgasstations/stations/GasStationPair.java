package filteringgasstations.stations;

import filteringgasstations.geolocation.CountryCode;

public class GasStationPair {
    OverpassGasStation firstStation;
    OverpassGasStation secondStation;
    CountryCode countryCodeFirst;
    CountryCode countryCodeSecond;
    Double airDistance; // meters
    Double drivingDistance; // meters
    Double drivingTime; // seconds

    public GasStationPair(OverpassGasStation firstStation, OverpassGasStation secondStation, CountryCode countryCodeFirst, CountryCode countryCodeSecond, Double airDistance) {
        this.firstStation = firstStation;
        this.secondStation = secondStation;
        this.countryCodeFirst = countryCodeFirst;
        this.countryCodeSecond = countryCodeSecond;
        this.airDistance = airDistance;
    }

    @Override
    public String toString() {
        return "\"" + firstStation.id + "\"," +
                "\"" + secondStation.id + "\"," +
                countryCodeFirst + "," + countryCodeSecond + "," +
                airDistance + "," +
                drivingDistance + "," +
                drivingTime;
    }

    public void setDrivingDistance(Double drivingDistance) {
        this.drivingDistance = drivingDistance;
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
