package filteringgasstations.stations;

import filteringgasstations.App;
import filteringgasstations.database.models.GermanPrice;
import filteringgasstations.database.models.StationOfInterest;
import filteringgasstations.database.service.*;
import filteringgasstations.geolocation.BorderPoint;
import filteringgasstations.geolocation.CountryCode;
import filteringgasstations.routing.Route;
import filteringgasstations.routing.osrm.OSRMClient;
import filteringgasstations.utils.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static filteringgasstations.utils.Utils.distance;


public class StationsFinder {
    private final CopyOnWriteArrayList<OverpassGasStation> stationsNearBorder = new CopyOnWriteArrayList<>();
    private final double DIRECT_DISTANCE_LIMIT;
    private final double BORDER_LIMIT;
    private final List<BorderPoint> germanBorder;
    private OSRMCacheService osrmCacheService;
    private InputFileService inputFileService;
    private GermanPriceService germanPriceService;

    private BorderPointService borderPointService;
    private HashMap<CountryCode, List<OverpassGasStation>> allStations;
    private List<GasStationPair> pairsInDrivableDistance = new ArrayList<>();

    public StationsFinder(OSRMCacheService osrmCacheService, InputFileService inputFileService, BorderPointService borderPointService, GermanPriceService germanPriceService, double directDistanceLimit, double borderLimit) {
        this.osrmCacheService = osrmCacheService;
        this.inputFileService = inputFileService;
        this.germanPriceService = germanPriceService;
        this.DIRECT_DISTANCE_LIMIT = directDistanceLimit;
        this.BORDER_LIMIT = borderLimit;

        boolean hasChanged = Utils.hasBorderChanged(inputFileService);
        germanBorder = Utils.readGermanBorder(inputFileService, borderPointService);
        allStations = Utils.readGasStationsForEachCountry(inputFileService);
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
                pairs.add(new GasStationPair(from, to, from.getAddress().getCountry(), to.getAddress().getCountry(), directDistance));
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
                Route distances = OSRMClient.getRoute(osrmCacheService, pair);
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

    public void writeStationsOfInterestToFile(StationOfInterestService stationOfInterestService) {
        File output = new File("output");
        if (!output.exists()) {
            var _bool = output.mkdir();
        }
        String filename = "output/gasStationsRange".concat(String.valueOf((int) this.BORDER_LIMIT)).concat(".csv");
        Arrays.stream(CountryCode.values()).forEach(country -> {
                    List<OverpassGasStation> countryGasList = allStations.getOrDefault(country, new ArrayList<>());
                    AtomicInteger countryStations = new AtomicInteger(0);
                    countryGasList.forEach(station -> {
                        assert station.getAddress().getCountry() != null;
                        Optional<StationOfInterest> ofInterest = stationOfInterestService.get(station.id);
                        if (ofInterest.isPresent()) {
                            StationOfInterest stationOfInterest = ofInterest.get();
                            if (stationOfInterest.getBorderDistance() < this.BORDER_LIMIT) {
                                stationsNearBorder.add(station);
                                countryStations.incrementAndGet();
                            }
                        } else {
                            Optional<Double> match = this.germanBorder.stream()
                                    .map(point -> distance(point.getLatitude(), station.getLatitude(), point.getLongitude(), station.getLongitude()))
                                    .sorted().findFirst();
                            if (match.isPresent()) {
                                StationOfInterest nearest = new StationOfInterest(station.id, country.getCode(), match.get());
                                stationOfInterestService.save(nearest);
                                if (nearest.getBorderDistance() < this.BORDER_LIMIT) {
                                    stationsNearBorder.add(station);
                                    countryStations.incrementAndGet();
                                }
                            }
                        }
                    });
                    System.out.println(country.getName() + ": " + countryStations.get() + " gas stations inside " + this.BORDER_LIMIT + "km");
                }
        );
        try {
            output = new File(filename);
            FileWriter writer = new FileWriter(output);
            writer.append("id,lat,lon,country,city,street,housenumber,postcode,name\n");
            for (OverpassGasStation station : stationsNearBorder) {
                writer.append(station.toString()).append('\n');
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<AveragePrices> getPriceDataForGermanStations() {
        List<AveragePrices> priceData = new ArrayList<>();
        for (OverpassGasStation station : stationsNearBorder) {
            if (station.getAddress().getCountry().equals(CountryCode.GER)) {
                var data = germanPriceService.getAllByStation(station.id);
                priceData.add(new AveragePrices(station, data));
            }
        }
        return priceData;
    }

    public void writeDrivablePairsToFile() {

        File filecsv = new File("output");
        if (!filecsv.exists()) {
            var _bool = filecsv.mkdir();
        }

        try {
            filecsv = new File("output/AllPairsIn10Km.csv");
            FileWriter fwcsv = new FileWriter(filecsv);
            fwcsv.append("idFirstStation,idSecondStation,countryCodeFirst,countryCodeSecond," +
                    "airDistance,drivingDistance,drivingTime\n");

            for (GasStationPair pair : pairsInDrivableDistance) {
                fwcsv.append(pair.toString()).append("\n");
            }
            fwcsv.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
