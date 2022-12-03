package filteringgasstations;

import filteringgasstations.comparisons.AveragePriceComparison;
import filteringgasstations.comparisons.PriceDifference;
import filteringgasstations.comparisons.PriceDifferencePerKmComparison;
import filteringgasstations.database.models.*;
import filteringgasstations.database.service.*;
import filteringgasstations.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@SpringBootApplication
public class PriceDifferenceComputation implements CommandLineRunner {

    private static final List<String> highwayStations = List.of(new String[]{"c2e66726-f71c-491e-91b5-5390e76f374a",
            "df8e1459-867f-460a-bebb-9d7d044c2d58",
            "a13a2fb4-2eeb-47a9-8260-a54f03ff5a84",
            "44531d7a-4f95-49ed-8127-7e010faa126b",
            "f193221d-3d5f-4a52-a929-597780f8e043",
            "d916b480-038c-48ae-9177-baca1ce27213",
            "951425a6-476d-4b80-b2a5-fc689faf9793",
            "d2adf2c5-661d-4011-8378-81f77d2ed670",
            "8f2ff955-82b7-4327-b94f-964494dd8ea9",
            "86e7b9ca-31bb-4bda-af12-20f5384504dc",
            "c92d2337-0e2d-4c67-a793-7910c781482c",
            "7bf0b93a-647c-4175-8c05-cf6b6a612ea4",
            "72a94850-f9a9-4039-88cd-beb3bd3fad3d",
            "d54c4617-44a3-4db0-9b26-a8db3d6e108b",
            "e1a15081-2503-9107-e040-0b0a3dfe563c",
            "0627cc2b-d880-4c21-89bd-37e983c02624",
            "3579aceb-8134-49f6-ab8b-f437b9d6b1bf",
            "7b66bafe-a348-4e27-9e3b-137caf8fa197",
            "829ca312-aa11-4735-a39f-927661372050",
            "a00a98c1-6216-4ead-86ee-ae5df2035e1b",
            "05b7ea0f-825a-4aa6-a0a1-ff6ebcd579de",
            "09e2aaa0-893f-46b5-ada0-1237e4507c04",
            "c06208ef-8ce2-4d05-9dab-c93c0234d285",
            "4b1ef3ea-b716-46da-8727-04c21c2db248",
            "abfd8040-d939-45c8-8cc8-7a3f64d553b0",
            "c9ff5ad0-eab2-4444-b0a1-20e3a9536e29",
            "b3f3445a-f88d-5819-e100-00000630df04",
            "0947c43c-cbd6-4eca-9e6f-26d2bbc825ed",
            "805325e6-2418-406f-9732-bf7ce2777a60",
            "8e1e6928-5911-45f9-83b4-06a8b2be9d12",
            "21006633-2054-4a48-8e4e-93fdf8b6cb85",
            "f2ef6fe5-9589-4b4e-8792-71f8b62f27c7",
            "0662d86f-f85c-4936-9cc3-61753f512d25",
            "a753f452-b7e7-48a2-8f7c-caff93de81d1",
            "631df570-0876-4d52-82bc-1676d16fb25e"});
    @Autowired
    private CompetitorsService competitorsService;
    @Autowired
    private GermanAveragePriceService germanAveragePriceService;
    @Autowired
    private ForeignAveragePriceService foreignAveragePriceService;
    @Autowired
    private StationOfInterestService stationOfInterestService;
    @Autowired
    private PriceDifferenceOfCompetitorsService priceDifferenceOfCompetitorsService;

    public static void main(String[] args) {
        SpringApplication.run(PriceDifferenceComputation.class, args);
    }

    /**
     * Retrieve all station info from the database that are part of a competitor pair
     * Every pair has a mirrored pair so that we can retrieve all the competitors of one stations
     * regardless of the order we computed the distance in (A -> B or B -> A does not matter anymore)
     *
     * @return hashmap with ids and info for each station
     */
    private HashMap<String, StationOfInterest> getAllStations() {
        List<String> stationIds = competitorsService.getAllStations();
        List<StationOfInterest> stationsofinterests = stationOfInterestService.getAll();
        HashMap<String, StationOfInterest> stationsMap = new HashMap<>();
        stationsofinterests.stream().filter(stationOfInterest -> stationIds.contains(stationOfInterest.getId())).forEach(stationOfInterest -> stationsMap.put(stationOfInterest.getId(), stationOfInterest));
        return stationsMap;
    }

