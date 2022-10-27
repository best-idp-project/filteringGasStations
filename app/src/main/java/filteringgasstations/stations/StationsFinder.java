package filteringgasstations.stations;

import filteringgasstations.App;
import filteringgasstations.geolocation.BorderPoint;
import filteringgasstations.geolocation.CountryCode;
import filteringgasstations.routing.Route;
import filteringgasstations.routing.osrm.OSRMClient;
import filteringgasstations.utils.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static filteringgasstations.utils.Utils.distance;

public class StationsFinder {

    private HashMap<CountryCode, List<OverpassGasStation>> allStations = new HashMap<>();
    private final List<OverpassGasStation> stationsNearBorder = new ArrayList<>();
    private List<GasStationPair> pairsInDrivableDistance = new ArrayList<>();

    private final double DIRECT_DISTANCE_LIMIT;
    private final double BORDER_LIMIT;
    private final List<BorderPoint> germanBorder;

    public StationsFinder(double directDistanceLimit, double borderLimit) {
        this.DIRECT_DISTANCE_LIMIT = directDistanceLimit;
        this.BORDER_LIMIT = borderLimit;

        germanBorder = Utils.readGermanBorder();
        allStations = Utils.readGasStationsForEachCountry();
        allStations.put(CountryCode.GER, Utils.readGermanStations());
    }

    private static List<GasStationPair> getPairsOfInterest(List<OverpassGasStation> stations, double distanceLimit) {
        List<GasStationPair> pairs = new ArrayList<>();
        for (int fromIndex = 0; fromIndex < stations.size(); fromIndex++) {
            for (int toIndex = fromIndex + 1; toIndex < stations.size(); toIndex++) {
                OverpassGasStation from = stations.get(fromIndex);
                OverpassGasStation to = stations.get(toIndex);
                if (!from.getAddress().getCountry().equals(CountryCode.GER) && !to.getAddress().getCountry().equals(CountryCode.GER)) {
                    continue;
                }
                double directDistance = distance(from.getLatitude(), to.getLatitude(), from.getLongitude(), to.getLongitude());
                if (directDistance > distanceLimit) {
                    continue;
                }
                pairs.add(new GasStationPair(from, to, directDistance));
            }
        }
        return pairs;
    }


    public void calculateDistancesBetweenStations() {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
        var pairs = getPairsOfInterest(stationsNearBorder, this.DIRECT_DISTANCE_LIMIT);
        AtomicInteger counter = new AtomicInteger(0);
        List<CompletableFuture<GasStationPair>> futures = new ArrayList<>();
        pairs.forEach(pair -> {
            CompletableFuture<GasStationPair> job = CompletableFuture.supplyAsync(() -> {
                Route distances = OSRMClient.getRoute(pair);
                if (distances != null) {
                    pair.setDrivingDistance(distances.getDrivingDistance());
                    pair.setDrivingTime(distances.getDrivingTime());
                    System.out.println(Thread.currentThread().getName() + ": http req #: " + counter.incrementAndGet() + "/" + pairs.size());
                }
                return pair;
            }, executorService);
            futures.add(job);
        });
        pairsInDrivableDistance = futures.stream().parallel().map(job -> {
            try {
                return job.get();
            } catch (InterruptedException | ExecutionException e) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
        System.out.println("There are " + pairsInDrivableDistance.size() + " pairs with air distance <=" + App.DIRECT_DISTANCE_LIMIT + " km");
    }

    public void writeStationsOfInterestToFile() {
        boolean found = false;
        int counterFound = 0;

        File output = new File("output");
        if (!output.exists()) {
            var _bool = output.mkdir();
        }
        String filename = "output/gasStationsRange".concat(String.valueOf(this.BORDER_LIMIT)).concat(".csv");
        try {
            output = new File(filename);
            FileWriter writer = new FileWriter(output);
            writer.append("id,lat,lon,country,city,street,housenumber,postcode,name\n");

            for (CountryCode country : CountryCode.values()) {
                List<OverpassGasStation> countryGasList = allStations.getOrDefault(country, new ArrayList<>());
                for (OverpassGasStation station : countryGasList) {
                    assert station.getAddress().getCountry() != null;
                    for (BorderPoint point : this.germanBorder) {
                        if ((distance(point.getLatitude(), station.getLatitude(), point.getLongitude(), station.getLongitude()) < this.BORDER_LIMIT) && (!found)) {
                            writer.append(station.toString()).append("\n");
                            stationsNearBorder.add(station);
                            counterFound++;
                            found = true;
                        }
                    }
                    found = false;
                }
                System.out.println(country.getName() + ": " + counterFound + " gas stations inside " + this.BORDER_LIMIT + "km");
                counterFound = 0;
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeDrivablePairsToFile() {

        File filecsv = new File("output");
        if (!filecsv.exists()) {
            var _bool = filecsv.mkdir();
        }

        try {
            filecsv = new File("output/allPairsIn10Km.csv");
            FileWriter fwcsv = new FileWriter(filecsv);
            fwcsv.append("idFirstStation,idSecondStation,airDistance,drivingDistance,drivingTime\n");

            for (GasStationPair pair : pairsInDrivableDistance) {
                fwcsv.append(pair.toString()).append("\n");
            }
            fwcsv.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
