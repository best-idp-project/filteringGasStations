package filteringgasstations.geolocation;

public class BorderPoint {
    double latitude = 0.0;
    double longitude = 0.0;

    public BorderPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}

