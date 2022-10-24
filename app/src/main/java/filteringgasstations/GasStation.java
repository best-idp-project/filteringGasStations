package filteringgasstations;

public class GasStation {
    final String id;
    final double lat;
    final double lon;
    GasStationAddress addr;

    public GasStation(String id, double latitude, double longitude, GasStationAddress address) {
        this.id = id;
        lat = latitude;
        lon = longitude;
        addr = address;
    }

    public GasStationAddress getAddr() {
        return addr;
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
        return id +
                "," + lat +
                "," + lon +
                "," + addr;
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
