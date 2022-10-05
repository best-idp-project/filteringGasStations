package filteringgasstations;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class App {
    public static final int RANGE_KM = 50;

    static HashMap<CountryCode, List<GasStation>> allStations = new HashMap<>();

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
        System.out.println("German Borders has " + points.size() + " points");
        return points;
    }

    public static List<GasStation> readCSV(String country) {
        var file = ClassLoader.getSystemClassLoader().getResource("csv/" + country + "_gas_stations.csv");
        if (file == null) {
            return Collections.emptyList();
        }
        return Utils.readFile(file.getPath()).stream().filter(Objects::nonNull).map(line -> line.split("\t")).filter(line -> Arrays.stream(line).allMatch(column -> {
            try {
                Double.parseDouble(column);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        })).map(line -> {
            var id = Long.parseLong(line[0]);
            var latitude = Double.parseDouble(line[1]);
            var longitude = Double.parseDouble(line[2]);
            return new GasStation(id, latitude, longitude);
        }).toList();
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

    public static void main(String[] args) {
        List<BorderPoint> germanBorders = readGermanBorder();

        // READ THE GAS STATIONS OF ANOTHER COUNTRY
        for (CountryCode countryCode : CountryCode.values()) {
            if (countryCode == CountryCode.GER) {
                continue;
            }
            String countryName = countryCode.getName();
            var csvStations = readCSV(countryName.toLowerCase());
            var jsonStations = readJSON(countryName.toLowerCase());
            List<GasStation> stations = new ArrayList<>(jsonStations);
            if (jsonStations.size() != csvStations.size()) {
                System.out.println("JSON(" + jsonStations.size() + ") and CSV(" + csvStations.size() + ") data not of equal size. -.-");
            }
            stations.addAll(csvStations);
            stations = stations.stream().distinct().toList();

            allStations.put(countryCode, stations);

            System.out.println(countryName + " has " + stations.size() + " gas stations \n");
        }

        // now for every country, check for every gas station the distance to all points of the german border
        // if the distance is bigger than RANGE_KM
        String s1 = "output/gasStationsRange";
        boolean found = false;
        int counterFound = 0;
        File file = new File("output");
        if (!file.exists()) {
            var _bool = file.mkdir();
        }

        try {
            String filename = s1.concat(String.valueOf(RANGE_KM)).concat(".csv");
            file = new File(filename);

            FileWriter fw = new FileWriter(file);

            for (CountryCode country : CountryCode.values()) {
                if (country == CountryCode.GER) {
                    continue;
                }
                String countryName = country.getName();
                List<GasStation> countryGasList = allStations.getOrDefault(country, new ArrayList<>());

                for (GasStation g : countryGasList) {
                    for (BorderPoint b : germanBorders) {
                        if ((Utils.distance(b.latitude, g.lat, b.longitude, g.lon) < RANGE_KM) && (!found)) {
                            //System.out.println("Country " + countryName + " one inside range");
                            // write the resulting stations in a separate file
                            //fw.append(country.getKey() + " Lat: " + g.lat + " Lon: " + g.lon + " \n");
                            fw.append(g.toString()).append("\n");
                            counterFound++;
                            found = true;
                        }
                    }
                    found = false;
                }
                System.out.println("Country " + countryName + ": " + counterFound + " gas stations inside " + RANGE_KM + "km");
                counterFound = 0;
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("*** END ***");
    }
}
