package filteringgasstations;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
}
