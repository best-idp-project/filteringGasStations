package filteringgasstations.comparisons;


import filteringgasstations.database.models.AveragePrice;
import filteringgasstations.database.models.StationOfInterest;

import java.util.Comparator;
import java.util.List;

public abstract class CompetitorComparison {

    private StationOfInterest first;
    private StationOfInterest second;
    private double distance; //driving distance
    private List<AveragePrice> firstStationPrices;
    private List<AveragePrice> secondStationPrices;

    public CompetitorComparison(StationOfInterest first, StationOfInterest second, double distance, List<AveragePrice> firstStationPrices, List<AveragePrice> secondStationPrices) {
        this.first = first;
        this.second = second;
        this.distance = distance;
        firstStationPrices = firstStationPrices.stream().sorted(Comparator.comparing(AveragePrice::getDate)).toList();
        secondStationPrices = secondStationPrices.stream().sorted(Comparator.comparing(AveragePrice::getDate)).toList();
        this.firstStationPrices = firstStationPrices;
        this.secondStationPrices = secondStationPrices;
    }

    public StationOfInterest getFirst() {
        return first;
    }

    public void setFirst(StationOfInterest first) {
        this.first = first;
    }

    public StationOfInterest getSecond() {
        return second;
    }

    public void setSecond(StationOfInterest second) {
        this.second = second;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public List<AveragePrice> getFirstStationPrices() {
        return firstStationPrices;
    }

    public void setFirstStationPrices(List<AveragePrice> firstStationPrices) {
        this.firstStationPrices = firstStationPrices;
    }

    public List<AveragePrice> getSecondStationPrices() {
        return secondStationPrices;
    }

    public void setSecondStationPrices(List<AveragePrice> secondStationPrices) {
        this.secondStationPrices = secondStationPrices;
    }
}
