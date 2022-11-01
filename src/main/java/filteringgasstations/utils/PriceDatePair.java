package filteringgasstations.utils;

import java.util.Date;

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
