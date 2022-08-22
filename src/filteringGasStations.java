import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class filteringGasStations {
    static List<Borders> germanBorders = new ArrayList<>();
    static List<GasStation> gasStationsAustria = new ArrayList<GasStation>();
    static List<GasStation> gasStationsBelgium = new ArrayList<GasStation>();
    static List<GasStation> gasStationsCzechia = new ArrayList<GasStation>();
    static List<GasStation> gasStationsDenmark = new ArrayList<GasStation>();
    static List<GasStation> gasStationsFrance = new ArrayList<GasStation>();
    static List<GasStation> gasStationsLuxembourg = new ArrayList<GasStation>();
    static List<GasStation> gasStationsNetherlands = new ArrayList<GasStation>();
    static List<GasStation> gasStationsPoland = new ArrayList<GasStation>();
    static List<GasStation> gasStationsSwitzerland = new ArrayList<GasStation>();

    public static final int RANGE_KM = 30;


    enum countries {
        AUSTRIA("austria", gasStationsAustria), BELGIUM("belgium", gasStationsBelgium),
        CZECHIA("czechia", gasStationsCzechia), DENMARK("denmark", gasStationsDenmark),
        FRANCE("france", gasStationsFrance), LUXEMBOURG("luxembourg", gasStationsLuxembourg),
        NETHERLANDS("netherlands", gasStationsNetherlands), POLAND("poland", gasStationsPoland),
        SWITZERLAND("switzerland", gasStationsSwitzerland);

        private final String key;
        private final List value;

        countries(String key, List value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public List getValue() {
            return value;
        }
    }

    /**
     * Calculate distance between two points in latitude and longitude
     * <p>
     * lat1, lon1 Start point lat2, lon2 End point
     *
     * @returns Distance in Kilometers
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

    public static void main(String[] args) {
        // READ THE GERMAN BORDERS AND STORE THEM

        // open the file as a reader
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("./resources/borders_germany.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // read line by line the german borders
        String line = null;
        Pattern decimalNumPattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        Matcher matcher = null;
        try {
            line = br.readLine();
            //System.out.println("line read:" + line);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // central part of the file until the end
        while (!Objects.equals(line, "]") && line != null) {
            try {
                line = br.readLine();
                //System.out.println("line read:" + line);
                String onlyNumericText = line.replaceAll("[^\\w\\s\\.]", "");
                //System.out.println("only numeric:" + onlyNumericText);
                matcher = decimalNumPattern.matcher(onlyNumericText);
                if (matcher.find()) {
                    double lon = Double.valueOf(matcher.group());
                    matcher.find();
                    double lat = Double.valueOf(matcher.group());
                    Borders border = new Borders(lat, lon);
                    germanBorders.add(border);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("German Borders are " + germanBorders.size() + " points \n");
        //System.out.println(germanBorders);


        // READ THE GAS STATIONS OF ANOTHER COUNTRY
        for (countries country : countries.values()) {
            String countryName = country.getKey();
            List countryGasList = country.getValue();
            try {
                br = new BufferedReader(new FileReader("./resources/" + countryName + "_gas_stations.csv"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            try {
                line = br.readLine();
                //System.out.println("first line read:" + line);
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (line != null) {
                try {
                    line = br.readLine();
                    if (line != null) {
                        //System.out.println("line read:" + line);
                        String onlyNumericText = line.replaceAll("[^\\w\\s\\.]", "");
                        //System.out.println("only numeric:" + onlyNumericText);
                        matcher = decimalNumPattern.matcher(onlyNumericText);
                        if (matcher.find()) {
                            matcher.find();
                            double lat = Double.valueOf(matcher.group());
                            matcher.find();
                            double lon = Double.valueOf(matcher.group());
                            //System.out.println(lat);
                            //System.out.println(lon);
                            GasStation gasStation = new GasStation(lat, lon);
                            countryGasList.add(gasStation);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(countryName + " has " + countryGasList.size() + " gas stations \n");
            //System.out.println(countryGasList);
        }

        // now for every country, check for every gas station the distance to all points of the german border
        // if the distance is bigger than RANGE_KM
        String s1 = "./output/gasStationsRange";
        boolean found = false;
        int counterFound = 0;


        try {
            FileWriter fw = new FileWriter(s1.concat(String.valueOf(RANGE_KM)).concat(".txt"));
            for (countries country : countries.values()) {
                String countryName = country.getKey();
                List<GasStation> countryGasList = country.getValue();

                for (GasStation g : countryGasList) {
                    //System.out.println(counter + "/" + countryGasList.size());
                    for (Borders b : germanBorders) {
                        if ((distance(b.lat, g.lat, b.lon, g.lon) < RANGE_KM) && (!found)) {
                            //System.out.println("Country " + countryName + " one inside range");
                            // write the resulting stations in a separate file
                            //fw.append(country.getKey() + " Lat: " + g.lat + " Lon: " + g.lon + " \n");
                            fw.append(g.lon + "," + g.lat + "\n");
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
