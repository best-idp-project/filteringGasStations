package filteringgasstations.comparisons;

import java.util.Date;

public class PriceDifference {

    private Double priceDifference;
    private Date date;

    public PriceDifference(Double priceDifference, Date date) {
        this.priceDifference = priceDifference;
        this.date = date;
    }

    public Double getPriceDifference() {
        return priceDifference;
    }

    public Date getDate() {
        return date;
    }
}
