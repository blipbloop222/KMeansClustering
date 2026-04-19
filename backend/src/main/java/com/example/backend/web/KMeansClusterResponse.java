package com.example.backend.web;

public record KMeansClusterResponse(
        double[][] centroids,
        int[] labels,
        int iterations,
        double inertia,
        int numPoints,
        int dimensions) {}
