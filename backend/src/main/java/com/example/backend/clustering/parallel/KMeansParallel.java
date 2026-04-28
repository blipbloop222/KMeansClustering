package com.example.backend.clustering.parallel;

import com.example.backend.clustering.sequential.KMeansSequential;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class KMeansParallel {
    /**
     * Performs K-Means clustering using a parallel approach.
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
        
        // Initial validation
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("data must be non-empty");
        }
        if (k < 1) {
            throw new IllegalArgumentException("k must be at least 1");
        }
        int n = data.length;
        int d = data[0].length;
        if (k > n) {
            throw new IllegalArgumentException("k cannot exceed number of points");
        }

        // Validate data consistency
        for (double[] row : data) {
            if (row == null || row.length != d) {
                throw new IllegalArgumentException("all rows must have the same dimension");
            }
        }

        Random rnd = new Random(seed);
        double[][] centroids = initializeCentroids(data, k, rnd);
        int[] labels = new int[n];

        int iter = 0;
        boolean converged = false;

        for (; iter < maxIter && !converged; iter++) {
            // Parallel label assignment
            assignLabelsParallel(data, n, d, k, centroids, labels);

            // Compute new centroids
            double[][] newCentroids = new double[k][d];
            int[] counts = new int[k];
            accumulateCentroidsParallel(data, n, d, labels, newCentroids, counts);
            
            repairEmptyClusters(data, n, d, k, rnd, newCentroids, counts);
            finalizeCentroids(newCentroids, counts, d);

            converged = centroidsMovedLessThan(centroids, newCentroids, k, d, tol);
            centroids = newCentroids;
        }

        assignLabelsParallel(data, n, d, k, centroids, labels);
        double inertia = computeInertia(data, n, d, centroids, labels);

        return new KMeansSequential.Result(centroids, labels, iter, inertia);
    }

    /**
     * Initialize centroids using a similar approach to sequential method.
     */
    private static double[][] initializeCentroids(double[][] data, int k, Random rnd) {
        Set<Integer> picked = new HashSet<>();
        while (picked.size() < k) {
            picked.add(rnd.nextInt(data.length));
        }
        List<Integer> order = new ArrayList<>(picked);
        Collections.sort(order);
        
        double[][] centroids = new double[k][data[0].length];
        for (int c = 0; c < k; c++) {
            int idx = order.get(c);
            System.arraycopy(data[idx], 0, centroids[c], 0, data[0].length);
        }
        return centroids;
    }

    /**
     * Parallel label assignment.
     */
    private static void assignLabelsParallel(
            double[][] data, int n, int d, int k, 
            double[][] centroids, int[] labels) {
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        int chunkSize = n / numThreads;
        
        for (int t = 0; t < numThreads; t++) {
            final int start = t * chunkSize;
            final int end = (t == numThreads - 1) ? n : start + chunkSize;
            
            executor.submit(() -> {
                for (int i = start; i < end; i++) {
                    int best = 0;
                    double bestDist = squaredDist(data[i], centroids[0], d);
                    for (int c = 1; c < k; c++) {
                        double dist = squaredDist(data[i], centroids[c], d);
                        if (dist < bestDist) {
                            bestDist = dist;
                            best = c;
                        }
                    }
                    labels[i] = best;
                }
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Parallel label assignment interrupted", e);
        }
    }

    /**
     * Parallel centroid accumulation.
     */
    private static void accumulateCentroidsParallel(
            double[][] data, int n, int d, int[] labels, 
            double[][] sums, int[] counts) {
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        int chunkSize = n / numThreads;
        
        for (int t = 0; t < numThreads; t++) {
            final int start = t * chunkSize;
            final int end = (t == numThreads - 1) ? n : start + chunkSize;
            
            executor.submit(() -> {
                double[][] localSums = new double[sums.length][d];
                int[] localCounts = new int[sums.length];
                
                for (int i = start; i < end; i++) {
                    int c = labels[i];
                    localCounts[c]++;
                    for (int j = 0; j < d; j++) {
                        localSums[c][j] += data[i][j];
                    }
                }
                
                // Synchronize results
                synchronized (sums) {
                    for (int c = 0; c < sums.length; c++) {
                        counts[c] += localCounts[c];
                        for (int j = 0; j < d; j++) {
                            sums[c][j] += localSums[c][j];
                        }
                    }
                }
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Parallel centroid accumulation interrupted", e);
        }
    }

    /**
     * Repair empty clusters.
     */
    private static void repairEmptyClusters(
            double[][] data, int n, int d, int k, Random rnd, 
            double[][] sums, int[] counts) {
        for (int c = 0; c < k; c++) {
            if (counts[c] == 0) {
                int idx = rnd.nextInt(n);
                System.arraycopy(data[idx], 0, sums[c], 0, d);
                counts[c] = 1;
            }
        }
    }

    /**
     * Finalize centroids by dividing by counts.
     */
    private static void finalizeCentroids(double[][] sums, int[] counts, int d) {
        for (int c = 0; c < sums.length; c++) {
            int cnt = counts[c];
            for (int j = 0; j < d; j++) {
                sums[c][j] /= cnt;
            }
        }
    }

    /**
     * Check if centroids have converged.
     */
    private static boolean centroidsMovedLessThan(
            double[][] prev, double[][] next, int k, int d, double tol) {
        double tolSq = tol * tol;
        for (int c = 0; c < k; c++) {
            if (squaredDist(prev[c], next[c], d) > tolSq) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compute squared distance.
     */
    private static double squaredDist(double[] a, double[] b, int d) {
        double s = 0;
        for (int j = 0; j < d; j++) {
            double t = a[j] - b[j];
            s += t * t;
        }
        return s;
    }

    /**
     * Compute inertia.
     */
    private static double computeInertia(
            double[][] data, int n, int d, double[][] centroids, int[] labels) {
        double sum = 0;
        for (int i = 0; i < n; i++) {
            sum += squaredDist(data[i], centroids[labels[i]], d);
        }
        return sum;
    }
}