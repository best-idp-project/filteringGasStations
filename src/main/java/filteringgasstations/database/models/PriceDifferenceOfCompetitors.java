package filteringgasstations.database.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This file is only in this repository so that the database table gets created
 */
@Entity(name = "price_per_km_comparison")
public class PriceDifferenceOfCompetitors {

    private String id;
    private String firstStation;
    private String countryFirstStation;
    private String secondStation;
    private String countrySecondStation;

    private double distance;

    private Double pricePerKm;
    private Date date;

    public PriceDifferenceOfCompetitors(String firstStation, String countryFirstStation, String secondStation, String countrySecondStation, double distance, Double pricePerKm, Date date) {
        this.firstStation = firstStation;
        this.countryFirstStation = countryFirstStation;
        this.secondStation = secondStation;
        this.countrySecondStation = countrySecondStation;
        this.distance = distance;
        this.pricePerKm = pricePerKm;
        this.date = date;
    }

    public PriceDifferenceOfCompetitors() {

    }

    @Id
    public String getId() {
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
        String date = DATE_FORMAT.format(this.date);
        return date + "_" + firstStation + "_" + secondStation + "_";
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstStation() {
        return firstStation;
    }

    public void setFirstStation(String firstStation) {
        this.firstStation = firstStation;
    }

    public String getCountryFirstStation() {
        return countryFirstStation;
    }

    public void setCountryFirstStation(String countryFirstStation) {
        this.countryFirstStation = countryFirstStation;
    }

    public String getSecondStation() {
        return secondStation;
    }

    public void setSecondStation(String secondStation) {
        this.secondStation = secondStation;
    }

    public String getCountrySecondStation() {
        return countrySecondStation;
    }

    public void setCountrySecondStation(String countrySecondStation) {
        this.countrySecondStation = countrySecondStation;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public Double getPricePerKm() {
        return pricePerKm;
    }

    public void setPricePerKm(Double pricePerKm) {
        this.pricePerKm = pricePerKm;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
