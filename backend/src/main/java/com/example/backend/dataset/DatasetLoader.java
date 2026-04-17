package dataset;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatasetLoader {

    public static double[][] loadCSV(String fileName) {
        List<double[]> dataList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

            //skip header
            String line = br.readLine();

            //read actual data
            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) 
                    continue;

                String[] values = line.split(",");
                double[] row = new double[values.length];

                for (int i = 0; i < values.length; i++) {
                    row[i] = Double.parseDouble(values[i].trim());
                }

                dataList.add(row);
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format: " + e.getMessage());
        }

        return dataList.toArray(new double[0][]);
    }
}