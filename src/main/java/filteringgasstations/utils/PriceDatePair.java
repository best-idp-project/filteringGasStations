package filteringgasstations.utils;

import java.util.Date;

/**
 * Object that contains a date and an average price of the day
 * Used in the end day procedure, after calculating the average this object is created and stored for a certain station
 */

public class PriceDatePair {
    public final Double avgPrice;
    public final Date date;

    public PriceDatePair(Double avgPrice, Date date) {
        this.avgPrice = avgPrice;
        this.date = date;
    }

    @Override
    public String toString() {
        return "PriceDatePair{" +
                "avgPrice=" + avgPrice +
                ", date=" + date +
                '}';
    }
}
