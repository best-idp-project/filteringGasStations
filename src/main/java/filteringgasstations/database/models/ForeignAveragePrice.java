package filteringgasstations.database.models;

import com.google.gson.annotations.SerializedName;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * For easier access later, we store the foreign average prices in a separate table in the database.
 */
@Entity(name = "foreign_average_prices")
@Table(indexes = {@Index(name = "foreign_average_station_index", columnList = "station")})
public class ForeignAveragePrice extends AveragePrice {

    private String id;

    @SerializedName(value = "station", alternate = "station_uuid")
    private String station;
    private Date date;

    private double average;

    public ForeignAveragePrice() {
    }

    public ForeignAveragePrice(String station, Date date, double average) {
        this.station = station;
        this.date = date;
        this.average = average;
    }

    @Id
    public String getId() {
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
        String date = DATE_FORMAT.format(this.date);
        return date + "_" + station;
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

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }
}
