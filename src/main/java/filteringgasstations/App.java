package filteringgasstations;

import filteringgasstations.database.service.*;
import filteringgasstations.stations.StationsFinder;
import filteringgasstations.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class App implements CommandLineRunner {

    @Autowired
    private GermanPriceService germanPriceService;
    @Autowired
    private OSRMCacheService osrmCacheService;

    @Autowired
    private InputFileService inputFileService;
    @Autowired
    private BorderPointService borderPointService;
    @Autowired
    private StationOfInterestService stationOfInterestService;
    public static final int RANGE_KM = 20; // select the range
    public static final double DIRECT_DISTANCE_LIMIT = 10.;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Utils.readGermanPrices(germanPriceService);
        StationsFinder finder = new StationsFinder(osrmCacheService, inputFileService, borderPointService, DIRECT_DISTANCE_LIMIT, RANGE_KM);
        // For every country, check for every gas station the distance to all points of the german border
        System.out.println();
        System.out.println("Stations inside range of " + RANGE_KM + "km");
        finder.writeStationsOfInterestToFile(stationOfInterestService);

        System.out.println();
        System.out.println("Calculating distances between stations");
        finder.calculateDistancesBetweenStations();

        System.out.println("Write valid pairs");
        finder.writeDrivablePairsToFile();
        System.out.println("bye");
        System.exit(0);
    }
}
