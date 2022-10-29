package filteringgasstations;

import filteringgasstations.stations.StationsFinder;


public class App {
    public static final int RANGE_KM = 20; // select the range
    public static final double DIRECT_DISTANCE_LIMIT = 10.;


    public static void main(String[] args) {

        StationsFinder finder = new StationsFinder(DIRECT_DISTANCE_LIMIT, RANGE_KM);
        // For every country, check for every gas station the distance to all points of the german border
        System.out.println();
        System.out.println("Stations inside range of " + RANGE_KM + "km");
        finder.writeStationsOfInterestToFile();

        System.out.println();
        System.out.println("Calculating distances between stations");
        finder.calculateDistancesBetweenStations();

        System.out.println("Write valid pairs");
        finder.writeDrivablePairsToFile();
    }
}
