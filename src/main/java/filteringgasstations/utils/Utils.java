package filteringgasstations.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.opencsv.CSVReader;
import filteringgasstations.database.models.GermanPrice;
import filteringgasstations.database.models.InputFile;
import filteringgasstations.database.service.GermanPriceService;
import filteringgasstations.database.service.InputFileService;
import filteringgasstations.geolocation.BorderPoint;
import filteringgasstations.geolocation.CountryCode;
import filteringgasstations.stations.GasStationAddress;
import filteringgasstations.stations.Overpass;
import filteringgasstations.stations.OverpassGasStation;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Utils {

    /**
     * Calculate distance between two points in latitude and longitude
     *
     * @param lat1 latitude of start point
     * @param lat2 latitude of end point
     * @param lon1 longitude of start point
     * @param lon2 longitude of end point
     * @return air distance between two points (km)
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

    /**
     * Helper which reads all gas stations from a file and stores it in a list
     *
     * @param country country code
     * @return list of gas stations read from the file
     */
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

    /**
     * Check if the border file changed
     *
     * @param inputFileService cached files
     * @return true if changed, false otherwise
     */
    public static boolean hasBorderChanged(InputFileService inputFileService) {
        String filename = "borders_germany.json";
        return hasFileChanged(inputFileService, filename);
    }

    /**
     * Check if a file has changed compared to the cached version
     *
     * @param inputFileService cached input files
     * @param filename         name of the file
     * @return true if changed, false otherwise
     */
    public static boolean hasFileChanged(InputFileService inputFileService, String filename) {
        var file = ClassLoader.getSystemClassLoader().getResource(filename);
        assert file != null;
        Optional<String> checksum = InputFile.getChecksum(filename);
        assert checksum.isPresent();
        Optional<InputFile> storedSum = inputFileService.get(filename);
        return storedSum.map(inputFile -> !inputFile.getHashsum().equals(checksum.get())).orElse(true);
    }

    /**
     * Read all points of the german border
     *
     * @return a list of border points in lat/lon
     */
    public static List<BorderPoint> readGermanBorder() {
        List<BorderPoint> points = new ArrayList<>();
        String filename = "borders_germany.json";

        System.out.println();
        System.out.println("READING GERMAN BORDER POINTS");

        try {
            var file = ClassLoader.getSystemClassLoader().getResource(filename);
            assert file != null;

            JsonReader reader = new JsonReader(new FileReader(file.getPath()));
            Type BORDER_TYPE = new TypeToken<List<List<Double>>>() {
            }.getType();
            Gson gson = new Gson();
            List<List<Double>> parsedPoints = gson.fromJson(reader, BORDER_TYPE);
            points = parsedPoints.stream().filter(array -> array.size() == 2).parallel().map(array -> {
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

    /**
     * Read gas stations for all countries (except DE), store everything in a hashmap
     *
     * @return hashmap with list of station for every country
     */
    public static HashMap<CountryCode, List<OverpassGasStation>> readGasStationsForEachCountry() {
        HashMap<CountryCode, List<OverpassGasStation>> allStations = new HashMap<>();
        System.out.println("READING ALL GAS STATIONS FOR EACH COUNTRY");
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

    /**
     * Separate read for german stations (different source)
     *
     * @return list of stations
     */
    public static List<OverpassGasStation> readGermanStations() {
        List<OverpassGasStation> germanStations = new ArrayList<>();
        var file = ClassLoader.getSystemClassLoader().getResource("stations_germany.csv");
        for (String[] line : Utils.readCSV(file)) {
            GasStationAddress address = new GasStationAddress(CountryCode.GER, line[6], line[3],
                    line[4], line[5], line[1]);
            OverpassGasStation station = new OverpassGasStation(line[0], Double.parseDouble(line[7]),
                    Double.parseDouble(line[8]), address);
            germanStations.add(station);
        }
        System.out.println("Germany has " + germanStations.size() + " gas stations");
        return germanStations;
    }

    public static List<GermanPrice> readGermanPrices(GermanPriceService germanPriceService) {
        List<GermanPrice> prices = new ArrayList<>();
        long startIDs = System.currentTimeMillis();
        List<String> ids = germanPriceService.getAllIds();
        Set<String> saved = ConcurrentHashMap.newKeySet(ids.size());
        saved.addAll(ids);
        System.out.println("getting ids took " + (System.currentTimeMillis() - startIDs) / 1000);

        String mainPath = "prices/germany";
        var path = ClassLoader.getSystemClassLoader().getResource(mainPath);
        assert path != null;
        File directory = new File(path.getPath());
        String[] subdirectories = Objects.requireNonNull(directory.list());
        Arrays.stream(subdirectories).parallel().forEach(subdirectoryName -> {
            var subPath = ClassLoader.getSystemClassLoader().getResource(mainPath + "/" + subdirectoryName);
            if (subPath == null) {
                return;
            }
            File subdirectory = new File(subPath.getPath());
            System.out.println("Reading " + subdirectory + ":");
            for (String file : Objects.requireNonNull(subdirectory.list())) {
                var filePath = ClassLoader.getSystemClassLoader().getResource(mainPath + "/" + subdirectoryName + "/" + file);
                long start = System.currentTimeMillis();
                AtomicInteger newOnes = new AtomicInteger();

                List<String[]> lines = Utils.readCSV(filePath);
                int amount = lines.size();
                AtomicInteger counter = new AtomicInteger();
                lines.parallelStream().forEach(line -> {
                    GermanPrice entry = new GermanPrice(
                            DateTime.parse(line[0], DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ssZ")),
                            line[1],
                            Double.parseDouble(line[2]));
                    if (!saved.contains(entry.getId())) {
                        newOnes.incrementAndGet();
                        germanPriceService.save(entry);
                        System.out.println(counter.incrementAndGet() + "/" + amount);
                    } else {
                        saved.remove(entry.getId());
                    }
                });
                System.out.println(file + " new: " + newOnes + " took " + (System.currentTimeMillis() - start) / 1000);

            }
        });
        return prices;
    }

    /**
     * Create directory in the project
     *
     * @param name the name of the directory
     */
    public static void createDirectory(String name) {
        try {
            Path parent = Path.of(name).getParent();
            Files.createDirectories(parent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper which writes a csv file with a certain path, columns and lines
     *
     * @param filepath output file path
     * @param columns  attributes name
     * @param lines    entries to write
     */
    public static void writeCSV(String filepath, String[] columns, List<String> lines) {
        createDirectory(filepath);

        try {
            File file = new File(filepath);
            FileWriter writer = new FileWriter(file);
            StringBuilder builder = new StringBuilder();
            List<String> columnList = new ArrayList<>(Arrays.stream(columns).toList());
            String firstColumn = columnList.remove(0);

            writer.write(columnList.stream().reduce(firstColumn, (acc, column) -> acc + "," + column) + "\n");

            lines.forEach(line -> builder.append(line).append("\n"));

            writer.write(builder.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper which reads a file (with or without header)
     *
     * @param file         path of the file
     * @param removeHeader keep or remove header boolean
     * @return list of string read from the csv file
     */
    public static List<String[]> readCSV(URL file, boolean removeHeader) {
        List<String[]> lines = new ArrayList<>();
        try {
            if (file == null) {
                return lines;
            }
            try (CSVReader reader = new CSVReader(new FileReader(file.getPath()))) {
                String[] line;
                if (removeHeader) {
                    reader.readNext();
                }
                while ((line = reader.readNext()) != null) {
                    lines.add(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lines;
    }

    public static List<String[]> readCSV(URL file) {
        return readCSV(file, true);
    }
}
