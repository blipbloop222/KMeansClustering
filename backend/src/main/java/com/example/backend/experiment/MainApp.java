package com.example.backend.experiment;

import com.example.backend.clustering.sequential.KMeansSequential;
import com.example.backend.clustering.parallel.KMeansParallel;
import com.example.backend.dataset.DatasetGenerator;
import java.util.Arrays;

public class MainApp {
    public static void main(String[] args) {
        // Generate a clustered dataset
        double[][] data = DatasetGenerator.generateClustered(1000, 2, 3, 42L);
        
        // Demonstrate Sequential Clustering
        System.out.println("Sequential K-Means Clustering:");
        KMeansSequential.Result seqResult = KMeansSequential.cluster(data, 3, 300, 1e-6, 42L);
        printClusteringResults(seqResult, "Sequential");
        
        // Demonstrate Parallel Clustering
        System.out.println("\nParallel K-Means Clustering:");
        KMeansSequential.Result parallelResult = KMeansParallel.cluster(data, 3, 300, 1e-6, 42L);
        printClusteringResults(parallelResult, "Parallel");
    }
    
    private static void printClusteringResults(KMeansSequential.Result result, String type) {
        System.out.println("Clustering Algorithm: " + type);
        System.out.println("Number of Iterations: " + result.iterations());
        System.out.println("Inertia: " + result.inertia());
        
        System.out.println("\nCentroids:");
        for (int i = 0; i < result.centroids().length; i++) {
            System.out.println("Cluster " + i + ": " + Arrays.toString(result.centroids()[i]));
        }
        
        // Count points in each cluster
        int[] clusterCounts = new int[result.centroids().length];
        for (int label : result.labels()) {
            clusterCounts[label]++;
        }
        
        System.out.println("\nCluster Sizes:");
        for (int i = 0; i < clusterCounts.length; i++) {
            System.out.println("Cluster " + i + ": " + clusterCounts[i] + " points");
        }
    }
}