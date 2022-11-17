package filteringgasstations.routing.osrm;

import com.google.gson.annotations.SerializedName;
import filteringgasstations.routing.Route;

/**
 * #AndreasReview
 */
public class OSRMRoute {

    private OSRMLeg[] legs;
    @SerializedName(value = "weightName", alternate = "weight_name")
    private String weightName;
    private double weight;
    private double duration;
    private double distance;

    public double getDuration() {
        return duration;
    }

    public double getDistance() {
        return distance;
    }
}
