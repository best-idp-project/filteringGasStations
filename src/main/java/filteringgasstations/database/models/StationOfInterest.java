package filteringgasstations.database.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * We store the stations with their country in the database with their distance to the closest border point to quickly
 * get all the stations in a country in a certain radius.
 */
@Entity(name = "stations_of_interest")
@Table
public class StationOfInterest {

    private String id;
    private String country;
    private double borderDistance;
    private Double latitude;
    private Double longitude;

    public StationOfInterest(String id, String country, double borderDistance, Double latitude, Double longitude) {
        this.id = id;
        this.country = country;
        this.borderDistance = borderDistance;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public StationOfInterest() {

    }

    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getBorderDistance() {
        return borderDistance;
    }

    public void setBorderDistance(double borderDistance) {
        this.borderDistance = borderDistance;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
