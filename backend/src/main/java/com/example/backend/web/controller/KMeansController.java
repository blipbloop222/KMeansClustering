package com.example.backend.web.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.service.KMeansClusteringService;
import com.example.backend.web.request.KMeansClusterRequest;

@RestController
@RequestMapping("/api/kmeans")
public class KMeansController {

    private final KMeansClusteringService clusteringService;

    public KMeansController(KMeansClusteringService clusteringService) {
        this.clusteringService = clusteringService;
    }

    @PostMapping("/sequential")
    public ResponseEntity<?> runSequential(@RequestBody KMeansClusterRequest request) {
        try {
            return ResponseEntity.ok(clusteringService.runSequential(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/parallel")
    public ResponseEntity<?> runParallel(@RequestBody KMeansClusterRequest request) {
        try {
            return ResponseEntity.ok(clusteringService.runParallel(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/concurrent")
    public ResponseEntity<?> runConcurrent(@RequestBody KMeansClusterRequest request) {
        try {
            return ResponseEntity.ok(clusteringService.runConcurrent(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } 
    }
}
