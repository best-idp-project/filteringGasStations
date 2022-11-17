package filteringgasstations.database.models;

import com.google.gson.annotations.SerializedName;
import org.joda.time.DateTime;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Date;

/**
 * Currently not really needed, but in order to be able to quickly gather all the prices that we have for one
 * station we stored them in a separate table.
 */
@Entity(name = "german_prices")
@Table(indexes = {@Index(name = "station_index", columnList = "station")})
public class GermanPrice {

    private String id;

    private Date date;

    private long timestamp;
    @SerializedName(value = "station", alternate = "station_uuid")
    private String station;
    private double e5;

    public GermanPrice() {
    }

    public GermanPrice(DateTime date, String station, double e5) {
        this.date = date.toDate();
        this.timestamp = date.getMillis();
        this.station = station;
        this.e5 = e5;
    }

    @Id
    public String getId() {
        return timestamp + "_" + station;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public double getE5() {
        return e5;
    }

    public void setE5(double e5) {
        this.e5 = e5;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
