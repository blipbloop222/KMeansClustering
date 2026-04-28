package com.example.backend.clustering.core;

import java.util.Random;

/**
 * Core utility class for K-Means clustering algorithms.
 * Provides common utility methods and constants for clustering implementations.
 */
public final class KMeansCore {
    // Private constructor to prevent instantiation
    private KMeansCore() {}

    /**
     * Default maximum iterations for clustering algorithm.
     */
    public static final int DEFAULT_MAX_ITERATIONS = 300;

    /**
     * Default convergence tolerance.
     */
    public static final double DEFAULT_TOLERANCE = 1e-6;

    public static final long DEFAULT_SEED = 0;

    /**
     * Creates a reproducible random number generator with a given seed.
     * 
     * @param seed The seed for random number generation
     * @return A new Random instance initialized with the given seed
     */
    public static Random createReproducibleRandom(long seed) {
        return new Random(seed);
    }

    /**
     * Validates input data for clustering.
     * 
     * @param data Input data array
     * @param k Number of clusters
     * @throws IllegalArgumentException if input is invalid
     */
    public static void validateClusteringInput(double[][] data, int k) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Data must be non-empty");
        }
        if (k < 1) {
            throw new IllegalArgumentException("Number of clusters must be at least 1");
        }
        if (k > data.length) {
            throw new IllegalArgumentException("Number of clusters cannot exceed number of points");
        }

        int dimension = data[0].length;
        for (double[] row : data) {
            if (row == null || row.length != dimension) {
                throw new IllegalArgumentException("All data points must have the same dimension");
            }
        }
    }

    /**
     * Calculates squared Euclidean distance between two points.
     * 
     * @param a First point
     * @param b Second point
     * @param dimension Dimension of the points
     * @return Squared Euclidean distance
     */
    public static double squaredDistance(double[] a, double[] b, int dimension) {
        double sum = 0;
        for (int j = 0; j < dimension; j++) {
            double diff = a[j] - b[j];
            sum += diff * diff;
        }
        return sum;
    }
}