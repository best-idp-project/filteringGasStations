package filteringgasstations.comparisons;

import filteringgasstations.database.models.AveragePrice;
import filteringgasstations.database.models.StationOfInterest;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the price information of a pair of competitors and calculates the price difference between per km of distance.
 */
public class PriceDifferencePerKmComparison extends CompetitorComparison {

    List<PriceDifference> differences = new ArrayList<>();

    public PriceDifferencePerKmComparison(StationOfInterest first, StationOfInterest second, double distance, List<AveragePrice> firstStationPrices, List<AveragePrice> secondStationPrices) {
        super(first, second, distance, firstStationPrices, secondStationPrices);
        if (firstStationPrices.size() != secondStationPrices.size() && firstStationPrices.size() != 184) {
            throw new IllegalArgumentException("The two lists of average prices must have the same size.");
        }
        for (int i = 0; i < firstStationPrices.size(); i++) {
            if (!firstStationPrices.get(i).getDate().equals(secondStationPrices.get(i).getDate())) {
                throw new IllegalArgumentException("The two lists of average prices must have the same dates.");
            }
            if (firstStationPrices.get(i).getAverage() == 0 || secondStationPrices.get(i).getAverage() == 0) {
                differences.add(new PriceDifference(null, firstStationPrices.get(i).getDate()));
            } else {
                double difference = firstStationPrices.get(i).getAverage() - secondStationPrices.get(i).getAverage();
                double km = distance / 1000;
                double differencePerKm = difference / km;
                differences.add(new PriceDifference(differencePerKm, firstStationPrices.get(i).getDate()));
            }
        }
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
        for (PriceDifference difference : differences) {
            builder.append(difference.getPriceDifference())
                    .append(",");
        }
        return builder.substring(0, builder.length() - 1);
    }

    public List<PriceDifference> getDifferences() {
        return differences;
    }

    /*public List<Comparison> toDatabaseComparison() {
        List<Comparison> comparisons = new ArrayList<>();
        for (int i = 0; i < getDifferences().size(); i++) {
            Date date = getFirstStationPrices().get(i).getDate();
            StationOfInterest first = getFirst();
            StationOfInterest second = getSecond();
            Double differencePerKM = getDifferences().get(i);
            Comparison comparison = new Comparison(
                    first.getId(),
                    first.getCountry(),
                    second.getId(),
                    second.getCountry(),
                    getDistance(),
                    null,
                    null,
                    differencePerKM,
                    date,
                    ComparisonType.PRICE_DIFFERENCE_PER_KM);
            comparisons.add(comparison);
        }
        return comparisons;
    }

     */
}
