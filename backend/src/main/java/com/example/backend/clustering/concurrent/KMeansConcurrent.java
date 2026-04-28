package com.example.backend.clustering.concurrent;

import com.example.backend.clustering.sequential.KMeansSequential;

public final class KMeansConcurrent {
    /**
     * Performs K-Means clustering using a concurrent approach.
     * 
     * @param data Input data points
     * @param k Number of clusters
     * @param maxIter Maximum number of iterations
     * @param tol Convergence tolerance
     * @param seed Random seed
     * @return Clustering result with centroids, labels, iterations, and inertia
     */
    public static KMeansSequential.Result cluster(
            double[][] data, 
            int k, 
            int maxIter, 
            double tol, 
            long seed) {
        
        // For now, fallback to sequential implementation
        return KMeansSequential.cluster(data, k, maxIter, tol, seed);
    }
}