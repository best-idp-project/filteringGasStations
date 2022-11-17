package filteringgasstations.stations;

import filteringgasstations.database.models.GermanPrice;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * #AndreasReview
 * The only usage of this class is inside a not used function ... why exists then?
 */
public class AveragePrices {

    private OverpassGasStation station;

    private List<GermanPrice> dataPoints;

    private Map<String, List<GermanPrice>> pointsByDay = new HashMap<>();

    public AveragePrices(OverpassGasStation station, List<GermanPrice> dataPoints) {
        this.station = station;
        this.dataPoints = dataPoints;
        this.dataPoints.forEach(data -> {
            String key = DateTime.parse(data.getDate().toString(), DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.S")).toString("yyyy-MM-dd");
            var list = pointsByDay.getOrDefault(key, new ArrayList<>());
            list.add(data);
            pointsByDay.put(key, list);
        });
    }

    private List<GermanPrice> expandData(List<GermanPrice> original) {
        var truncated = original.stream().map(germanPrice -> {
            LocalDateTime d = LocalDateTime.parse(germanPrice.getDate().toString());
            d.minusSeconds(d.getSecondOfMinute());
            germanPrice.setDate(d.toDate());
            return germanPrice;
        }).toList();
        return truncated;
    }

    public OptionalDouble getAverageOf(String date) {
        return pointsByDay.getOrDefault(date, new ArrayList<>()).stream().mapToDouble(GermanPrice::getE5).average();
    }
}
