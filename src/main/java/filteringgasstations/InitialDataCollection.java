package filteringgasstations;

import filteringgasstations.database.service.CompetitorsService;
import filteringgasstations.database.service.GermanPriceService;
import filteringgasstations.database.service.OSRMCacheService;
import filteringgasstations.database.service.StationOfInterestService;
import filteringgasstations.stations.StationsFinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InitialDataCollection implements CommandLineRunner {

    public static final int RANGE_KM = 20; // select the range
    public static final double DIRECT_DISTANCE_LIMIT = 10.;
    @Autowired
    private GermanPriceService germanPriceService;
    @Autowired
    private OSRMCacheService osrmCacheService;
    @Autowired
    private StationOfInterestService stationOfInterestService;
    @Autowired
    private CompetitorsService competitorsService;

    public static void main(String[] args) {
        SpringApplication.run(InitialDataCollection.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        StationsFinder finder = new StationsFinder(osrmCacheService, germanPriceService, competitorsService, DIRECT_DISTANCE_LIMIT, RANGE_KM);
        finder.readGermanBorder();
        finder.readAllStations();
        // For every country, check for every gas station the distance to all points of the german border
        System.out.println();
        System.out.println("Stations inside range of " + RANGE_KM + "km");
        finder.writeStationsOfInterestToFile(stationOfInterestService);

        System.out.println();
        System.out.println("Calculating distances between stations");
        // Calculate all possible pairs and for each calculate the driving time and distance
        finder.calculateDistancesBetweenStations();

        System.out.println("Write valid pairs");
        // Write all valid pairs to file
        finder.writePairsToFile();
    }
}
