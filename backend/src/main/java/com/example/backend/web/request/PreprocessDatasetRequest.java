package com.example.backend.web.request;

public record PreprocessDatasetRequest(
        String rawFilePath,
        String outputFilePath
) {}