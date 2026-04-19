package com.example.backend.clustering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


public final class KMeansSequential {

    public record Result(double[][] centroids, int[] labels, int iterations, double inertia) {}

    private KMeansSequential() {}


    public static Result cluster(double[][] data, int k, long seed) {
        return cluster(data, k, 300, 1e-6, seed);
    }

    /**
     * @param data rows = points, columns = features (same layout as {@code DatasetGenerator})
     * @param k number of clusters
     * @param maxIter maximum Lloyd iterations
     * @param tol convergence when every centroid moves less than this (Euclidean) in one iteration
     */
    public static Result cluster(double[][] data, int k, int maxIter, double tol, long seed) {
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
        for (double[] row : data) {
            if (row == null || row.length != d) {
                throw new IllegalArgumentException("all rows must have the same dimension");
            }
        }

        Random rnd = new Random(seed);
        double[][] centroids = initCentroids(data, n, d, k, rnd);
        int[] labels = new int[n];

        int iter = 0;
        boolean converged = false;

        for (; iter < maxIter && !converged; iter++) {
            assignLabels(data, n, d, k, centroids, labels);
            double[][] newCentroids = new double[k][d];
            int[] counts = new int[k];
            accumulateCentroids(data, n, d, labels, newCentroids, counts);
            repairEmptyClusters(data, n, d, k, rnd, newCentroids, counts);
            finalizeCentroids(newCentroids, counts, d);
            converged = centroidsMovedLessThan(centroids, newCentroids, k, d, tol);
            centroids = newCentroids;
        }

        assignLabels(data, n, d, k, centroids, labels);
        double inertia = computeInertia(data, n, d, centroids, labels);
        return new Result(centroids, labels, iter, inertia);
    }

    private static double[][] initCentroids(double[][] data, int n, int d, int k, Random rnd) {
        Set<Integer> picked = new HashSet<>();
        while (picked.size() < k) {
            picked.add(rnd.nextInt(n));
        }
        List<Integer> order = new ArrayList<>(picked);
        Collections.sort(order);
        double[][] centroids = new double[k][d];
        for (int c = 0; c < k; c++) {
            int idx = order.get(c);
            System.arraycopy(data[idx], 0, centroids[c], 0, d);
        }
        return centroids;
    }

    private static void assignLabels(double[][] data, int n, int d, int k, double[][] centroids, int[] labels) {
        for (int i = 0; i < n; i++) {
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
    }

    private static void accumulateCentroids(
            double[][] data, int n, int d, int[] labels, double[][] sums, int[] counts) {
        for (int i = 0; i < n; i++) {
            int c = labels[i];
            counts[c]++;
            for (int j = 0; j < d; j++) {
                sums[c][j] += data[i][j];
            }
        }
    }

    private static void repairEmptyClusters(
            double[][] data, int n, int d, int k, Random rnd, double[][] sums, int[] counts) {
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
            for (int j = 0; j < d; j++) {
                sums[c][j] /= cnt;
            }
        }
    }

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

    private static double squaredDist(double[] a, double[] b, int d) {
        double s = 0;
        for (int j = 0; j < d; j++) {
            double t = a[j] - b[j];
            s += t * t;
        }
        return s;
    }

    private static double computeInertia(
            double[][] data, int n, int d, double[][] centroids, int[] labels) {
        double sum = 0;
        for (int i = 0; i < n; i++) {
            sum += squaredDist(data[i], centroids[labels[i]], d);
        }
        return sum;
    }
}
