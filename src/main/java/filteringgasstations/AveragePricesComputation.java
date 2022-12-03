package filteringgasstations;


import filteringgasstations.database.models.ForeignAveragePrice;
import filteringgasstations.database.models.GermanAveragePrice;
import filteringgasstations.database.service.ForeignAveragePriceService;
import filteringgasstations.database.service.GermanAveragePriceService;
import filteringgasstations.database.service.StationOfInterestService;
import filteringgasstations.geolocation.CountryCode;
import filteringgasstations.stations.ForeignPriceEntry;
import filteringgasstations.stations.GermanStation;
import filteringgasstations.utils.PriceDatePair;
import filteringgasstations.utils.Utils;
import org.apache.commons.lang3.time.DateUtils;

import java.sql.Date;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class AveragePricesComputation {

    private static final int ONE_WEEK = 604800000; // how many ms in one week
    private static final int ONE_DAY_6_22 = 57600; // how many sec in a day of anaysis
    static List<GermanStation> germanStations;

    private static GermanAveragePriceService germanAveragePriceService;

    private static ForeignAveragePriceService foreignAveragePriceService;

    private static StationOfInterestService stationOfInterestService;

    /**
     * Read all german prices entry of the 15 days before the start of the analysis to be sure of having a starting price
     * the first day (and not a nil).
     */
    public static void readPricesSomeDaysBeforeStart() {
        for (int day = 1; day < 15; day++) {
            String fileName = "prices/germany/extra/2022-04-" + String.format("%02d", day) + "-prices.csv";
            var file = ClassLoader.getSystemClassLoader().getResource(fileName);
            Utils.readCSV(file).forEach(line -> {
                for (GermanStation i : germanStations) {
                    if (i.id.equals(line[1])) {
                        i.setLastPrice(Double.parseDouble(line[2]));
                        break;
                    }
                }
            });
        }
        System.out.println();
        System.out.println("Reading prices of the 15 days before start - DONE");
    }

    /**
     * Read all GERMAN price entries for all the days of the analysis (between April 15 and October 15)
     * and store for every station the corresponding entries.
     * At the end of every day call the endDayProcedure to calculate the avg price
     */
    public static void readAllDaysPrices() {
        for (int month = 4; month <= 10; month++) {
            for (int day = 1; day <= 31; day++) {
                String fileName = "prices/germany/" + month + "/2022-" + String.format("%02d", month) + "-" + String.format("%02d", day) + "-prices.csv";
                var file = ClassLoader.getSystemClassLoader().getResource(fileName);
                List<String[]> lines = Utils.readCSV(file);
                if (lines.isEmpty()) {
                    continue;
                }
                lines.forEach(line -> {
                    for (GermanStation station : germanStations) {
                        if (station.id.equals(line[1])) {
                            station.addTimeAndPrice(Time.valueOf(line[0].substring(11, 19)), Double.parseDouble(line[2]));
                            break;
                        }
                    }
                });
                endDayProcedure(Date.valueOf("2022-" + String.format("%02d", month) + "-" + String.format("%02d", day)));
            }
        }

        // remove stations that contain only zero values during the whole period of 6 months
        AtomicInteger zeroStations = new AtomicInteger();
        List<GermanStation> copy = new ArrayList<>(germanStations);
        copy.forEach(station -> {
            if (station.avgPrices.get(station.avgPrices.size() - 1).avgPrice == 0.0) {
                zeroStations.getAndIncrement();
                germanStations.remove(station);
            }
        });

        System.out.println();
        System.out.println("Reading german prices and computing avg - DONE");
    }

    /**
     * For each station and a specific day, calculate avg price of the day based on the store price entries
     * and then clear the price entries for the station (ready for the next day)
     *
     * @param date the day we want to calculate the avg price
     */
    public static void endDayProcedure(Date date) {
        germanStations.forEach(station -> {
            if (station.prices.size() == 0) {
                // to improve this, manual import of prices for some days before the start of the analysis
                station.avgPrices.add(new PriceDatePair(station.lastPrice, date));
                //System.out.println(station.id + station.avgPrices);
                return;
            }
            computeAveragePriceOfDay(station, date);
            // set last price for the following day
            station.setLastPrice(station.prices.get(station.prices.size() - 1));
            // clear the price and timestamps arrays
            station.prices.clear();
            station.timestamps.clear();
            //System.out.println(station.id + station.avgPrices);
        });
    }

    /**
     * Given a station and a date, compute the average using the lastPrice (initial price of the day)
     * and all the entries of the day.
     * The mechanism is like the integral of the price over the day.
     * Finally, divide this value with the number of ms in a day (from 6 to 22)
     *
     * @param station the station we want to calculate the average
     * @param date    the date we want to calculate the average
     */
    public static void computeAveragePriceOfDay(GermanStation station, Date date) {
        double totalPrice = 0.0;
        Double lastPrice = station.lastPrice;
        Time lastTime = Time.valueOf("06:00:00");
        final Time startTime = Time.valueOf("06:00:00");
        final Time endTime = Time.valueOf("22:00:00");
        for (int i = 0; i < station.timestamps.size(); i++) {
            if (station.timestamps.get(i).before(startTime))
                station.setLastPrice(station.prices.get(i));
            else if (station.timestamps.get(i).before(endTime)) {
                totalPrice += lastPrice * (station.timestamps.get(i).getTime() - lastTime.getTime()) / 1000;
                lastPrice = station.prices.get(i);
                lastTime = station.timestamps.get(i);
            }
        }
        totalPrice += lastPrice * (endTime.getTime() - lastTime.getTime()) / 1000;
        Double avgPrice = totalPrice / ONE_DAY_6_22;
        station.avgPrices.add(new PriceDatePair(avgPrice, date));
    }

    /**
     * Read foreign price dataset
     *
     * @return a list of price entries
     */
    public static List<ForeignPriceEntry> readForeignPriceDataset() {
        var file = ClassLoader.getSystemClassLoader().getResource("prices/foreign/foreign_petrol_prices.csv");
        List<ForeignPriceEntry> foreignPriceDataset = Utils.readCSV(file).stream().map(line -> new ForeignPriceEntry(CountryCode.valueOf(line[0]),
                Date.valueOf(line[1]), Double.parseDouble(line[2]))).toList();
        System.out.println();
        System.out.println("Reading foreign prices and computing avg - DONE");
        return foreignPriceDataset;
    }

    public static void main(GermanAveragePriceService germanAveragePriceService, ForeignAveragePriceService foreignAveragePriceService, StationOfInterestService stationOfInterestService) throws Exception {
        AveragePricesComputation.germanAveragePriceService = germanAveragePriceService;
        AveragePricesComputation.foreignAveragePriceService = foreignAveragePriceService;
        AveragePricesComputation.stationOfInterestService = stationOfInterestService;
        germanStations = germanStations();

        // FOR GERMAN STATIONS
        readPricesSomeDaysBeforeStart();
        readAllDaysPrices();
        writeStationsAndAvgPricesDE();

        // FOR FOREIGN STATIONS
        writeStationsAndAvgPricesFOREIGN();
    }

    /**
     * Read all german stations within 20 km from the border from the database
     *
     * @return a list of german stations
     */
    public static CopyOnWriteArrayList<GermanStation> germanStations() {
        return new CopyOnWriteArrayList<>(stationOfInterestService.getAllByCountry(CountryCode.GER).stream()
                .filter(stationOfInterest -> stationOfInterest.getBorderDistance() <= 20)
                .map(station -> new GermanStation(station.getId())).toList());
    }

    /**
     * Write for every german station the avg price for every day of the analysis
     */
    public static void writeStationsAndAvgPricesDE() {
        String filename = "output/allStationsAndAvgPricesDE.csv";
        String[] columns = new String[]{
                "id",
                "date",
                "avgPrice"
        };
        List<String> lines = new CopyOnWriteArrayList<>();
        Set<String> ids = new HashSet<>(germanAveragePriceService.getAllIds());
        germanStations.forEach(germanStation ->
                germanStation.avgPrices.stream().filter(priceDatePair -> priceDatePair.date.after(Date.valueOf("2022-04-14"))).parallel().forEach(
                        priceDatePair -> {
                            var entry = new GermanAveragePrice(germanStation.id, priceDatePair.date, priceDatePair.avgPrice);
                            if (!ids.contains(entry.getId())) {
                                germanAveragePriceService.save(entry);
                            }
                            lines.add(germanStation.id + "," + priceDatePair.date + "," + priceDatePair.avgPrice);
                        }));
        Utils.writeCSV(filename, columns, lines);
        System.out.println();
        System.out.println("Writing DE stations and avg prices - DONE");

    }

    /**
     * Write for every german station the avg price for every day of the analysis
     */
    public static void writeStationsAndAvgPricesFOREIGN() {
        List<ForeignPriceEntry> foreignPriceDataset = readForeignPriceDataset();
        String filename = "output/allStationsAndAvgPricesFOREIGN.csv";
        String[] columns = new String[]{
                "id",
                "date",
                "avgPrice"
        };
        List<String> lines = new ArrayList<>();
        // for every station in the foreignStations20Km.csv
        var file = ClassLoader.getSystemClassLoader().getResource("foreignStations20Km.csv");
        final Date start = Date.valueOf("2022-04-15");
        final Date end = Date.valueOf("2022-10-15");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Set<String> ids = new HashSet<>(foreignAveragePriceService.getAllIds());
        Utils.readCSV(file).parallelStream().forEach(line -> {
            for (ForeignPriceEntry entry : foreignPriceDataset) {
                // find the first entry in foreign_petrol_prices.csv with same country code
                if (entry.countryCode == CountryCode.valueOf(line[1])) {
                    // 604 800 000 is one week in ms
                    for (java.util.Date date = entry.date; date.before(new Date(entry.date.getTime() + ONE_WEEK)); date = DateUtils.addDays(date, 1)) {
                        if (date.before(start) || date.after(end))
                            continue;
                        ForeignAveragePrice pr = new ForeignAveragePrice(line[0], date, entry.price);
                        if (!ids.contains(pr.getId())) {
                            foreignAveragePriceService.save(pr);
                        }
                        lines.add(line[0] + "," + dateFormat.format(date) + "," + entry.price);
                    }
                }
            }
        });
        Utils.writeCSV(filename, columns, lines);
        System.out.println();
        System.out.println("Writing FOREIGN stations and avg prices - DONE");
    }
}