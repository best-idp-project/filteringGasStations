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

    @Override
    public String toString() {
        return
                " country='" + country + '\'' +
                        ", city='" + city + '\'' +
                        ", street='" + street + '\'' +
                        ", housenumber='" + housenumber + '\'' +
                        ", postcode='" + postcode + '\'' +
                        ", name='" + name + '\'';
    }
}
