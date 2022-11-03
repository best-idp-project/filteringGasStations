package filteringgasstations.database.models;

import com.google.gson.annotations.SerializedName;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "german_prices")
@Table(indexes = { @Index(name = "station_index", columnList = "station")})
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
