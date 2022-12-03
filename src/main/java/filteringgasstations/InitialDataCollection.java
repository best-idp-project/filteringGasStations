package filteringgasstations;

import filteringgasstations.database.service.CompetitorsService;
import filteringgasstations.database.service.GermanPriceService;
import filteringgasstations.database.service.OSRMCacheService;
import filteringgasstations.database.service.StationOfInterestService;
import filteringgasstations.stations.StationsFinder;

public class InitialDataCollection {

    public static final int RANGE_KM = 20; // select the range
    public static final double DIRECT_DISTANCE_LIMIT = 10.;

    public static void main(OSRMCacheService osrmCacheService, GermanPriceService germanPriceService, CompetitorsService competitorsService, StationOfInterestService stationOfInterestService) {

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
