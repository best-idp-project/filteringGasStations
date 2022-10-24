package filteringgasstations;

public class GasStationAddress {
    String country;
    String city;
    String street;
    String housenumber;
    String postcode;
    String name;


    public GasStationAddress(String country, String city, String street, String housenumber, String postcode, String name) {
        this.country = country;
        this.city = city;
        this.street = street;
        this.housenumber = housenumber;
        this.postcode = postcode;
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
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
