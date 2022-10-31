package filteringgasstations.stations;

import com.google.gson.annotations.SerializedName;

public class GasStation {
    public final String id;
    @SerializedName(value = "latitude", alternate = "lat")
    public final double latitude;
    @SerializedName(value = "longitude", alternate = "lon")
    public final double longitude;
    GasStationAddress address;

    public GasStation(String id, double latitude, double longitude, GasStationAddress address) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public GasStationAddress getAddress() {
        return address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        try {
            GasStation that = (GasStation) o;
            if (Double.compare(that.latitude, latitude) != 0) return false;
            return Double.compare(that.longitude, longitude) == 0;
        } catch (Exception ignored) {
        }

        return false;
    }

    @Override
    public String toString() {
        return id +
                "," + latitude +
                "," + longitude +
                "," + address;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(latitude);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
