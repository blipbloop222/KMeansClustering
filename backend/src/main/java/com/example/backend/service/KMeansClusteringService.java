package com.example.backend.service;

import com.example.backend.clustering.core.KMeansCore;
import com.example.backend.clustering.sequential.KMeansSequential;
import com.example.backend.clustering.parallel.KMeansParallel;
import com.example.backend.clustering.concurrent.KMeansConcurrent;
import com.example.backend.dataset.DatasetGenerator;
import com.example.backend.utils.ErrorHandler;
import com.example.backend.web.request.GenerateDatasetSpec;
import com.example.backend.web.request.KMeansClusterRequest;
import com.example.backend.web.response.KMeansClusterResponse;

import org.springframework.stereotype.Service;

@Service
public class KMeansClusteringService {
    
    private KMeansClusterResponse processClusteringRequest(KMeansClusterRequest request, ClusteringAlgorithm clusteringAlgorithm) {
        // Validate input parameters
        validateClusteringRequest(request);
        
        // Resolve data points
        double[][] data = resolveData(request);
        
        // Extract clustering parameters with defaults
        long seed = request.seed() != null ? request.seed() : KMeansCore.DEFAULT_SEED;
        int maxIter = request.maxIterations() != null ? request.maxIterations() : KMeansCore.DEFAULT_MAX_ITERATIONS;
        double tol = request.tolerance() != null ? request.tolerance() : KMeansCore.DEFAULT_TOLERANCE;
        
        // Perform clustering
        long start = System.nanoTime();

        KMeansSequential.Result result = clusteringAlgorithm.cluster(data, request.k(), maxIter, tol, seed);

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
        return processClusteringRequest(request, KMeansSequential::cluster);
    }
    
    /**
     * Runs K-Means clustering using parallel algorithm.
     * 
     * @param request Clustering request details
     * @return Clustering results
     */
    public KMeansClusterResponse runParallel(KMeansClusterRequest request) {
        return processClusteringRequest(request, KMeansParallel::cluster);
    }
    
    /**
     * Runs K-Means clustering using concurrent algorithm.
     * 
     * @param request Clustering request details
     * @return Clustering results
     */
    public KMeansClusterResponse runConcurrent(KMeansClusterRequest request) {
        return processClusteringRequest(request, KMeansConcurrent::cluster);
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
    
    /**
     * Resolves the input data points for clustering.
     * 
     * @param request Clustering request
     * @return Resolved data points
     * @throws IllegalArgumentException if data resolution fails
     */
    private static double[][] resolveData(KMeansClusterRequest request) {
        boolean hasPoints = request.points() != null && request.points().length > 0;
        boolean hasGenerate = request.generate() != null;
        
        // Validate data source
        if (hasPoints && hasGenerate) {
            ErrorHandler.handleInvalidArgument("Provide only one of \"points\" or \"generate\"");
        }
        if (!hasPoints && !hasGenerate) {
            ErrorHandler.handleInvalidArgument("Provide either \"points\" or \"generate\"");
        }
        
        // Generate dataset if requested
        if (hasGenerate) {
            GenerateDatasetSpec g = request.generate();
            
            // Validate generation parameters
            if (g.n() < 1) {
                ErrorHandler.handleInvalidArgument("generate.n must be at least 1");
            }
            if (g.dimensions() < 1) {
                ErrorHandler.handleInvalidArgument("generate.dimensions must be at least 1");
            }
            if (g.generatorClusters() < 1) {
                ErrorHandler.handleInvalidArgument("generate.generatorClusters must be at least 1");
            }
            
            return DatasetGenerator.generateClustered(
                g.n(), 
                g.dimensions(), 
                g.generatorClusters(), 
                request.seed() != null ? request.seed() : KMeansCore.DEFAULT_SEED
            );
        }
        
        return request.points();
    }
    
    /**
     * Functional interface for clustering algorithms.
     */
    @FunctionalInterface
    private interface ClusteringAlgorithm {
        KMeansSequential.Result cluster(double[][] data, int k, int maxIter, double tol, long seed);
    }
}