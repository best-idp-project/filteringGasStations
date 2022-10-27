package filteringgasstations.routing.osrm;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import filteringgasstations.routing.Route;
import filteringgasstations.stations.GasStationPair;
import filteringgasstations.stations.Overpass;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;

public class OSRMClient {

    private static final String URL = "http://router.project-osrm.org/route/v1/driving/";

    public static Route getRoute(GasStationPair pair) {
        String builtUrl = URL + pair.getFirstStation().getLongitude() + "," + pair.getFirstStation().getLongitude() + ";"
                + pair.getSecondStation().getLongitude() + "," + pair.getSecondStation().getLatitude() + "?overview=false";
        System.out.println(builtUrl);
        try {
            int status = 0;
            HttpURLConnection connection;
            java.net.URL url = new URL(builtUrl);
            do {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                status = connection.getResponseCode();
                if (status != 200) {
                    System.out.println("REQUEST FAILED");
                }
            } while (status != 200);
            OSRMResponse route = new Gson().fromJson(new InputStreamReader(connection.getInputStream()), new TypeToken<OSRMResponse>() {
            }.getType());
            connection.disconnect();
            return route;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
