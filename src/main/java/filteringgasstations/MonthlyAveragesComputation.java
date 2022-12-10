package filteringgasstations;


import filteringgasstations.database.models.StationOfInterest;
import filteringgasstations.database.service.CompetitorsService;
import filteringgasstations.database.service.ForeignAveragePriceService;
import filteringgasstations.database.service.GermanAveragePriceService;
import filteringgasstations.database.service.StationOfInterestService;
import filteringgasstations.geolocation.CountryCode;
import filteringgasstations.utils.Utils;

import java.util.*;

public class MonthlyAveragesComputation {

    private static GermanAveragePriceService germanAveragePriceService;

    private static ForeignAveragePriceService foreignAveragePriceService;

    private static CompetitorsService competitorsService;

    private static StationOfInterestService stationOfInterestService;

    public static void main(GermanAveragePriceService germanAveragePriceService, ForeignAveragePriceService foreignAveragePriceService, StationOfInterestService stationOfInterestService, CompetitorsService competitorsService) throws Exception {
        MonthlyAveragesComputation.germanAveragePriceService = germanAveragePriceService;
        MonthlyAveragesComputation.foreignAveragePriceService = foreignAveragePriceService;
        MonthlyAveragesComputation.stationOfInterestService = stationOfInterestService;
        MonthlyAveragesComputation.competitorsService = competitorsService;
        System.out.println("Computing the border average ...");
        calculateBorderAverage();
    }

    /**
     * Compute avg price per month considering all german stations near each foreign country border
     */
    public static void calculateBorderAverage() {
        List<String> lines = new ArrayList<>();
        for (CountryCode countryCode : Arrays.stream(CountryCode.values()).filter(f -> f != CountryCode.GER).toArray(CountryCode[]::new)) {
            System.out.println(countryCode + ": ");
            List<String> germanCompetitors = competitorsService.getGermanCompetitorsOfCountry(countryCode);
            for (int i = 4; i <= 10; i++) {
                List<Double> averagePrices = new ArrayList<>();
                String month = i < 10 ? "0" + i : "" + i;
                for (String id : germanCompetitors) {
                    Double price = germanAveragePriceService.getAveragePriceByStationAndMonth(id, month);
                    if (price != null) {
                        averagePrices.add(price);
                    }

                }
                double averagePrice = averagePrices.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                System.out.println("Border DE-" + countryCode + ": " + germanCompetitors.size() + " month: " + month + " average: " + averagePrice);
                lines.add("DE-" + countryCode + ',' + germanCompetitors.size() + ',' + month + ',' + averagePrice);
            }
        }
        Utils.writeCSV("output/border_averages.csv", new String[]{"border", "number_of_stations", "month", "average"}, lines);
    }
}