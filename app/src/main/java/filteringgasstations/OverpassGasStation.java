package filteringgasstations;

import java.util.HashMap;

public class OverpassGasStation extends GasStation {

    public String type;
    HashMap<String, String> tags;

    public void addImportantFields() {
        this.addr = new GasStationAddress(tags.get("addr:country"), tags.get("addr:city"), tags.get("addr::street"), tags.get("addr::housenumber"), tags.get("addr:postcode"), tags.get("name"));
    }

    public OverpassGasStation(String id, double latitude, double longitude, GasStationAddress addr) {
        super(id, latitude, longitude, addr);
    }
}
