package filteringgasstations.database.models;

import java.util.Date;

/**
 * Class to represent the average price of a station for a specific date independent of the country.
 */
public abstract class AveragePrice {


    public abstract String getStation();

    public abstract Date getDate();

    public abstract double getAverage();
}
