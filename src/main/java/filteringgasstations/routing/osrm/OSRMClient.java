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

/**
 * OSMR http://project-osrm.org/
 * Routing engine for shortest paths in road network
 */
public class OSRMClient {

    private static final String URL = "http://router.project-osrm.org/route/v1/driving/";

    /**
     * Method that calls the routing API to get driving time and distances, if these info are not already cached
     *
     * @param cache to check if the route is already cached
     * @param pair  which we want to calculate distances
     * @return the route that connects the two station
     */
    public static Route getRoute(OSRMCacheService cache, GasStationPair pair) {
        String uri = pair.getFirstStation().getLongitude() + "," + pair.getFirstStation().getLatitude() + ";"
                + pair.getSecondStation().getLongitude() + "," + pair.getSecondStation().getLatitude();
        Optional<OSRMCache> hit = cache.get(uri);
        if (hit.isPresent()) {
            return hit.get();
        }
        String target = URL + uri + "?overview=false";
        System.out.println(target);
        try {
            int status;
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
