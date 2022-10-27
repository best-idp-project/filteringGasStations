package filteringgasstations.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.opencsv.CSVReader;
import filteringgasstations.geolocation.BorderPoint;
import filteringgasstations.geolocation.CountryCode;
import filteringgasstations.stations.GasStationAddress;
import filteringgasstations.stations.Overpass;
import filteringgasstations.stations.OverpassGasStation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class Utils {

    /**
     * Calculate distance between two points in latitude and longitude
     * <p>
     * lat1, lon1 Start point lat2, lon2 End point
     *
     * @return Distance in Kilometers
     */
    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000;

        distance = Math.pow(distance, 2);
        //System.out.println(Math.sqrt(distance) / 1000);
        return Math.sqrt(distance) / 1000;
    }

    public static List<String> readFile(String file) {
        List<String> lines = new ArrayList<>();
        BufferedReader reader = null;
        String line = null;
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        assert reader != null;
        do {
            try {
                line = reader.readLine();
                lines.add(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } while (line != null);
        return lines;
    }


    public static List<OverpassGasStation> readCountryGasStationsJSON(CountryCode country) {
        Type OVERPASS_TYPE = new TypeToken<Overpass>() {
        }.getType();
        var file = ClassLoader.getSystemClassLoader().getResource("json/" + country.getCode() + ".json");
        if (file == null) {
            return Collections.emptyList();
        }
        Gson gson = new Gson();
        try {
            JsonReader reader = new JsonReader(new FileReader(file.getPath()));
            Overpass data = gson.fromJson(reader, OVERPASS_TYPE);
            return Arrays.stream(data.elements).filter(n -> n.type.equals("node")).toList();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public static List<BorderPoint> readGermanBorder() {
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
        System.out.println("German Borders has " + points.size() + " points\n");
        return points;
    }

    public static HashMap<CountryCode, List<OverpassGasStation>> readGasStationsForEachCountry() {
        HashMap<CountryCode, List<OverpassGasStation>> allStations = new HashMap<>();
        for (CountryCode countryCode : CountryCode.values()) {
            if (countryCode == CountryCode.GER) {
                continue;
            }
            List<OverpassGasStation> stations = Utils.readCountryGasStationsJSON(countryCode).stream().toList();
            stations.forEach(station -> station.addImportantFields(countryCode));

            allStations.put(countryCode, stations);

            System.out.println(countryCode.getName() + " has " + stations.size() + " gas stations");
        }
        return allStations;
    }

    public static List<OverpassGasStation> readGermanStations() {
        List<OverpassGasStation> germanStations = new ArrayList<>();
        var file = ClassLoader.getSystemClassLoader().getResource("stations_germany.csv");

        try {
            assert file != null;
            try (CSVReader reader = new CSVReader(new FileReader(file.getPath()))) {
                String[] lineInArray;
                reader.readNext();  // skip the header
                while ((lineInArray = reader.readNext()) != null) {
                    GasStationAddress address = new GasStationAddress(CountryCode.GER, lineInArray[6], lineInArray[3],
                            lineInArray[4], lineInArray[5], lineInArray[1]);
                    OverpassGasStation station = new OverpassGasStation(lineInArray[0], Double.parseDouble(lineInArray[7]),
                            Double.parseDouble(lineInArray[8]), address);
                    germanStations.add(station);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Germany has " + germanStations.size() + " gas stations");
        return germanStations;
    }
}
