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

    public StationOfInterest(String id, String country, double borderDistance) {
        this.id = id;
        this.country = country;
        this.borderDistance = borderDistance;
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
}
