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
import java.util.concurrent.Future;

public final class KMeansParallel {

    private KMeansParallel() {}

    /**
     * Performs K-Means clustering using a parallel approach.
     * Creates and shuts down its own thread pool (used by the REST service layer).
     */
    public static KMeansSequential.Result cluster(double[][] data, int k, int maxIter, double tol, long seed) {
        return cluster(data, k, maxIter, tol, seed, Runtime.getRuntime().availableProcessors());
    }

    /**
     * Creates a fixed thread pool of {@code numThreads}, runs clustering, then shuts the pool down.
     * Use this when you need a one-shot call and don't manage pool lifecycle yourself.
     */
    public static KMeansSequential.Result cluster(double[][] data, int k, int maxIter, double tol, long seed, int numThreads) {
        if (numThreads < 1) throw new IllegalArgumentException("numThreads must be at least 1");
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        try {
            return cluster(data, k, maxIter, tol, seed, executor);
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Runs clustering on a caller-supplied {@link ExecutorService}.
     * The pool is NOT shut down — the caller owns its lifecycle.
     * Use this in benchmarks to reuse one pool across many dataset runs.
     */
    public static KMeansSequential.Result cluster(double[][] data, int k, int maxIter, double tol, long seed,
                                                   ExecutorService executor) {
        // Validation
        if (data == null || data.length == 0)  throw new IllegalArgumentException("data must be non-empty");
        if (k < 1)                             throw new IllegalArgumentException("k must be at least 1");
        if (executor == null)                  throw new IllegalArgumentException("executor must not be null");

        int n = data.length;
        int d = data[0].length;

        if (k > n) throw new IllegalArgumentException("k cannot exceed number of points");
        for (double[] row : data) {
            if (row == null || row.length != d)
                throw new IllegalArgumentException("all rows must have the same dimension");
        }

        // Derive the effective thread count from the pool so chunk sizes stay correct.
        // For a fixed-thread pool this equals numThreads; for other pool types it is a safe fallback.
        int numThreads = (executor instanceof java.util.concurrent.ThreadPoolExecutor tpe)
                ? (int) tpe.getMaximumPoolSize()
                : Runtime.getRuntime().availableProcessors();

        Random rnd        = new Random(seed);
        double[][] centroids = initializeCentroids(data, k, rnd);
        int[] labels      = new int[n];
        int iter          = 0;
        boolean converged = false;

        while (iter < maxIter && !converged) {
            assignLabelsParallel(executor, numThreads, data, n, d, k, centroids, labels);

            double[][] newCentroids = new double[k][d];
            int[] counts            = new int[k];
            accumulateCentroidsParallel(executor, numThreads, data, n, d, labels, newCentroids, counts);
            repairEmptyClusters(data, n, d, k, rnd, newCentroids, counts);
            finalizeCentroids(newCentroids, counts, d);

            converged = centroidsMovedLessThan(centroids, newCentroids, k, d, tol);
            centroids = newCentroids;
            iter++;
        }

        assignLabelsParallel(executor, numThreads, data, n, d, k, centroids, labels);
        double inertia = computeInertia(data, n, d, centroids, labels);
        return new KMeansSequential.Result(centroids, labels, iter, inertia);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static double[][] initializeCentroids(double[][] data, int k, Random rnd) {
        Set<Integer> picked = new HashSet<>();
        while (picked.size() < k) picked.add(rnd.nextInt(data.length));
        List<Integer> order = new ArrayList<>(picked);
        Collections.sort(order);

        double[][] centroids = new double[k][data[0].length];
        for (int c = 0; c < k; c++)
            System.arraycopy(data[order.get(c)], 0, centroids[c], 0, data[0].length);
        return centroids;
    }

    private static void assignLabelsParallel(ExecutorService executor, int numThreads,
                                              double[][] data, int n, int d, int k,
                                              double[][] centroids, int[] labels) {
        int chunkSize = n / numThreads;
        List<Future<?>> futures = new ArrayList<>();

        for (int t = 0; t < numThreads; t++) {
            final int start = t * chunkSize;
            final int end   = (t == numThreads - 1) ? n : start + chunkSize;
            if (start >= end) continue;

            futures.add(executor.submit(() -> {
                for (int i = start; i < end; i++) {
                    int best = 0;
                    double bestDist = squaredDist(data[i], centroids[0], d);
                    for (int c = 1; c < k; c++) {
                        double dist = squaredDist(data[i], centroids[c], d);
                        if (dist < bestDist) { bestDist = dist; best = c; }
                    }
                    labels[i] = best;
                }
            }));
        }
        waitForTasks(futures);
    }

    private static void accumulateCentroidsParallel(ExecutorService executor, int numThreads,
                                                     double[][] data, int n, int d, int[] labels,
                                                     double[][] sums, int[] counts) {
        int chunkSize = n / numThreads;
        List<Future<?>> futures = new ArrayList<>();

        for (int t = 0; t < numThreads; t++) {
            final int start = t * chunkSize;
            final int end   = (t == numThreads - 1) ? n : start + chunkSize;
            if (start >= end) continue;

            futures.add(executor.submit(() -> {
                double[][] localSums   = new double[sums.length][d];
                int[]      localCounts = new int[sums.length];

                for (int i = start; i < end; i++) {
                    int c = labels[i];
                    localCounts[c]++;
                    for (int j = 0; j < d; j++) localSums[c][j] += data[i][j];
                }

                synchronized (sums) {
                    for (int c = 0; c < sums.length; c++) {
                        counts[c] += localCounts[c];
                        for (int j = 0; j < d; j++) sums[c][j] += localSums[c][j];
                    }
                }
            }));
        }
        waitForTasks(futures);
    }

    private static void waitForTasks(List<Future<?>> futures) {
        try {
            for (Future<?> f : futures) f.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Parallel execution interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("Parallel execution failed", e);
        }
    }

    private static void repairEmptyClusters(double[][] data, int n, int d, int k,
                                             Random rnd, double[][] sums, int[] counts) {
        for (int c = 0; c < k; c++) {
            if (counts[c] == 0) {
                int idx = rnd.nextInt(n);
                System.arraycopy(data[idx], 0, sums[c], 0, d);
                counts[c] = 1;
            }
        }
    }

    private static void finalizeCentroids(double[][] sums, int[] counts, int d) {
        for (int c = 0; c < sums.length; c++) {
            int cnt = counts[c];
            for (int j = 0; j < d; j++) sums[c][j] /= cnt;
        }
    }

    private static boolean centroidsMovedLessThan(double[][] prev, double[][] next, int k, int d, double tol) {
        double tolSq = tol * tol;
        for (int c = 0; c < k; c++)
            if (squaredDist(prev[c], next[c], d) > tolSq) return false;
        return true;
    }

    private static double squaredDist(double[] a, double[] b, int d) {
        double sum = 0;
        for (int j = 0; j < d; j++) { double diff = a[j] - b[j]; sum += diff * diff; }
        return sum;
    }

    private static double computeInertia(double[][] data, int n, int d, double[][] centroids, int[] labels) {
        double sum = 0;
        for (int i = 0; i < n; i++) sum += squaredDist(data[i], centroids[labels[i]], d);
        return sum;
    }
}
