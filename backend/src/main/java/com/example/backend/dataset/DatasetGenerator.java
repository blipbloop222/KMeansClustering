package dataset;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class DatasetGenerator {

    //generate random dataset
    public static double[][] generateRandom(int n, int d, long seed) {
        Random r = new Random(seed);
        double[][] data = new double[n][d];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < d; j++) {
                data[i][j] = r.nextDouble() * 1000;
            }
        }

        return data;
    }

    //generate clustered dataset
    public static double[][] generateClustered(int n, int d, int k, long seed) {
        Random r = new Random(seed);
        double[][] data = new double[n][d];

        //create centroids
        double[][] centroids = new double[k][d];
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < d; j++) {
                centroids[i][j] = r.nextDouble() * 1000;
            }
        }

        //generate points around centroids
        for (int i = 0; i < n; i++) {
            int cluster = rand.nextInt(k);

            for (int j = 0; j < d; j++) {
                data[i][j] = centroids[cluster][j] + rand.nextGaussian() * 50;
            }
        }

        return data;
    }

    public static void saveToCSV(double[][] data, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {

            int d = data[0].length;

            //header
            for (int i = 0; i < d; i++) {
                writer.append("Feature").append(String.valueOf(i));
                if (i < d - 1) writer.append(",");
            }
            writer.append("\n");

            for (double[] row : data) {
                for (int i = 0; i < d; i++) {
                    writer.append(String.valueOf(row[i]));
                    if (i < d - 1) writer.append(",");
                }
                writer.append("\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
