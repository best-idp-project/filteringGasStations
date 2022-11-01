package filteringgasstations.routing.osrm;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import filteringgasstations.database.models.OSRMCache;
import filteringgasstations.database.service.OSRMCacheService;
import filteringgasstations.routing.Route;
import filteringgasstations.stations.GasStationPair;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

public class OSRMClient {

    private static final String URL = "http://router.project-osrm.org/route/v1/driving/";

    public static Route getRoute(OSRMCacheService cache, GasStationPair pair) {
        String uri = pair.getFirstStation().getLongitude() + "," + pair.getFirstStation().getLongitude() + ";"
                + pair.getSecondStation().getLongitude() + "," + pair.getSecondStation().getLatitude() + "?overview=false";
        Optional<OSRMCache> hit = cache.get(uri);
        if (hit.isPresent()) {
            System.out.println("cache hit");
            return hit.get();
        }
        String target = URL + uri;
        System.out.println(target);
        try {
            int status = 0;
            HttpURLConnection connection;
            java.net.URL url = new URL(target);
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
            cache.save(new OSRMCache(uri, route.getDrivingTime(), route.getDrivingDistance()));
            return route;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
