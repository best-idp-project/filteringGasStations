package filteringgasstations;

import com.opencsv.CSVReader;
import filteringgasstations.utils.Utils;

import java.io.FileReader;
import java.util.*;

/**
 * Aggregate in a file the number of competitors per station / day
 * Format: station lat - station lon - day1 #competitors - .... - day183 #competitors
 */
public class AggregateCompetition {
    private static final double COMPETITION_VALUE = 0.02;   // this means 2 cent/km
    private static final HashMap<String, Integer[]> stationCompetitorsPerDay = new HashMap<>();

    /**
     * Read the difference.csv file.
     */
    private static String[] readDifferenceCsv() {
        String[] header = null;
        try (CSVReader reader = new CSVReader(new FileReader("output/difference.csv"))) {
            String[] line;
            header = reader.readNext();
            while ((line = reader.readNext()) != null) {
                parseLine(line);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return header;
    }

    /**
     * Parse every entry and add to the competitor array of a station if measure exceed the threshold
     *
     * @param line a line from the difference file
     */
    private static void parseLine(String[] line) {
        for (int i = 5; i < 188; i++) {
            // if station is not yet in the hashmap (first appearance in a pair)
            if (!stationCompetitorsPerDay.containsKey(line[0])) {
                Integer[] competitorsArray = new Integer[183];
                Arrays.fill(competitorsArray, 0);
                stationCompetitorsPerDay.put(line[0], competitorsArray);
            }
            // if stations falls within the analysis
            if (Double.parseDouble(line[i]) > COMPETITION_VALUE) {
                stationCompetitorsPerDay.get(line[0])[i - 5]++;
            }
        }
    }

    /**
     * Format the hashmap to obtain a list of string to print on csv
     *
     * @return list of string for the file out
     */
    private static List<String> competitionToListOfString() {
        List<String> output = new ArrayList<>();
        for (String station : stationCompetitorsPerDay.keySet()) {
            String latLon = matchWithGasStationsFile(station);
            output.add(latLon + "," + Arrays.toString(stationCompetitorsPerDay.get(station)).replace("[", " ").replace("]", " "));
        }
        return output;
    }

    private static void printAggregatedCompetitors(String[] header) {
        header = fixHeader(header);
        Utils.writeCSV("output/aggregatedCompetition" + COMPETITION_VALUE + ".csv", header, competitionToListOfString());
    }

    /**
     * Helper method to fix array
     *
     * @param header header of difference file
     * @return header of the new file to print
     */
    private static String[] fixHeader(String[] header) {
        List<String> list = new ArrayList<>(Arrays.asList(header));
        Collection<String> collection = new ArrayList<>(Arrays.asList("station1", "country1", "station2", "country2", "distance"));
        list.removeAll(collection);
        list.add(0, "lat");
        list.add(1, "lon");
        header = list.toArray(new String[]{});
        return header;
    }

    /**
     * Match the ids of station with their coordinates (needed to represent the results on a map)
     *
     * @param station id of the station
     * @return lat, lon for of the station
     */
    private static String matchWithGasStationsFile(String station) {
        try (CSVReader reader = new CSVReader(new FileReader("output/gasStationsRange20.csv"))) {
            String[] line;
            reader.readNext();
            while ((line = reader.readNext()) != null) {
                if (Objects.equals(line[0], station))
                    return (line[1] + "," + line[2]);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void run() {
        String[] header = readDifferenceCsv();
        printAggregatedCompetitors(header);
    }

}

