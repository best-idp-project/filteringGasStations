package filteringgasstations.stations;

import filteringgasstations.utils.PriceDatePair;

import java.sql.Time;
import java.util.ArrayList;

/**
 * German station with id, all (date, avgprice) pairs, lastprice (for the avg calculation) and all new price entries
 * stored as timestamps/prices.
 */
public class GermanStation {
    public final String id;
    public ArrayList<PriceDatePair> avgPrices = new ArrayList<>();
    public double lastPrice;
    public ArrayList<Time> timestamps = new ArrayList<>();
    public ArrayList<Double> prices = new ArrayList<>();

    public GermanStation(String id) {
        this.id = id;
        this.lastPrice = 0;
    }

    public void setLastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
    }

    public void addTimeAndPrice(Time time, Double price) {
        this.timestamps.add(time);
        this.prices.add(price);
    }

    @Override
    public String toString() {
        return "GermanStation{" +
                "id='" + id + '\'' +
                ", lastPrice=" + lastPrice +
                ", timestamps=" + timestamps +
                ", prices=" + prices +
                '}';
    }
}
