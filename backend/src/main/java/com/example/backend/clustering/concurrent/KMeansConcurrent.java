package com.example.backend.clustering.concurrent;

import com.example.backend.clustering.core.KMeansCore;
import com.example.backend.clustering.sequential.KMeansSequential;

import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public final class KMeansConcurrent {
    /**
     * Performs K-Means clustering using a concurrent approach. It uses manual thread creation and a
     * {@link CyclicBarrier} for inter-thread coordination.
     *
     * @param data Input data points
     * @param k Number of clusters
     * @param maxIter Maximum number of iterations
     * @param tol Convergence tolerance
     * @param seed Random seed
     * @return Clustering result with centroids, labels, iterations, and inertia
     */
    public static KMeansSequential.Result cluster(double[][] data, int k, int maxIter, double tol, long seed) {
        // ------------------------------------------------------------------
        // Validation
        // ------------------------------------------------------------------
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("data must be non-empty");
        }
        if (k < 1) {
            throw new IllegalArgumentException("k must be at least 1");
        }
        final int n = data.length;
        final int d = data[0].length;
        if (k > n) {
            throw new IllegalArgumentException("k cannot exceed number of points");
        }
        for (double[] row : data) {
            if (row == null || row.length != d) {
                throw new IllegalArgumentException("all rows must have the same dimension");
            }
        }

        // ------------------------------------------------------------------
        // Initialisation
        // ------------------------------------------------------------------
        Random rnd = new Random(seed);
        double[][] centroids = initializeCentroids(data, k, rnd);
        final int[] labels = new int[n];

        final int numThreads = Runtime.getRuntime().availableProcessors();

        // Synchronized state updated by the barrier and safely read by workers in the subsequent phase.
        final SharedState state = new SharedState(k, d, tol, maxIter);

        // ------------------------------------------------------------------
        // Barrier setup
        //
        // Two barriers are used per iteration so that each logical phase
        // (assign labels / accumulate centroids) has a clean synchronisation
        // point with its own barrier action.
        //
        //  Phase 1 barrier: all threads finish label assignment
        //                   → barrier action merges nothing (labels[] is
        //                     written without a race because each thread owns
        //                     a disjoint range of indices), then signals that
        //                     phase 1 is complete.
        //
        //  Phase 2 barrier: all threads finish partial centroid accumulation
        //                   → barrier action merges per-thread partial sums
        //                     into global sums, repairs empty clusters,
        //                     finalises centroids, checks convergence, and
        //                     advances the iteration counter.
        // ------------------------------------------------------------------

        // Per-thread partial accumulators — allocated once, cleared each iteration.
        final double[][][] partialSums   = new double[numThreads][k][d];
        final int[][]      partialCounts = new int[numThreads][k];

        // Synchronizes threads after independent label assignments to ensure consistent state before Phase 2.
        final Runnable phase1Action = () -> { /* intentionally empty */ };

        // Merges thread-local partial sums to calculate new centroids and updates convergence status.
        final Runnable phase2Action = () ->
                state.mergeAndUpdate(partialSums, partialCounts, numThreads, rnd, data, n);

        final CyclicBarrier phase1Barrier = new CyclicBarrier(numThreads, phase1Action);
        final CyclicBarrier phase2Barrier = new CyclicBarrier(numThreads, phase2Action);

        // ------------------------------------------------------------------
        // Worker thread definition
        // ------------------------------------------------------------------
        Thread[] threads = new Thread[numThreads];
        final int chunkSize = (n + numThreads - 1) / numThreads; // ceiling division

        for (int t = 0; t < numThreads; t++) {
            final int threadId = t;
            final int start = threadId * chunkSize;
            final int end = Math.min(start + chunkSize, n);

            threads[t] = new Thread(() -> {
                try {
                    // Each worker loops until the barrier action signals convergence or the iteration limit is reached.
                    for (int i = 0; i < maxIter && !state.done; i++) {

                        // Threads independently assign local point labels using read-only centroids to eliminate synchronization needs.
                        double[][] currentCentroids = state.centroids;
                        for (int j = start; j < end; j++) {
                            int best     = 0;
                            double bestD = squaredDist(data[j], currentCentroids[0], d);
                            for (int c = 1; c < k; c++) {
                                double dist = squaredDist(data[j], currentCentroids[c], d);
                                if (dist < bestD) {
                                    bestD = dist;
                                    best  = c;
                                }
                            }
                            labels[j] = best;
                        }

                        // Wait for all threads to finish label assignment.
                        phase1Barrier.await();

                        // Threads accumulate sums into thread-local slices to avoid synchronization overhead.
                        double[][] mySum   = partialSums[threadId];
                        int[]      myCnt   = partialCounts[threadId];

                        // Clear from the previous iteration.
                        for (int c = 0; c < k; c++) {
                            myCnt[c] = 0;
                            for (int j = 0; j < d; j++) {
                                mySum[c][j] = 0.0;
                            }
                        }

                        for (int j = start; j < end; j++) {
                            int c = labels[j];
                            myCnt[c]++;
                            for (int l = 0; l < d; l++) {
                                mySum[c][l] += data[j][l];
                            }
                        }

                        // Barrier synchronization point where the final thread executes phase2Action to update centroids and convergence.
                        phase2Barrier.await();
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Worker thread interrupted", e);
                } catch (BrokenBarrierException e) {
                    throw new RuntimeException("Barrier broken — a sibling thread failed", e);
                }
            }, "kmeans-worker-" + t);
        }

        // ------------------------------------------------------------------
        // Start all worker threads and wait for them to finish
        // ------------------------------------------------------------------
        // Publish the initial centroids into shared state before threads start.
        state.centroids = centroids;

        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Main thread interrupted while waiting for workers", e);
            }
        }

        // ------------------------------------------------------------------
        // Final inertia computation (single-threaded, post-convergence)
        // ------------------------------------------------------------------
        double inertia = computeInertia(data, n, d, state.centroids, labels);

        return new KMeansSequential.Result(state.centroids, labels, state.iterations, inertia);
    }

    // =========================================================================
    // SharedState — written exclusively by the barrier action (single-threaded)
    // =========================================================================

    /** Shared state updated by a single-threaded barrier action and safely read by workers after release. */
    private static final class SharedState {

        volatile double[][] centroids; // current centroid positions
        volatile boolean done;      // true when convergence or maxIter reached
        int iterations;

        private final double tol;
        private final int maxIter;
        private final int k;
        private final int d;

        SharedState(int k, int d, double tol, int maxIter) {
            this.k = k;
            this.d = d;
            this.tol = tol;
            this.maxIter = maxIter;
        }

        /** Finalizes centroids by merging partial sums and checking convergence against the current state. */
        void mergeAndUpdate(double[][][] partialSums, int[][] partialCounts, int numThreads, Random rnd, double[][] data, int n) {
            // --- Merge partial accumulators ---
            double[][] globalSums   = new double[k][d];
            int[] globalCounts = new int[k];

            for (int t = 0; t < numThreads; t++) {
                for (int c = 0; c < k; c++) {
                    globalCounts[c] += partialCounts[t][c];
                    for (int j = 0; j < d; j++) {
                        globalSums[c][j] += partialSums[t][c][j];
                    }
                }
            }

            // --- Repair empty clusters ---
            // Assign a random data point to any cluster that received no points.
            for (int c = 0; c < k; c++) {
                if (globalCounts[c] == 0) {
                    int idx = rnd.nextInt(n);
                    System.arraycopy(data[idx], 0, globalSums[c], 0, d);
                    globalCounts[c] = 1;
                }
            }

            // --- Finalise new centroids (divide sums by counts) ---
            double[][] newCentroids = new double[k][d];
            for (int c = 0; c < k; c++) {
                int cnt = globalCounts[c];
                for (int j = 0; j < d; j++) {
                    newCentroids[c][j] = globalSums[c][j] / cnt;
                }
            }

            // --- Check convergence ---
            // Validates convergence by comparing current state centroids with newly computed ones before updating the field.
            boolean converged = centroidsMovedLessThan(this.centroids, newCentroids, k, d, tol);

            // Publish new centroids so workers see them in the next iteration.
            centroids = newCentroids;
            iterations++;

            // Signal termination if converged or iteration limit reached.
            done = converged || (iterations >= maxIter);
        }
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /** Initialises k centroids by sampling k distinct data points at random. */
    private static double[][] initializeCentroids(double[][] data, int k, Random rnd) {
        Set<Integer> picked = new HashSet<>();
        while (picked.size() < k) {
            picked.add(rnd.nextInt(data.length));
        }
        List<Integer> order = new ArrayList<>(picked);
        Collections.sort(order);

        int d = data[0].length;
        double[][] centroids = new double[k][d];
        for (int c = 0; c < k; c++) {
            System.arraycopy(data[order.get(c)], 0, centroids[c], 0, d);
        }
        return centroids;
    }

    /** Returns {@code true} if every centroid moved less than {@code tol}. */
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

    /** Squared Euclidean distance between two {@code d}-dimensional points. */
    private static double squaredDist(double[] a, double[] b, int d) {
        return KMeansCore.squaredDistance(a, b, d);
    }

    /** Computes the total within-cluster sum of squared distances (inertia). */
    private static double computeInertia(
            double[][] data, int n, int d, double[][] centroids, int[] labels) {
        double sum = 0;
        for (int i = 0; i < n; i++) {
            sum += squaredDist(data[i], centroids[labels[i]], d);
        }
        return sum;
    }
}