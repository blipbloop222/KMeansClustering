package com.example.backend.web.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.backend.service.DatasetService;
import com.example.backend.web.request.GenerateDatasetRequest;
import com.example.backend.web.request.LoadDatasetRequest;
import com.example.backend.web.request.PreprocessDatasetRequest;
import com.example.backend.web.response.DatasetResponse;

@RestController
@RequestMapping("/api/datasets")
public class DatasetController {

    private final DatasetService datasetService;

    public DatasetController(DatasetService datasetService) {
        this.datasetService = datasetService;
    }

    /**
     * Generates a synthetic dataset with clustered data points.
     * benchmarking large datasets
     * no CSV file exists yet
     * 
     * @param request Contains parameters for dataset generation
     * @return Response containing the generated dataset
     */
    @PostMapping("/generate")
    public ResponseEntity<DatasetResponse> generateDataset(@RequestBody GenerateDatasetRequest request) {
        DatasetResponse response = datasetService.generateDataset(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Loads a dataset from a CSV file.
     * dataset already exists as CSV
     * loading Kaggle dataset
     * loading generated dataset from disk
     * real-world testing
     * 
     * @param request Contains the file path and normalization flag
     * @return Response containing the loaded dataset
     */
    @PostMapping("/load")
    public ResponseEntity<DatasetResponse> loadDataset(@RequestBody LoadDatasetRequest request) {
        DatasetResponse response = datasetService.loadDataset(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Preprocesses a dataset from Kaggle.
     *
     * @param request Contains the file paths for raw and output files
     * @return Response indicating success
     */
    @PostMapping("/preprocess")
    public ResponseEntity<String> preprocessDataset(@RequestBody PreprocessDatasetRequest request) {
        datasetService.preprocessDataset(request);
        return ResponseEntity.ok("Dataset preprocessing completed successfully.");
    }
}