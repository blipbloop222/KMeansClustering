package com.example.backend.web.request;

public record GenerateDatasetRequest(
        int n,
        int dimensions,
        int k,
        long seed
) {}