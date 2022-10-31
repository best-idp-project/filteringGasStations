package filteringgasstations;

import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FilterGasPricesDataset {

    public static void main(String[] args) {
        filterDatasetWithGermanStations();
    }

    public static List<String> germanStations() {
        List<String> germanStations = new ArrayList<>();
        var file = ClassLoader.getSystemClassLoader().getResource("germanStations20Km.csv");

        try {
            assert file != null;
            try (CSVReader reader = new CSVReader(new FileReader(file.getPath()))) {
                String[] lineInArray;
                while ((lineInArray = reader.readNext()) != null) {
                    germanStations.add(lineInArray[0]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("German stations correctly imported");
        return germanStations;
    }

    public static void filterDatasetWithGermanStations() {
        List<String> germanStations = germanStations();

        for (int month = 4; month <= 10; month++) {
            for (int day = 1; day <= 31; day++) {
                List<String> filteredDataset = new ArrayList<>();
                String fileName = "prices/germany/" + month + "/2022-" + String.format("%02d", month) + "-" + String.format("%02d", day) + "-prices.csv";
                var file = ClassLoader.getSystemClassLoader().getResource(fileName);
                if (file == null) continue;
                try {
                    try (CSVReader reader = new CSVReader(new FileReader(file.getPath()))) {
                        String[] lineInArray;
                        // add header
                        filteredDataset.add(String.join(",", reader.readNext()));
                        // add entries only in case of a match
                        while ((lineInArray = reader.readNext()) != null) {
                            if (germanStations.contains(lineInArray[1])) {
                                // remove outliers (in the raw file there are 0 and neg values)
                                if (Double.parseDouble(lineInArray[2]) > 1)
                                    filteredDataset.add(String.join(",", lineInArray));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                writeValidEntriesToFile(filteredDataset, fileName);
            }
        }
        System.out.println("All prices dataset are now filtered");

    }

    public static void writeValidEntriesToFile(List<String> filteredDataset, String fileName) {

        File filecsv = new File("output/germany/");
        if (!filecsv.exists()) {
            var _bool = filecsv.mkdir();
        }

        try {
            filecsv = new File("output/" + fileName);
            FileWriter fwcsv = new FileWriter(filecsv);

            for (String entry : filteredDataset) {
                fwcsv.append(entry).append("\n");
            }
            fwcsv.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Output produced: filtered datasets");
    }
}
