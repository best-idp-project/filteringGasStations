package filteringgasstations;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FuelPriceEvaluation implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        InitialDataCollection.main(args);
        AveragePricesComputation.main(args);
        PriceDifferenceComputation.main(args);
        AggregateCompetition aggregateCompetition = new AggregateCompetition();
        aggregateCompetition.run();
        System.exit(0);
    }
}
