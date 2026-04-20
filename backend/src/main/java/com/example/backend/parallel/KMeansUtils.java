package com.example.backend.parallel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KMeansUtils {

    // Convert dataset → Points
    public static List<Point> convertToPoints(double[][] data) {
        List<Point> points = new ArrayList<>();
        for (double[] row : data) {
            points.add(new Point(row));
        }
        return points;
    }

    // Initialize random centroids
    public static List<Point> initializeCentroids(List<Point> points, int k) {
        List<Point> centroids = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < k; i++) {
            Point p = points.get(rand.nextInt(points.size()));
            centroids.add(new Point(p.values.clone()));
        }

        return centroids;
    }

    // Distance calculation
    public static double distance(Point p, Point c) {
        double sum = 0;
        for (int i = 0; i < p.values.length; i++) {
            double diff = p.values[i] - c.values[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    // Update centroids
    public static void updateCentroids(List<Point> points, List<Point> centroids, int k) {
        int dim = centroids.get(0).values.length;

        double[][] sums = new double[k][dim];
        int[] counts = new int[k];

        for (Point p : points) {
            int c = p.cluster;
            counts[c]++;
            for (int i = 0; i < dim; i++) {
                sums[c][i] += p.values[i];
            }
        }

        for (int i = 0; i < k; i++) {
            if (counts[i] == 0) continue;

            for (int j = 0; j < dim; j++) {
                centroids.get(i).values[j] = sums[i][j] / counts[i];
            }
        }
    }
}