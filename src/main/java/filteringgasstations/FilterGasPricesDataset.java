package filteringgasstations;


import filteringgasstations.database.models.ForeignAveragePrice;
import filteringgasstations.database.models.GermanAveragePrice;
import filteringgasstations.database.service.ForeignAveragePriceService;
import filteringgasstations.database.service.GermanAveragePriceService;
import filteringgasstations.geolocation.CountryCode;
import filteringgasstations.stations.ForeignPriceEntry;
import filteringgasstations.stations.GermanStation;
import filteringgasstations.utils.PriceDatePair;
import filteringgasstations.utils.Utils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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

@SpringBootApplication
public class FilterGasPricesDataset implements CommandLineRunner {

    @Autowired
    private GermanAveragePriceService germanAveragePriceService;

    @Autowired
    private ForeignAveragePriceService foreignAveragePriceService;
    private static final int ONE_WEEK = 604800000;
    static List<GermanStation> germanStations;

    public static CopyOnWriteArrayList<GermanStation> germanStations() {
        var file = ClassLoader.getSystemClassLoader().getResource("germanStations20Km.csv");
        List<GermanStation> germanStations = Utils.readCSV(file).stream().map(line -> new GermanStation(line[0])).toList();
        System.out.println("German stations correctly imported");
        return new CopyOnWriteArrayList<>(germanStations);
    }

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
        System.out.println("Prices of the 15 days before start OK");
    }
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
                            //System.out.println(i.timestamps.get(i.timestamps.size() - 1));
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
            } else {
                //System.out.println(station.id + " " + station.avgPrices);
            }
        });
        //System.out.println("missing prices counter: " + zeroStations.get());

    }

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
        Double avgPrice = totalPrice / ((22 - 6) * 60 * 60);
        station.avgPrices.add(new PriceDatePair(avgPrice, date));
    }

    public static void writeStationsAndAvgPricesDE(GermanAveragePriceService germanAveragePriceService) {
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
        System.out.println("All stations and avg prices for DE done");

    }

    public static List<ForeignPriceEntry> readForeignPriceDataset() {
        var file = ClassLoader.getSystemClassLoader().getResource("prices/foreign/foreign_petrol_prices.csv");
        List<ForeignPriceEntry> foreignPriceDataset = Utils.readCSV(file).stream().map(line -> new ForeignPriceEntry(CountryCode.valueOf(line[0]),
                Date.valueOf(line[1]), Double.parseDouble(line[2]))).toList();
        System.out.println("Foreign price dataset OK");
        return foreignPriceDataset;
    }

    public void writeStationsAndAvgPricesFOREIGN() {
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
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final Date start = Date.valueOf("2022-04-15");
        final Date end = Date.valueOf("2022-10-15");
        Set<String> ids = new HashSet<>(foreignAveragePriceService.getAllIds());
        Utils.readCSV(file).parallelStream().forEach(line -> {
            for (ForeignPriceEntry entry : foreignPriceDataset) {
                // find the first entry in foreign_petrol_prices.csv with same country code
                if (entry.countryCode == CountryCode.valueOf(line[1]))
                    // 604 800 000 is one week in ms
                    for (java.util.Date date = entry.date; date.before(new Date(entry.date.getTime() + ONE_WEEK)); date = DateUtils.addDays(date, 1)) {
                        if (date.before(start) || date.after(end))
                            continue;
                        ForeignAveragePrice pr = new ForeignAveragePrice(line[0], date, entry.price);
                        if (!ids.contains(pr.getId())) {
                            foreignAveragePriceService.save(pr);
                        }
                        //lines.add(line[0] + "," + dateFormat.format(date) + "," + entry.price);
                    }
            }
        });
        Utils.writeCSV(filename, columns, lines);
    }

    public static void main(String[] args) {
        SpringApplication.run(FilterGasPricesDataset.class, args);
    }
    @Override
    public void run(String... args) throws Exception {
        germanStations = germanStations();
        // FOR GERMAN STATIONS
        //readPricesSomeDaysBeforeStart();
        //readAllDaysPrices();
        //writeStationsAndAvgPricesDE(germanAveragePriceService);

        // FOR FOREIGN STATIONS
        writeStationsAndAvgPricesFOREIGN();
        System.exit(0);
    }
}