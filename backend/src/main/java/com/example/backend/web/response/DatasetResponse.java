package com.example.backend.web.response;

public record DatasetResponse(
        double[][] points,
        int numPoints,
        int dimensions
) {}