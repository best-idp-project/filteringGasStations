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

        StationsFinder finder = new StationsFinder(osrmCacheService, competitorsService, DIRECT_DISTANCE_LIMIT, RANGE_KM);
        finder.readGermanBorder();
        finder.readAllStations();
        // For every country, check for every gas station the distance to all points of the german border
        System.out.println();
        System.out.println("STATIONS IN A RANGE OF " + RANGE_KM + "KM FROM THE BORDER:");
        finder.writeStationsOfInterestToFile(stationOfInterestService);

        System.out.println();
        System.out.println("CALCULATING DISTANCES BETWEEN STATIONS:");
        // Calculate all possible pairs and for each calculate the driving time and distance
        finder.calculateDistancesBetweenStations();

        System.out.println();
        System.out.println("Writing valid pairs to file ... ");
        // Write all valid pairs to file
        finder.writePairsToFile();
    }
}
