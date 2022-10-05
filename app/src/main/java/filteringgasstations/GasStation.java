package filteringgasstations;

public class GasStation {
    final long id;
    final double lat;
    final double lon;

    public GasStation(long id, double latitude, double longitude) {
        this.id = id;
        lat = latitude;
        lon = longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        try {
            GasStation that = (GasStation) o;
            if (Double.compare(that.lat, lat) != 0) return false;
            return Double.compare(that.lon, lon) == 0;
        } catch (Exception ignored) {
        }

        return false;
    }

    @Override
    public String toString() {
        return id + "," + lat + "," + lon;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(lat);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(lon);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
