package filteringgasstations.stations;

import filteringgasstations.geolocation.CountryCode;

public class GasStationAddress {
    CountryCode country;
    String city;
    String street;
    String housenumber;
    String postcode;
    String name;


    public GasStationAddress(CountryCode country, String city, String street, String housenumber, String postcode, String name) {
        this.country = country;
        this.city = city;
        this.street = street;
        this.housenumber = housenumber;
        this.postcode = postcode;
        this.name = name;
    }

    public CountryCode getCountry() {
        return country;
    }

    public void setCountry(CountryCode country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return
                country + ","
                        + city + "," +
                        street + "," +
                        housenumber + "," +
                        postcode + "," +
                        name;
    }
}
