package filteringgasstations;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class App {
    public static final int RANGE_KM = 30;

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
            points = parsedPoints.stream()
                    .filter(array -> array.size() == 2)
                    .map(array -> {
                        var longitude = array.get(0);
                        var latitude = array.get(1);
                        return new BorderPoint(latitude, longitude);
                    })
                    .toList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("German Borders has " + points.size() + " points");
        return points;
    }

    public static void main(String[] args) {
        List<BorderPoint> germanBorders = readGermanBorder();

        // READ THE GAS STATIONS OF ANOTHER COUNTRY
        for (CountryCode countryCode : CountryCode.values()) {
            if (countryCode == CountryCode.GER) {
                continue;
            }
            String countryName = countryCode.getName();
            List<GasStation> stations = new ArrayList<>();
            var file = ClassLoader.getSystemClassLoader().getResource(countryName.toLowerCase() + "_gas_stations.csv");
            if (file != null) {
                stations = Utils.readFile(file.getPath())
                        .stream()
                        .filter(Objects::nonNull)
                        .map(line -> line.split("\t"))
                        .filter(line -> Arrays.stream(line).allMatch(column -> {
                            try {
                                Double.parseDouble(column);
                                return true;
                            } catch (NumberFormatException e) {
                                return false;
                            }
                        }))
                        .map(line -> {
                            var id = line[0];
                            var latitude = Double.parseDouble(line[1]);
                            var longitude = Double.parseDouble(line[2]);
                            return new GasStation(latitude, longitude);
                        }).toList();
            }
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
            String filename = s1.concat(String.valueOf(RANGE_KM)).concat(".txt");
            file = new File(filename);

            FileWriter fw = new FileWriter(file);

            for (CountryCode country : CountryCode.values()) {
                String countryName = country.getName();
                List<GasStation> countryGasList = allStations.getOrDefault(country, new ArrayList<>());

                for (GasStation g : countryGasList) {
                    for (BorderPoint b : germanBorders) {
                        if ((Utils.distance(b.latitude, g.lat, b.longitude, g.lon) < RANGE_KM) && (!found)) {
                            //System.out.println("Country " + countryName + " one inside range");
                            // write the resulting stations in a separate file
                            //fw.append(country.getKey() + " Lat: " + g.lat + " Lon: " + g.lon + " \n");
                            fw.append(String.valueOf(g.lon)).append(",").append(String.valueOf(g.lat)).append("\n");
                            counterFound++;
                            found = true;
                        }
                    }
                    found = false;
                }
                System.out.println("Country " + countryName + ": " + counterFound + " gas stations inside 30km");
                counterFound = 0;
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("*** END ***");
    }
}
