package filteringgasstations;

import filteringgasstations.database.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FuelPriceEvaluation implements CommandLineRunner {
    @Autowired
    private GermanPriceService germanPriceService;
    @Autowired
    private OSRMCacheService osrmCacheService;
    @Autowired
    private StationOfInterestService stationOfInterestService;
    @Autowired
    private CompetitorsService competitorsService;
    @Autowired
    private ForeignAveragePriceService foreignAveragePriceService;

    @Autowired
    private GermanAveragePriceService germanAveragePriceService;
    @Autowired
    private PriceDifferenceOfCompetitorsService priceDifferenceOfCompetitorsService;

    public static void main(String[] args) {
        SpringApplication.run(FuelPriceEvaluation.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        InitialDataCollection.main(osrmCacheService, germanPriceService, competitorsService, stationOfInterestService);
        AveragePricesComputation.main(germanAveragePriceService, foreignAveragePriceService, stationOfInterestService);
        PriceDifferenceComputation.main(competitorsService, germanAveragePriceService, foreignAveragePriceService, stationOfInterestService, priceDifferenceOfCompetitorsService);
        AggregateCompetition.main();
        System.exit(0);
    }
}
