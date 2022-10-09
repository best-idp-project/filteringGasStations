package filteringgasstations;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class App {
    public static final int RANGE_KM = 50; // select the range
    public static final String format = "csv"; // select csv or json

    static HashMap<CountryCode, List<OverpassGasStation>> allStations = new HashMap<>();

    public static void main(String[] args) {
        List<BorderPoint> germanBorders = readGermanBorder();

        // Read all countries' gas stations
        System.out.println("HOW MANY GAS STATIONS PER COUNTRY");
        readGasStationsForEachCountry();

        // For every country, check for every gas station the distance to all points of the german border
        String s1 = "output/gasStationsRange";

        System.out.println("\nHOW MANY GAS STATIONS INSIDE RANGE");
        if (format.equals("csv"))
            gasStationsInRangeCSV(germanBorders, s1);
        else
            gasStationsInRangeJSON(germanBorders, s1);

        System.out.println("*** END ***");
    }

    private static List<BorderPoint> readGermanBorder() {
        List<BorderPoint> points = new ArrayList<>();
        try {
            Type BORDER_TYPE = new TypeToken<List<List<Double>>>() {
            }.getType();
            Gson gson = new Gson();
            var file = ClassLoader.getSystemClassLoader().getResource("borders_germany.json");
            assert file != null;
            JsonReader reader = new JsonReader(new FileReader(file.getPath()));
            List<List<Double>> parsedPoints = gson.fromJson(reader, BORDER_TYPE);
            points = parsedPoints.stream().filter(array -> array.size() == 2).map(array -> {
                var longitude = array.get(0);
                var latitude = array.get(1);
                return new BorderPoint(latitude, longitude);
            }).toList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("\n German Borders has " + points.size() + " points\n");
        return points;
    }

    public static List<OverpassGasStation> readJSON(String country) {
        Type OVERPASS_TYPE = new TypeToken<Overpass>() {
        }.getType();
        var file = ClassLoader.getSystemClassLoader().getResource("json/" + country + "_gas_stations.json");
        if (file == null) {
            return Collections.emptyList();
        }
        Gson gson = new Gson();
        try {
            JsonReader reader = new JsonReader(new FileReader(file.getPath()));
            Overpass data = gson.fromJson(reader, OVERPASS_TYPE);
            var not_node = Arrays.stream(data.elements).filter(n -> !n.type.equals("node")).toList();
            return Arrays.stream(data.elements).filter(n -> n.type.equals("node")).toList();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private static void gasStationsInRangeJSON(List<BorderPoint> germanBorders, String s1) {
        boolean found = false;
        int counterFound = 0;

        File filejson = new File("output");
        if (!filejson.exists()) {
            var _bool = filejson.mkdir();
        }
        try {

            String filenamejson = s1.concat(String.valueOf(RANGE_KM)).concat(".json");
            filejson = new File(filenamejson);
            Gson gson = new Gson();
            FileWriter fwjson = new FileWriter(filejson);

            List<GasStation> stationsInsideRange = new ArrayList<>();

            for (CountryCode country : CountryCode.values()) {
                if (country == CountryCode.GER) {
                    continue;
                }
                String countryName = country.getName();
                List<OverpassGasStation> countryGasList = allStations.getOrDefault(country, new ArrayList<>());

                for (GasStation g : countryGasList) {
                    for (BorderPoint b : germanBorders) {
                        if ((Utils.distance(b.latitude, g.lat, b.longitude, g.lon) < RANGE_KM) && (!found)) {
                            stationsInsideRange.add(g);
                            counterFound++;
                            found = true;
                        }
                    }
                    found = false;
                }
                System.out.println("Country " + countryName + ": " + counterFound + " gas stations inside " + RANGE_KM + "km");
                counterFound = 0;
            }
            gson.toJson(stationsInsideRange, fwjson);
            fwjson.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void gasStationsInRangeCSV(List<BorderPoint> germanBorders, String s1) {
        boolean found = false;
        int counterFound = 0;

        File filecsv = new File("output");
        if (!filecsv.exists()) {
            var _bool = filecsv.mkdir();
        }

        try {
            String filenamecsv = s1.concat(String.valueOf(RANGE_KM)).concat(".csv");
            filecsv = new File(filenamecsv);
            FileWriter fwcsv = new FileWriter(filecsv);

            for (CountryCode country : CountryCode.values()) {
                if (country == CountryCode.GER) {
                    continue;
                }
                String countryName = country.getName();
                List<OverpassGasStation> countryGasList = allStations.getOrDefault(country, new ArrayList<>());

                for (GasStation g : countryGasList) {
                    for (BorderPoint b : germanBorders) {
                        if ((Utils.distance(b.latitude, g.lat, b.longitude, g.lon) < RANGE_KM) && (!found)) {
                            fwcsv.append(g.toString()).append("\n");
                            counterFound++;
                            found = true;
                        }
                    }
                    found = false;
                }
                System.out.println("Country " + countryName + ": " + counterFound + " gas stations inside " + RANGE_KM + "km");
                counterFound = 0;
            }
            fwcsv.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readGasStationsForEachCountry() {
        for (CountryCode countryCode : CountryCode.values()) {
            if (countryCode == CountryCode.GER) {
                continue;
            }
            String countryName = countryCode.getName();
            var jsonStations = readJSON(countryName.toLowerCase());
            List<OverpassGasStation> stations = new ArrayList<>(jsonStations);
            stations.forEach(OverpassGasStation::addImportantFields);
            stations = stations.stream().distinct().toList();

            allStations.put(countryCode, stations);

            System.out.println(countryName + " has " + stations.size() + " gas stations");
        }
    }
}
