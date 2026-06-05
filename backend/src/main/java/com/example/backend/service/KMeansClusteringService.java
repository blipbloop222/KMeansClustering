package com.example.backend.service;

import com.example.backend.clustering.core.KMeansCore;
import com.example.backend.clustering.sequential.KMeansSequential;
import com.example.backend.clustering.parallel.KMeansParallel;
import com.example.backend.clustering.concurrent.KMeansConcurrent;
import com.example.backend.dataset.DatasetLoader;
import com.example.backend.utils.ErrorHandler;
import com.example.backend.web.request.KMeansClusterRequest;
import com.example.backend.web.response.KMeansClusterResponse;

import org.springframework.stereotype.Service;

@Service
public class KMeansClusteringService {
    
    private KMeansClusterResponse processClusteringRequest(KMeansClusterRequest request, ClusteringAlgorithm clusteringAlgorithm) {
        validateClusteringRequest(request);

        double[][] data = resolveData(request);

        if (request.k() > data.length) {
            ErrorHandler.handleInvalidArgument("Number of clusters cannot exceed number of points (" + data.length + ")");
        }
        
        // Extract clustering parameters with defaults
        long seed = request.seed() != null ? request.seed() : KMeansCore.DEFAULT_SEED;
        int maxIter = request.maxIterations() != null ? request.maxIterations() : KMeansCore.DEFAULT_MAX_ITERATIONS;
        double tol = request.tolerance() != null ? request.tolerance() : KMeansCore.DEFAULT_TOLERANCE;
        
        // Perform clustering
        long start = System.nanoTime();

        KMeansCore.Result result = clusteringAlgorithm.cluster(
                data, request.k(), maxIter, tol, seed, request.threads());

        long end = System.nanoTime();

        long executionTimeMs = (end - start) / 1_000_000;
        
        return new KMeansClusterResponse(
            result.centroids(),
            result.labels(),
            result.iterations(),
            result.inertia(),
            executionTimeMs,
            data.length,
            data[0].length
        );
    }
    
    /**
     * Runs K-Means clustering using sequential algorithm.
     * 
     * @param request Clustering request details
     * @return Clustering results
     */
    public KMeansClusterResponse runSequential(KMeansClusterRequest request) {
        return processClusteringRequest(request,
                (data, k, maxIter, tol, seed, threads) -> KMeansSequential.cluster(data, k, maxIter, tol, seed));
    }

    /**
     * Runs K-Means clustering using parallel algorithm.
     * 
     * @param request Clustering request details
     * @return Clustering results
     */
    public KMeansClusterResponse runParallel(KMeansClusterRequest request) {
        return processClusteringRequest(request,
                (data, k, maxIter, tol, seed, threads) -> KMeansParallel.cluster(data, k, maxIter, tol, seed, threads));
    }

    /**
     * Runs K-Means clustering using concurrent algorithm.
     * 
     * @param request Clustering request details
     * @return Clustering results
     */
    public KMeansClusterResponse runConcurrent(KMeansClusterRequest request) {
        return processClusteringRequest(request,
                (data, k, maxIter, tol, seed, threads) -> KMeansConcurrent.cluster(data, k, maxIter, tol, seed, threads));
    }
    
    /**
     * Validates the clustering request.
     * 
     * @param request Clustering request to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateClusteringRequest(KMeansClusterRequest request) {
        // Validate k
        if (request.k() < 1) {
            ErrorHandler.handleInvalidArgument("Number of clusters (k) must be at least 1");
        }
        
        // Validate max iterations
        if (request.maxIterations() != null && request.maxIterations() < 1) {
            ErrorHandler.handleInvalidArgument("Maximum iterations must be at least 1");
        }
        
        // Validate tolerance
        if (request.tolerance() != null && request.tolerance() <= 0) {
            ErrorHandler.handleInvalidArgument("Tolerance must be positive");
        }
    }
    
    private static double[][] resolveData(KMeansClusterRequest request) {
        double[][] data = DatasetLoader.loadCSV(request.datasetFilePath());
        if (data == null || data.length == 0) {
            ErrorHandler.handleInvalidArgument("Failed to load dataset from: " + request.datasetFilePath());
        }
        return data;
    }
    
    /**
     * Functional interface for clustering algorithms.
     */
    @FunctionalInterface
    private interface ClusteringAlgorithm {
        KMeansCore.Result cluster(double[][] data, int k, int maxIter, double tol, long seed, int threads);
    }
}