package com.example.backend.web.request;

import com.example.backend.clustering.core.KMeansCore;
import com.example.backend.utils.ErrorHandler;

import java.util.Objects;

/**
 * Represents a request for K-Means clustering with comprehensive validation.
 * Ensures all input parameters meet the required constraints before clustering.
 *
 * @param k Number of clusters
 * @param seed Random seed for reproducibility (optional)
 * @param maxIterations Maximum number of iterations (optional, defaults to standard value)
 * @param tolerance Convergence tolerance (optional, defaults to standard value)
 * @param points Input data points for clustering
 * @param generate Optional dataset generation specification
 */
public record KMeansClusterRequest(
        int k,
        Long seed,
        Integer maxIterations,
        Double tolerance,
        double[][] points,
        GenerateDatasetSpec generate) {

    /**
     * Canonical constructor with comprehensive input validation.
     */
    public KMeansClusterRequest {
        // Validate k
        if (k < 1) {
            ErrorHandler.handleInvalidArgument("Number of clusters (k) must be at least 1");
        }

        // Validate points if no dataset generation is specified
        if (generate == null) {
            validatePoints(points);
        }

        // Set default values if not provided
        seed = seed != null ? seed : System.currentTimeMillis();
        maxIterations = maxIterations != null ? 
            maxIterations : KMeansCore.DEFAULT_MAX_ITERATIONS;
        tolerance = tolerance != null ? 
            tolerance : KMeansCore.DEFAULT_TOLERANCE;
    }

    /**
     * Validates input points for clustering.
     * 
     * @param inputPoints Data points to validate
     * @throws IllegalArgumentException if points are invalid
     */
    private void validatePoints(double[][] inputPoints) {
        if (inputPoints == null || inputPoints.length == 0) {
            ErrorHandler.handleInvalidArgument("Input points cannot be null or empty");
        }

        // Ensure all points have the same dimension
        int dimension = inputPoints[0].length;
        for (double[] point : inputPoints) {
            if (point == null || point.length != dimension) {
                ErrorHandler.handleInvalidArgument("All points must have the same dimension");
            }
            
            // Optional: Check for NaN or infinite values
            for (double value : point) {
                if (Double.isNaN(value) || Double.isInfinite(value)) {
                    ErrorHandler.handleInvalidArgument("Points cannot contain NaN or infinite values");
                }
            }
        }

        // Validate k does not exceed number of points
        if (k > inputPoints.length) {
            ErrorHandler.handleInvalidArgument("Number of clusters cannot exceed number of points");
        }
    }

    /**
     * Generates a new instance with a specific seed.
     * 
     * @param newSeed New seed for random number generation
     * @return A new KMeansClusterRequest with the specified seed
     */
    public KMeansClusterRequest withSeed(long newSeed) {
        return new KMeansClusterRequest(k, newSeed, maxIterations, tolerance, points, generate);
    }
}