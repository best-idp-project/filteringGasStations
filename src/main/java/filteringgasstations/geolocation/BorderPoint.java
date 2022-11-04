package filteringgasstations.geolocation;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "borderpoints")
@Table
public class BorderPoint {

    private String id;
    private double latitude = 0.0;
    private double longitude = 0.0;

    public BorderPoint(double latitude, double longitude) {
        this.id = latitude + "_" + longitude;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public BorderPoint() {

    }
    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}

