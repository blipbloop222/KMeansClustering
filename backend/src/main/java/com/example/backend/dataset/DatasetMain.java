package com.example.backend.dataset;

import java.io.File;

public class DatasetMain {

    private static final String OUTPUT_DIR = "../datasets";

    public static void main(String[] args) {

        new File(OUTPUT_DIR).mkdirs();

        //the sizes, dimensions, and K values requested by question
        int[] sizes = {10000, 100000, 1000000};
        int[] dimensions = {3, 5, 10};
        int[] kValues = {5, 10};

        System.out.println("Generating datasets batch...");

        //loop through and generate files for different scenarios
        for (int n : sizes) {
            for (int d : dimensions) {
                for (int k : kValues) {
                    double[][] data = DatasetGenerator.generateClustered(n, d, k, 42);
                    String fileName = OUTPUT_DIR + "/dataset_" + n + "points_" + d + "D_K" + k + ".csv";

                    DatasetGenerator.saveToCSV(data, fileName);
                    System.out.println("Created: " + fileName);
                }
            }
        }
        
        System.out.println("All datasets generated successfully.");

        // --- Walmart real-world dataset (RFM) ---
        String rawFile   = OUTPUT_DIR + "/walmart_raw.csv";
        String cleanFile = OUTPUT_DIR + "/walmart_rfm_clean.csv";

        System.out.println("\nPreprocessing Walmart dataset into RFM features...");
        KagglePreprocessor.processWalmartData(rawFile, cleanFile);

        // Verify the clean file loaded correctly
        double[][] realData = DatasetLoader.loadCSV(cleanFile);
        if (realData != null && realData.length > 0) {
            System.out.println("\n--- Walmart RFM Dataset Ready ---");
            DatasetUtils.printInfo(realData);
            DatasetUtils.printSample(realData, 5);
        }
    }
}