package filteringgasstations;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.opencsv.CSVReader;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static filteringgasstations.Utils.distance;


public class App {
    public static final int RANGE_KM = 20; // select the range

    static HashMap<CountryCode, List<OverpassGasStation>> allStations = new HashMap<>();
    static List<OverpassGasStation> allStationsIn20KmBorder = new ArrayList<>();
    static List<GasStationPair> allPairsIn10Km = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Reading border");
        List<BorderPoint> germanBorders = readGermanBorder();

        //Read all countries' gas stations
        System.out.println();
        System.out.println("Stations per country");
        readGasStationsForEachCountry();
        readGermanStations();

        // For every country, check for every gas station the distance to all points of the german border
        System.out.println();
        System.out.println("Stations inside range of " + RANGE_KM + "km");
        gasStationsInRange(germanBorders, "output/gasStationsRange");

        System.out.println();
        System.out.println("Calculating distances between stations");
        evaluateDistancesBetweenStations();

        System.out.println("Write valid pairs");
        printAllPairs();
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
        System.out.println("German Borders has " + points.size() + " points\n");
        return points;
    }

    public static List<OverpassGasStation> readJSON(String country) {
        Type OVERPASS_TYPE = new TypeToken<Overpass>() {
        }.getType();
        var file = ClassLoader.getSystemClassLoader().getResource("json/" + country + ".json");
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

    private static void gasStationsInRange(List<BorderPoint> germanBorders, String path) {
        boolean found = false;
        int counterFound = 0;

        File filecsv = new File("output");
        if (!filecsv.exists()) {
            var _bool = filecsv.mkdir();
        }

        try {
            String filenamecsv = path.concat(String.valueOf(RANGE_KM)).concat(".csv");
            filecsv = new File(filenamecsv);
            FileWriter fwcsv = new FileWriter(filecsv);
            fwcsv.append("id,lat,lon,country,city,street,housenumber,postcode,name\n");

            for (CountryCode country : CountryCode.values()) {
                List<OverpassGasStation> countryGasList = allStations.getOrDefault(country, new ArrayList<>());

                for (OverpassGasStation g : countryGasList) {
                    if (g.getAddr().getCountry() == null)
                        g.getAddr().setCountry(country.toString());
                    for (BorderPoint b : germanBorders) {
                        if ((distance(b.latitude, g.lat, b.longitude, g.lon) < RANGE_KM) && (!found)) {
                            fwcsv.append(g.toString()).append("\n");
                            allStationsIn20KmBorder.add(g);
                            counterFound++;
                            found = true;
                        }
                    }
                    found = false;
                }
                System.out.println(country.getName() + ": " + counterFound + " gas stations inside " + RANGE_KM + "km");
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
            var jsonStations = readJSON(countryCode.getCode());
            List<OverpassGasStation> stations = new ArrayList<>(jsonStations);
            stations.forEach(OverpassGasStation::addImportantFields);
            stations = stations.stream().distinct().toList();

            allStations.put(countryCode, stations);

            System.out.println(countryCode.getName() + " has " + stations.size() + " gas stations");
        }
    }

    public static void readGermanStations() {
        List<OverpassGasStation> germanStations = new ArrayList<>();
        var file = ClassLoader.getSystemClassLoader().getResource("stations_germany.csv");

        try {
            assert file != null;
            try (CSVReader reader = new CSVReader(new FileReader(file.getPath()))) {
                String[] lineInArray;
                reader.readNext();  // skip the header
                while ((lineInArray = reader.readNext()) != null) {
                    GasStationAddress address = new GasStationAddress("DE", lineInArray[6], lineInArray[3],
                            lineInArray[4], lineInArray[5], lineInArray[1]);
                    OverpassGasStation station = new OverpassGasStation(lineInArray[0], Double.parseDouble(lineInArray[7]),
                            Double.parseDouble(lineInArray[8]), address);
                    germanStations.add(station);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        allStations.put(CountryCode.GER, germanStations);

        System.out.println("Germany has " + germanStations.size() + " gas stations");

    }

    private static List<GasStationPair> getPairsOfInterest(List<OverpassGasStation> stations, double directDistanceLimit) {
        List<GasStationPair> pairs = new ArrayList<>();
        for (int fromIndex = 0; fromIndex < stations.size(); fromIndex++) {
            for (int toIndex = fromIndex + 1; toIndex < stations.size(); toIndex++) {
                OverpassGasStation from = stations.get(fromIndex);
                OverpassGasStation to = stations.get(toIndex);
                if (!"DE".equals(from.addr.country) && !"DE".equals(to.addr.country)) {
                    continue;
                }
                double directDistance = distance(from.lat, to.lat, from.lon, to.lon);
                if (directDistance > directDistanceLimit) {
                    continue;
                }
                pairs.add(new GasStationPair(from, to, directDistance));
            }
        }
        return pairs;
    }

    public static void evaluateDistancesBetweenStations() {
        var pairs = getPairsOfInterest(allStationsIn20KmBorder, 10);
        AtomicInteger counter = new AtomicInteger(0);
        allPairsIn10Km = pairs.parallelStream().peek(gasStationPair -> {
            httpRequestDrivingTimeAndDistances(gasStationPair);
            System.out.println("http req #: " + counter.incrementAndGet() + "/" + pairs.size());
        }).toList();
        System.out.println("There are " + allPairsIn10Km.size() + " pairs with air distance <= 10 km");
    }

    public static void httpRequestDrivingTimeAndDistances(GasStationPair pair) {
        String builtUrl = "http://router.project-osrm.org/route/v1/driving/" + pair.firstStation.lon + "," + pair.firstStation.lat + ";"
                + pair.secondStation.lon + "," + pair.secondStation.lat + "?overview=false";
        try {
            int status = 0;
            HttpURLConnection connection;
            URL url = new URL(builtUrl);
            do {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                status = connection.getResponseCode();
                if (status != 200) {
                    System.out.println("REQUEST FAILED");
                }
            } while (status != 200);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                JsonObject response = new Gson().fromJson(inputLine, JsonObject.class);
                JsonArray routes = response.get("routes").getAsJsonArray();
                JsonObject intermediate = routes.get(0).getAsJsonObject();
                JsonArray legs = intermediate.get("legs").getAsJsonArray();
                JsonObject intermediate1 = legs.get(0).getAsJsonObject();
                Double drivingDistance = intermediate1.get("distance").getAsDouble();
                Double drivingTime = intermediate1.get("duration").getAsDouble();
                //System.out.println(" Driving distance: " + drivingDistance + " and driving times: " + drivingTime);
                pair.setDrivingDistance(drivingDistance);
                pair.setDrivingTime(drivingTime);
            }
            in.close();
            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void printAllPairs() {

        File filecsv = new File("output");
        if (!filecsv.exists()) {
            var _bool = filecsv.mkdir();
        }

        try {
            filecsv = new File("output/allPairsIn10Km.csv");
            FileWriter fwcsv = new FileWriter(filecsv);
            fwcsv.append("idFirstStation,idSecondStation,airDistance,drivingDistance,drivingTime\n");

            for (GasStationPair pair : allPairsIn10Km) {
                fwcsv.append(pair.toString()).append("\n");
            }
            fwcsv.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
