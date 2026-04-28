package com.example.backend.web.response;

public record KMeansClusterResponse(
        double[][] centroids,
        int[] labels,
        int iterations,
        double inertia,
        long executionTimeMs,
        int numPoints,
        int dimensions) {}