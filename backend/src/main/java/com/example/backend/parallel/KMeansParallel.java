package com.example.backend.parallel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class KMeansParallel {

    // Parallel cluster assignment
    public static void assignClustersParallel(List<Point> points, List<Point> centroids, int numThreads) throws InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        int chunkSize = points.size() / numThreads;

        for (int i = 0; i < numThreads; i++) {
            int start = i * chunkSize;
            int end = (i == numThreads - 1) ? points.size() : start + chunkSize;

            List<Point> subList = points.subList(start, end);

            executor.execute(() -> {
                for (Point p : subList) {
                    double minDist = Double.MAX_VALUE;
                    int bestCluster = -1;

                    for (int j = 0; j < centroids.size(); j++) {
                        double dist = KMeansUtils.distance(p, centroids.get(j));

                        if (dist < minDist) {
                            minDist = dist;
                            bestCluster = j;
                        }
                    }

                    p.cluster = bestCluster;
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);
    }

    // Full K-Means loop
    public static void runKMeans(List<Point> points, int k, int maxIterations, int numThreads)
            throws InterruptedException {

        List<Point> centroids = KMeansUtils.initializeCentroids(points, k);

        for (int i = 0; i < maxIterations; i++) {
            assignClustersParallel(points, centroids, numThreads);
            KMeansUtils.updateCentroids(points, centroids, k);
        }
    }
}
