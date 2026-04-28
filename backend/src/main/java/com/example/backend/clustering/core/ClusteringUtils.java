package com.example.backend.clustering.core;

import java.util.Random;

/**
 * Utility class for common clustering operations and helper methods.
 * Provides standardized methods for clustering-related tasks.
 */
public final class ClusteringUtils {
    // Private constructor to prevent instantiation
    private ClusteringUtils() {}

    /**
     * Performs k-means++ centroid initialization.
     * 
     * @param data Input data points
     * @param k Number of clusters
     * @param rnd Random number generator
     * @return Initialized centroids
     */
    public static double[][] initializeKMeansPlusPlusCentroids(double[][] data, int k, Random rnd) {
        KMeansCore.validateClusteringInput(data, k);
        
        int n = data.length;
        int d = data[0].length;
        double[][] centroids = new double[k][d];
        
        // First centroid is randomly selected
        int firstCentroidIndex = rnd.nextInt(n);
        System.arraycopy(data[firstCentroidIndex], 0, centroids[0], 0, d);
        
        // Probabilistic selection of remaining centroids
        for (int c = 1; c < k; c++) {
            double[] distances = computeMinDistances(data, centroids, c, d);
            int nextCentroidIndex = selectNextCentroidIndex(distances, rnd);
            System.arraycopy(data[nextCentroidIndex], 0, centroids[c], 0, d);
        }
        
        return centroids;
    }

    /**
     * Computes minimum distances for k-means++ initialization.
     * 
     * @param data Input data points
     * @param centroids Current centroids
     * @param currentCentroidCount Number of centroids already selected
     * @param dimension Data point dimension
     * @return Array of minimum distances
     */
    private static double[] computeMinDistances(double[][] data, double[][] centroids, 
                                                int currentCentroidCount, int dimension) {
        double[] minDistances = new double[data.length];
        
        for (int i = 0; i < data.length; i++) {
            double minDist = Double.MAX_VALUE;
            for (int j = 0; j < currentCentroidCount; j++) {
                double dist = KMeansCore.squaredDistance(data[i], centroids[j], dimension);
                minDist = Math.min(minDist, dist);
            }
            minDistances[i] = minDist;
        }
        
        return minDistances;
    }

    /**
     * Selects the next centroid index based on weighted probability.
     * 
     * @param distances Distances array
     * @param rnd Random number generator
     * @return Selected centroid index
     */
    private static int selectNextCentroidIndex(double[] distances, Random rnd) {
        double totalDistance = 0;
        for (double dist : distances) {
            totalDistance += dist;
        }
        
        double randomValue = rnd.nextDouble() * totalDistance;
        double cumulativeDistance = 0;
        
        for (int i = 0; i < distances.length; i++) {
            cumulativeDistance += distances[i];
            if (cumulativeDistance >= randomValue) {
                return i;
            }
        }
        
        // Fallback (should rarely happen)
        return distances.length - 1;
    }
}