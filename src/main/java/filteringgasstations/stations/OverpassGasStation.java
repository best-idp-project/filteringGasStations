package filteringgasstations.stations;

import filteringgasstations.geolocation.CountryCode;

import java.util.HashMap;

public class OverpassGasStation extends GasStation {

    public String type;
    HashMap<String, String> tags;

    public OverpassGasStation(String id, double latitude, double longitude, GasStationAddress addr) {
        super(id, latitude, longitude, addr);
    }

    public void addImportantFields(CountryCode defaultCountry) {
        switch (tags.getOrDefault("addr:country", "")) {
            case "AT" -> defaultCountry = CountryCode.AUT;
            case "BE" -> defaultCountry = CountryCode.BEL;
            case "CZ" -> defaultCountry = CountryCode.CZE;
            case "DE" -> defaultCountry = CountryCode.GER;
            case "DK" -> defaultCountry = CountryCode.DNK;
            case "FR" -> defaultCountry = CountryCode.FRA;
            case "LU" -> defaultCountry = CountryCode.LUX;
            case "NL" -> defaultCountry = CountryCode.NLD;
            case "PL" -> defaultCountry = CountryCode.POL;
            default -> {
            }
        }
        this.address = new GasStationAddress(defaultCountry, tags.get("addr:city"), tags.get("addr::street"), tags.get("addr::housenumber"), tags.get("addr:postcode"), tags.get("name"));
    }
}
