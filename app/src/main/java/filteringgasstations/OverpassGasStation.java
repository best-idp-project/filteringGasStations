package filteringgasstations;

import filteringgasstations.GasStation;

import java.util.HashMap;

public class OverpassGasStation extends GasStation {

    public String type;
    HashMap<String, String> tags;

    @Override
    public String toString() {
        return id + "," + lat + "," + lon + ',' + tags;
    }

    public OverpassGasStation(long id, double latitude, double longitude) {
        super(id, latitude, longitude);
    }
}
