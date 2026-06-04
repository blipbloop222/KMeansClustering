package com.example.backend.web.request;

import com.example.backend.clustering.core.KMeansCore;
import com.example.backend.utils.ErrorHandler;

/**
 * Represents a request for K-Means clustering.
 *
 * @param k Number of clusters
 * @param seed Random seed for reproducibility (optional)
 * @param maxIterations Maximum number of iterations (optional, defaults to standard value)
 * @param tolerance Convergence tolerance (optional, defaults to standard value)
 * @param datasetFilePath Path to the CSV dataset file to load
 * @param threads Thread count for parallel/concurrent (optional, defaults to available processors)
 */
public record KMeansClusterRequest(
        int k,
        Long seed,
        Integer maxIterations,
        Double tolerance,
        String datasetFilePath,
        Integer threads) {

    public KMeansClusterRequest {
        if (k < 1) {
            ErrorHandler.handleInvalidArgument("Number of clusters (k) must be at least 1");
        }
        if (datasetFilePath == null || datasetFilePath.isBlank()) {
            ErrorHandler.handleInvalidArgument("datasetFilePath must be provided");
        }
        if (threads != null && threads < 1) {
            ErrorHandler.handleInvalidArgument("threads must be at least 1");
        }

        seed = seed != null ? seed : System.currentTimeMillis();
        maxIterations = maxIterations != null ? maxIterations : KMeansCore.DEFAULT_MAX_ITERATIONS;
        tolerance = tolerance != null ? tolerance : KMeansCore.DEFAULT_TOLERANCE;
        threads = threads != null ? threads : Runtime.getRuntime().availableProcessors();
    }

    public KMeansClusterRequest withSeed(long newSeed) {
        return new KMeansClusterRequest(k, newSeed, maxIterations, tolerance, datasetFilePath, threads);
    }
}