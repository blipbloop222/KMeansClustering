package com.example.backend.service;

import org.springframework.stereotype.Service;

import com.example.backend.dataset.DatasetGenerator;
import com.example.backend.dataset.DatasetLoader;
import com.example.backend.dataset.DatasetUtils;
import com.example.backend.dataset.KagglePreprocessor;
import com.example.backend.web.request.GenerateDatasetRequest;
import com.example.backend.web.request.LoadDatasetRequest;
import com.example.backend.web.request.PreprocessDatasetRequest;
import com.example.backend.web.response.DatasetResponse;

@Service
public class DatasetService {

    public DatasetResponse generateDataset(GenerateDatasetRequest request) {
        double[][] data = DatasetGenerator.generateClustered(request.n(),request.dimensions(),request.k(),request.seed());
        return new DatasetResponse(data,data.length,data[0].length);
    }

    public DatasetResponse loadDataset(LoadDatasetRequest request) {
        double[][] data =DatasetLoader.loadCSV(request.filePath());
        if (request.normalize()) {
            DatasetUtils.normalize(data);
        }
        return new DatasetResponse(data,data.length,data[0].length);
    }

    public void preprocessDataset(PreprocessDatasetRequest request) {
        KagglePreprocessor.processWalmartData(request.rawFilePath(),request.outputFilePath());
    }
}