package com.example.backend.web.request;

public record LoadDatasetRequest(
        String filePath,
        boolean normalize
) {}
