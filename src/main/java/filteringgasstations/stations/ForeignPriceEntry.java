package filteringgasstations.stations;

import filteringgasstations.geolocation.CountryCode;

import java.sql.Date;

public class ForeignPriceEntry {
    public CountryCode countryCode;
    public Date date;
    public double price;

    public ForeignPriceEntry(CountryCode countryCode, Date date, double price) {
        this.countryCode = countryCode;
        this.date = date;
        this.price = price;
    }

    @Override
    public String toString() {
        return "ForeignPriceEntry{" +
                "countryCode=" + countryCode +
                ", date=" + date +
                ", price=" + price +
                '}';
    }
}
