package com.example.backend.clustering;

import com.example.backend.clustering.sequential.KMeansSequential;
import com.example.backend.clustering.parallel.KMeansParallel;
import com.example.backend.dataset.DatasetGenerator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class KMeansClusteringTest {
    
    @Test
    public void testSequentialClustering() {
        // Generate a simple dataset
        double[][] data = DatasetGenerator.generateClustered(100, 2, 3, 42L);
        
        // Perform clustering
        KMeansSequential.Result result = KMeansSequential.cluster(data, 3, 300, 1e-6, 42L);
        
        // Basic assertions
        assertNotNull(result, "Clustering result should not be null");
        assertEquals(3, result.centroids().length, "Number of centroids should match k");
        assertEquals(100, result.labels().length, "Number of labels should match input data");
        assertTrue(result.iterations() > 0 && result.iterations() <= 300, "Iterations should be within expected range");
        assertTrue(result.inertia() >= 0, "Inertia should be non-negative");
    }
    
    @Test
    public void testParallelClustering() {
        // Generate a simple dataset
        double[][] data = DatasetGenerator.generateClustered(100, 2, 3, 42L);
        
        // Perform clustering
        KMeansSequential.Result result = KMeansParallel.cluster(data, 3, 300, 1e-6, 42L);
        
        // Basic assertions
        assertNotNull(result, "Clustering result should not be null");
        assertEquals(3, result.centroids().length, "Number of centroids should match k");
        assertEquals(100, result.labels().length, "Number of labels should match input data");
        assertTrue(result.iterations() > 0 && result.iterations() <= 300, "Iterations should be within expected range");
        assertTrue(result.inertia() >= 0, "Inertia should be non-negative");
    }
    
    @Test
    public void testClusteringParameterValidation() {
        double[][] data = DatasetGenerator.generateClustered(100, 2, 3, 42L);
        
        // Test invalid k
        assertThrows(IllegalArgumentException.class, () -> 
            KMeansSequential.cluster(data, 0, 300, 1e-6, 42L), 
            "Should throw exception for k < 1"
        );
        
        // Test null data
        assertThrows(IllegalArgumentException.class, () -> 
            KMeansSequential.cluster(null, 3, 300, 1e-6, 42L), 
            "Should throw exception for null data"
        );
        
        // Test k > number of points
        assertThrows(IllegalArgumentException.class, () -> 
            KMeansSequential.cluster(data, 200, 300, 1e-6, 42L), 
            "Should throw exception when k exceeds number of points"
        );
    }
    
    @Test
    public void testClusteringConsistency() {
        // Generate a deterministic dataset
        double[][] data = DatasetGenerator.generateClustered(100, 2, 3, 42L);
        
        // Run multiple times with same seed
        KMeansSequential.Result result1 = KMeansSequential.cluster(data, 3, 300, 1e-6, 42L);
        KMeansSequential.Result result2 = KMeansSequential.cluster(data, 3, 300, 1e-6, 42L);
        
        // Compare results
        assertArrayEquals(result1.centroids(), result2.centroids(), "Centroids should be identical with same seed");
        assertArrayEquals(result1.labels(), result2.labels(), "Labels should be identical with same seed");
    }
}