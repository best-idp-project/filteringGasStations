package filteringgasstations.stations;

import filteringgasstations.App;
import filteringgasstations.database.models.Competitors;
import filteringgasstations.database.models.StationOfInterest;
import filteringgasstations.database.service.*;
import filteringgasstations.geolocation.BorderPoint;
import filteringgasstations.geolocation.CountryCode;
import filteringgasstations.routing.Route;
import filteringgasstations.routing.osrm.OSRMClient;
import filteringgasstations.utils.Utils;

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

    private CompetitorsService competitorsService;
    private HashMap<CountryCode, List<OverpassGasStation>> allStations;
    private List<GasStationPair> pairsInDrivableDistance = new ArrayList<>();

    public StationsFinder(OSRMCacheService osrmCacheService, InputFileService inputFileService, BorderPointService borderPointService, GermanPriceService germanPriceService, CompetitorsService competitorsService, double directDistanceLimit, double borderLimit) {
        this.osrmCacheService = osrmCacheService;
        this.inputFileService = inputFileService;
        this.germanPriceService = germanPriceService;
        this.DIRECT_DISTANCE_LIMIT = directDistanceLimit;
        this.competitorsService = competitorsService;
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
        System.out.println("There are " + pairsInDrivableDistance.size() + " pairs with air distance <= " + App.DIRECT_DISTANCE_LIMIT + " km");
    }

    public void writeStationsOfInterestToFile(StationOfInterestService stationOfInterestService) {
        String filename = "output/gasStationsRange".concat(String.valueOf((int) this.BORDER_LIMIT)).concat(".csv");
        Arrays.stream(CountryCode.values()).forEach(country -> {
                    List<OverpassGasStation> countryGasList = allStations.getOrDefault(country, new CopyOnWriteArrayList<>());
                    List<StationOfInterest> savedStations = stationOfInterestService.getAllByCountry(country);
                    AtomicInteger countryStations = new AtomicInteger(0);
                    countryGasList.stream().filter(station -> station.getAddress().getCountry() != null).forEach(station -> {
                        Optional<StationOfInterest> ofInterest = savedStations.stream().filter(stationOfInterest -> stationOfInterest.getId().equals(station.getId())).findFirst();
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
        var stations = stationsNearBorder.stream().sorted(Comparator.comparing(GasStation::getId)).toList();
        stationsNearBorder.clear();
        stationsNearBorder.addAll(stations);

        String[] columns = new String[]{
                "id",
                "lat",
                "lon",
                "country",
                "city",
                "street",
                "housenumber",
                "postcode",
                "name"
        };
        Utils.writeCSV(filename, columns, stationsNearBorder.stream().map(OverpassGasStation::toString).toList());
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
        String[] columns = new String[]{
                "idFirstStation",
                "idSecondStation",
                "countryCodeFirst",
                "countryCodeSecond",
                "airDistance",
                "drivingDistance",
                "drivingTime"
        };
        Set<String> ids = new HashSet<>(competitorsService.getIds());
        Set<String> finalIds = ids;
        List<Competitors> competitors = pairsInDrivableDistance
                .stream()
                .map(Competitors::fromGasStationPair).toList();
        competitors.stream().filter(p -> !finalIds.contains(p.getId())).parallel().forEach(competitorsService::save);
        ids = new HashSet<>(competitorsService.getIds());
        Utils.writeCSV("output/allPairsIn10Km.csv", columns, pairsInDrivableDistance.stream().map(GasStationPair::toString).sorted().toList());
    }
}
