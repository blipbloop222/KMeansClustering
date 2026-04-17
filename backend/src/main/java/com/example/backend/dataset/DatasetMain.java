package dataset;

public class DatasetMain {

    public static void main(String[] args) {
        
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
                    String fileName = "dataset_" + n + "points_" + d + "D_K" + k + ".csv";
                    
                    DatasetGenerator.saveToCSV(data, fileName);
                    System.out.println("Created: " + fileName);
                }
            }
        }
        
        System.out.println("All datasets generated successfully.");

        //For evalution later, use kaggle dataset
        // String rawFile = "walmart_raw.csv"; 
        
        // String cleanFile = "kaggle_rfm_clean.csv";

        // //read the raw file, calculate Recency, Frequency, Monetary, and save the clean file.
        // KagglePreprocessor.processWalmartData(rawFile, cleanFile);

        // //Load the newly created clean file to verify it worked
        // double[][] realData = DatasetLoader.loadCSV(cleanFile);
        
        // DatasetUtils.normalize(realData);

        // System.out.println("\n--- Real World Data Ready for K-Means ---");
        // DatasetUtils.printInfo(realData);
        // DatasetUtils.printSample(realData, 5);
    }
}