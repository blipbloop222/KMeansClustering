package com.example.backend.experiment;

import com.example.backend.clustering.concurrent.KMeansConcurrent;
import com.example.backend.clustering.parallel.KMeansParallel;
import com.example.backend.clustering.sequential.KMeansSequential;
import com.example.backend.dataset.DatasetGenerator;

public class MainApp {
    public static void main(String[] args) {

        int n = 100000;
        int d = 5;
        int k = 5;
        long seed = 42L;

        System.out.println("Generating dataset...");
        double[][] data = DatasetGenerator.generateClustered(n, d, k, seed);

        runSequential(data, k, seed);
        runConcurrent(data, k, seed);
        runParallel(data, k, seed);
    }

    private static void runSequential(double[][] data, int k, long seed) {

        long start = System.nanoTime();

        KMeansSequential.Result result =
                KMeansSequential.cluster(data, k, 300, 1e-6, seed);

        long end = System.nanoTime();

        printSummary(
                "Sequential",
                result.iterations(),
                result.inertia(),
                (end - start) / 1_000_000
        );
    }

    private static void runConcurrent(double[][] data, int k, long seed) {

        long start = System.nanoTime();

        KMeansSequential.Result result =
                KMeansConcurrent.cluster(data, k, 300, 1e-6, seed);

        long end = System.nanoTime();

        printSummary(
                "Concurrent",
                result.iterations(),
                result.inertia(),
                (end - start) / 1_000_000
        );
    }

    private static void runParallel(double[][] data, int k, long seed) {

        long start = System.nanoTime();

        KMeansSequential.Result result =
                KMeansParallel.cluster(data, k, 300, 1e-6, seed);

        long end = System.nanoTime();

        printSummary(
                "Parallel",
                result.iterations(),
                result.inertia(),
                (end - start) / 1_000_000
        );
    }

    private static void printSummary(
            String algorithm,
            int iterations,
            double inertia,
            long executionTimeMs) {

        System.out.println("\n=== " + algorithm + " K-Means ===");
        System.out.println("Iterations      : " + iterations);
        System.out.println("Inertia         : " + inertia);
        System.out.println("Execution Time  : " + executionTimeMs + " ms");
    }
}