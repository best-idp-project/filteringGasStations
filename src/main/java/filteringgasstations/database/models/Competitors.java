package filteringgasstations.database.models;

import filteringgasstations.stations.GasStationPair;
import filteringgasstations.stations.OverpassGasStation;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * #AndreasReview
 */
@Entity(name = "competitors")
@Table(indexes = {@Index(name = "first_station_index", columnList = "firstStation"), @Index(name = "second_station_index", columnList = "secondStation")})
public class Competitors {

    private String id;
    private String firstStation;
    private String secondStation;
    private String countryFirst;
    private String countrySecond;
    double airDistance; // meters
    double drivingDistance; // meters
    double drivingTime; // seconds

    public Competitors() {
    }

    public static Competitors fromGasStationPair(GasStationPair gasStationPair) {
        Competitors competitors = new Competitors();
        OverpassGasStation firstStation = gasStationPair.getFirstStation();
        OverpassGasStation secondStation = gasStationPair.getSecondStation();
        competitors.firstStation = firstStation.getId();
        competitors.secondStation = secondStation.getId();
        competitors.countryFirst = firstStation.getAddress().getCountry().getCode();
        competitors.countrySecond = secondStation.getAddress().getCountry().getCode();
        competitors.airDistance = gasStationPair.getAirDistance();
        competitors.drivingDistance = gasStationPair.getDrivingDistance();
        competitors.drivingTime = gasStationPair.getDrivingTime();
        return competitors;
    }

    public String getFirstStation() {
        return firstStation;
    }

    public String getSecondStation() {
        return secondStation;
    }

    public String getCountryFirst() {
        return countryFirst;
    }

    public String getCountrySecond() {
        return countrySecond;
    }

    public double getAirDistance() {
        return airDistance;
    }

    public double getDrivingDistance() {
        return drivingDistance;
    }

    public double getDrivingTime() {
        return drivingTime;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Id
    public String getId() {
        return firstStation + "_" + secondStation;
    }

    public void setFirstStation(String firstStation) {
        this.firstStation = firstStation;
    }

    public void setSecondStation(String secondStation) {
        this.secondStation = secondStation;
    }

    public void setCountryFirst(String countryFirst) {
        this.countryFirst = countryFirst;
    }

    public void setCountrySecond(String countrySecond) {
        this.countrySecond = countrySecond;
    }

    public void setAirDistance(double airDistance) {
        this.airDistance = airDistance;
    }

    public void setDrivingDistance(double drivingDistance) {
        this.drivingDistance = drivingDistance;
    }

    public void setDrivingTime(double drivingTime) {
        this.drivingTime = drivingTime;
    }
}
