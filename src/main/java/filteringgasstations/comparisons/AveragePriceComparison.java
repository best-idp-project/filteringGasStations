package filteringgasstations.comparisons;

import filteringgasstations.database.models.AveragePrice;
import filteringgasstations.database.models.StationOfInterest;

import java.util.List;

/**
 * Contains the average prices of a pair of competitors and the distance between them.
 */
public class AveragePriceComparison extends CompetitorComparison {

    public AveragePriceComparison(StationOfInterest first, StationOfInterest second, double distance, List<AveragePrice> firstStationPrices, List<AveragePrice> secondStationPrices) {
        super(first, second, distance, firstStationPrices, secondStationPrices);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getFirst().getId())
                .append(",")
                .append(getFirst().getCountry())
                .append(",")
                .append(getSecond().getId())
                .append(",")
                .append(getSecond().getCountry())
                .append(",")
                .append(getDistance())
                .append(",");
        for (int i = 0; i < getFirstStationPrices().size(); i++) {
            if (!getFirstStationPrices().get(i).getDate().equals(getSecondStationPrices().get(i).getDate())) {
                throw new IllegalArgumentException("The two lists of average prices must have the same dates.");
            }
            builder.append(getFirstStationPrices().get(i).getAverage())
                    .append(",")
                    .append(getSecondStationPrices().get(i).getAverage())
                    .append(",");
        }
        return builder.substring(0, builder.length() - 1);
    }
}