    /**
     * For each station get the avg price from the db and calculate competitor pairs
     *
     * @param allStations stations retrieved from the db
     * @return the panel set with pairs of competitors and avg prices per day
     */
    private List<AveragePriceComparison> calculateCompetitorPairsWithAveragePrices(HashMap<String, StationOfInterest> allStations) {
        List<AveragePriceComparison> averagePriceComparisons = new CopyOnWriteArrayList<>();
        //we need to keep track of all the average prices, be it within Germany or foreign
        ConcurrentHashMap<String, List<GermanAveragePrice>> germanPrices = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, List<ForeignAveragePrice>> foreignPrices = new ConcurrentHashMap<>();
        allStations.keySet().parallelStream().forEach(station -> {
            var entry = allStations.get(station);
            if (entry == null) {
                return;
            }
            if (entry.getCountry().equals("DE")) {
                germanPrices.put(station, germanAveragePriceService.getAllByStation(station));
            } else {
                foreignPrices.put(station, foreignAveragePriceService.getAllByStation(station));
            }
        });

        for (String station : allStations.keySet()) {
            List<String> competitors = competitorsService.getAllCompetitorsOfStation(station);
            StationOfInterest firstStation = allStations.get(station);
            List<AveragePrice> first = new ArrayList<>();
            if (germanPrices.containsKey(station)) {
                first.addAll(germanPrices.get(station));
            } else {
                first.addAll(foreignPrices.get(station));
            }
            if (first.isEmpty()) {
                //System.out.println(station + " from " + firstStation.getCountry() + " is empty");
                continue;
            }
            List<AveragePriceComparison> comparisons = competitors.stream().parallel().map(competitor -> {
                Competitors entry = competitorsService.findByFirstStationAndSecondStation(station, competitor);
                if (entry == null) {
                    entry = competitorsService.findByFirstStationAndSecondStation(competitor, station);
                }
                StationOfInterest secondStation = allStations.get(competitor);
                List<AveragePrice> second = new ArrayList<>();
                if (germanPrices.containsKey(competitor)) {
                    second.addAll(germanPrices.get(competitor));
                } else {
                    second.addAll(foreignPrices.get(competitor));
                }
                if (second.isEmpty()) {
                    //System.out.println(competitor + " from " + secondStation.getCountry() + " is empty");
                    return null;
                }
                return new AveragePriceComparison(firstStation, secondStation, entry.getDrivingDistance(), first, second);
            }).filter(Objects::nonNull).toList();

            averagePriceComparisons.addAll(comparisons);

        }
        //remove all the entries that do not have the same size
        averagePriceComparisons = averagePriceComparisons.stream().filter(p -> p.getFirstStationPrices().size() == p.getSecondStationPrices().size()).collect(Collectors.toList());

        return averagePriceComparisons;
    }

    @Override
    public void run(String... args) {
        // retrieve all competitors from the db
        HashMap<String, StationOfInterest> allStations = getAllStations();
        // calculate the competitor pairs and their average prices
        List<AveragePriceComparison> averagePriceComparisons = calculateCompetitorPairsWithAveragePrices(allStations);

        //print the intermediate result of a price comparison between competitors
        printNormalPanelSet(averagePriceComparisons);

        Set<String> ids = new HashSet<>(priceDifferenceOfCompetitorsService.getIds());

        //convert average price comparisons to price differences
        List<PriceDifferencePerKmComparison> priceDifferencePerKmList = averagePriceComparisons.stream()
                .map(p ->
                        new PriceDifferencePerKmComparison(
                                p.getFirst(),
                                p.getSecond(),
                                p.getDistance(),
                                p.getFirstStationPrices(),
                                p.getSecondStationPrices()))
                .filter(p -> p.getFirstStationPrices().size() == p.getSecondStationPrices().size())
                .filter(p -> p.getDistance() > 1)
                .filter(p -> !highwayStations.contains(p.getFirst().getId()))
                .filter(p -> !highwayStations.contains(p.getSecond().getId()))
                .filter(p -> p.getDifferences().get(0) != null)
                .filter(p -> p.getDistance() < 25000 /*only keep competitors within 25km*/)
                .toList();

        savePriceDifferencePerKmInDatabase(ids, priceDifferencePerKmList);
        printDifferencePanelSet(priceDifferencePerKmList);
    }

    private void savePriceDifferencePerKmInDatabase(Set<String> ids, List<PriceDifferencePerKmComparison> priceDifferencePerKmList) {
        priceDifferencePerKmList.parallelStream().forEach(priceDifferencePerKm -> {
            StationOfInterest first = priceDifferencePerKm.getFirst();
            StationOfInterest second = priceDifferencePerKm.getSecond();
            double distance = priceDifferencePerKm.getDistance();
            List<PriceDifference> differences = priceDifferencePerKm.getDifferences();
            var newDifferences = differences
                    .stream()
                    .map(difference -> new PriceDifferenceOfCompetitors(
                            first.getId(),
                            first.getCountry(),
                            second.getId(),
                            second.getCountry(),
                            distance,
                            difference.getPriceDifference(),
                            difference.getDate()))
                    .filter(dayDifference -> {
                        if (ids.contains(dayDifference.getId())) {
                            ids.remove(dayDifference.getId());
                            return false;
                        }
                        return true;
                    }).toList();
            priceDifferenceOfCompetitorsService.saveAll(newDifferences);
        });
    }

    /**
     * Write the panel set with avg of price per pair / per day to csv
     *
     * @param panelSets the avg price panel set
     */
    private void printNormalPanelSet(List<AveragePriceComparison> panelSets) {
        AveragePriceComparison first = panelSets.get(0);
        List<String> columns = new ArrayList<>();
        columns.add("station1");
        columns.add("country1");
        columns.add("station2");
        columns.add("country2");
        columns.add("distance");
        for (int i = 0; i < first.getFirstStationPrices().size(); i++) {
            String date = first.getFirstStationPrices().get(i).getDate().toString().split(" ")[0];
            columns.add("first_" + date);
            columns.add("second_" + date);
        }
        Utils.writeCSV("output/averagePriceComparison.csv", columns.toArray(new String[]{}), panelSets.stream().map(AveragePriceComparison::toString).collect(Collectors.toList()));
    }

    /**
     * Write the panel set with difference of price per pair / per day to csv
     *
     * @param panelSets the price difference panel set
     */
    private void printDifferencePanelSet(List<PriceDifferencePerKmComparison> panelSets) {
        PriceDifferencePerKmComparison first = panelSets.get(0);
        List<String> columns = new ArrayList<>();
        columns.add("station1");
        columns.add("country1");
        columns.add("station2");
        columns.add("country2");
        columns.add("distance");
        for (int i = 0; i < first.getFirstStationPrices().size(); i++) {
            String date = first.getFirstStationPrices().get(i).getDate().toString().split(" ")[0];
            columns.add(date);
        }
        Utils.writeCSV("output/difference.csv", columns.toArray(new String[]{}), panelSets.stream().map(PriceDifferencePerKmComparison::toString).collect(Collectors.toList()));
    }
}
