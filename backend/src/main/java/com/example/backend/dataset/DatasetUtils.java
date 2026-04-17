package dataset;

public class DatasetUtils {

    public static void printInfo(double[][] data) {
        System.out.println("Number of points: " + data.length);
        System.out.println("Dimensions: " + data[0].length);
    }

    public static void printSample(double[][] data, int limit) {
        for (int i = 0; i < Math.min(limit, data.length); i++) {
            System.out.print("Point " + i + ": ");
            for (double val : data[i]) {
                System.out.print(val + " ");
            }
            System.out.println();
        }
    }

    public static void normalize(double[][] data) {
        if (data == null || data.length == 0) return;

        int d = data[0].length;

        for (int j = 0; j < d; j++) {
            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;

            //find min & max for each column
            for (double[] row : data) {
                if (row[j] < min) min = row[j];
                if (row[j] > max) max = row[j];
            }

            double range = max - min;

            //normalize column
            for (double[] row : data) {
                if (range > 0) {
                    row[j] = (row[j] - min) / range;
                } else {
                    row[j] = 0; // edge case: all values same
                }
            }
        }

        System.out.println("Dataset normalized successfully.");
    }
}