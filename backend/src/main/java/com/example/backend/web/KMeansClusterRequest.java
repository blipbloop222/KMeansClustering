package com.example.backend.web;

public record KMeansClusterRequest(
        int k,
        Long seed,
        Integer maxIterations,
        Double tolerance,
        double[][] points,
        GenerateDatasetSpec generate) {}
