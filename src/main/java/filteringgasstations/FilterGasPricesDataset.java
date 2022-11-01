package filteringgasstations;

import com.opencsv.CSVReader;
import filteringgasstations.stations.GasStationPair;
import filteringgasstations.stations.GermanStation;
import filteringgasstations.utils.PriceDatePair;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FilterGasPricesDataset {
    static List<GermanStation> germanStations = germanStations();

    public static void main(String[] args) {

        readPricesSomeDaysBeforeStart();
        readAllDaysPrices();
        writeStationsAndAvgPrices();
    }

    public static List<GermanStation> germanStations() {
        List<GermanStation> germanStations = new ArrayList<>();
        var file = ClassLoader.getSystemClassLoader().getResource("germanStations20Km.csv");

        try {
            assert file != null;
            try (CSVReader reader = new CSVReader(new FileReader(file.getPath()))) {
                String[] lineInArray;
                while ((lineInArray = reader.readNext()) != null) {
                    GermanStation temp = new GermanStation(lineInArray[0]);
                    germanStations.add(temp);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("German stations correctly imported");
        return germanStations;
    }

    public static void readPricesSomeDaysBeforeStart() {
        for (int day = 10; day < 15; day++) {
            String fileName = "prices/germany/extra/2022-04-" + day + "-prices.csv";
            var file = ClassLoader.getSystemClassLoader().getResource(fileName);
            try {
                try (CSVReader reader = new CSVReader(new FileReader(file.getPath()))) {
                    String[] lineInArray;
                    reader.readNext();  // skip header
                    while ((lineInArray = reader.readNext()) != null) {
                        for (GermanStation i : germanStations) {
                            if (i.id.equals(lineInArray[1])) {
                                i.setLastPrice(Double.parseDouble(lineInArray[2]));
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Prices of the 5 days before start OK");
    }

    public static void readAllDaysPrices() {
        for (int month = 4; month <= 10; month++) {
            for (int day = 1; day <= 31; day++) {
                String fileName = "prices/germany/" + month + "/2022-" + String.format("%02d", month) + "-" + String.format("%02d", day) + "-prices.csv";
                var file = ClassLoader.getSystemClassLoader().getResource(fileName);
                if (file == null) continue;
                try {
                    try (CSVReader reader = new CSVReader(new FileReader(file.getPath()))) {
                        String[] lineInArray;
                        reader.readNext();  // skip header
                        while ((lineInArray = reader.readNext()) != null) {
                            for (GermanStation i : germanStations) {
                                if (i.id.equals(lineInArray[1])) {
                                    i.addTimeAndPrice(Time.valueOf(lineInArray[0].substring(11, 19)), Double.parseDouble(lineInArray[2]));
                                    //System.out.println(i.timestamps.get(i.timestamps.size() - 1));
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //System.out.println(germanStations);
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
            } else
                System.out.println(station.id + " " + station.avgPrices);
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
        Double totalPrice = 0.0;
        Double lastPrice = station.lastPrice;
        Time lastTime = Time.valueOf("06:00:00");
        for (int i = 0; i < station.timestamps.size(); i++) {
            if (station.timestamps.get(i).before(Time.valueOf("06:00:00")))
                station.setLastPrice(station.prices.get(i));
            else if (station.timestamps.get(i).before(Time.valueOf("22:00:00"))) {
                totalPrice += lastPrice * (station.timestamps.get(i).getTime() - lastTime.getTime()) / 1000;
                lastPrice = station.prices.get(i);
                lastTime = station.timestamps.get(i);
            }
        }
        totalPrice += lastPrice * (Time.valueOf("22:00:00").getTime() - lastTime.getTime()) / 1000;
        Double avgPrice = totalPrice / ((22 - 6) * 60 * 60);
        station.avgPrices.add(new PriceDatePair(avgPrice, date));
    }

    public static void writeStationsAndAvgPrices() {

        File filecsv = new File("output");
        if (!filecsv.exists()) {
            var _bool = filecsv.mkdir();
        }

        try {
            filecsv = new File("output/allStationsAndAvgPricesDE.csv");
            FileWriter fwcsv = new FileWriter(filecsv);
            fwcsv.append("id,date,avgPrice\n");

            for (GermanStation station : germanStations) {
                for (PriceDatePair entry : station.avgPrices)
                    fwcsv.append(station.id + "," + entry.date + "," + entry.avgPrice + "\n");
            }
            fwcsv.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
