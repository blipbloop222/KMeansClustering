package com.example.backend.web;

import com.example.backend.clustering.KMeansSequential;
import com.example.backend.clustering.KMeansSequential.Result;
import com.example.backend.dataset.DatasetGenerator;
import org.springframework.stereotype.Service;

@Service
public class KMeansClusteringService {

    private static final int DEFAULT_MAX_ITER = 300;
    private static final double DEFAULT_TOLERANCE = 1e-6;
    private static final long DEFAULT_SEED = 42L;

    public KMeansClusterResponse cluster(KMeansClusterRequest request) {
        double[][] data = resolveData(request);
        long seed = request.seed() != null ? request.seed() : DEFAULT_SEED;
        int maxIter = request.maxIterations() != null ? request.maxIterations() : DEFAULT_MAX_ITER;
        double tol = request.tolerance() != null ? request.tolerance() : DEFAULT_TOLERANCE;

        if (maxIter < 1) {
            throw new IllegalArgumentException("maxIterations must be at least 1");
        }
        if (tol <= 0) {
            throw new IllegalArgumentException("tolerance must be positive");
        }

        Result result = KMeansSequential.cluster(data, request.k(), maxIter, tol, seed);
        return new KMeansClusterResponse(
                result.centroids(),
                result.labels(),
                result.iterations(),
                result.inertia(),
                data.length,
                data[0].length);
    }

    private static double[][] resolveData(KMeansClusterRequest request) {
        boolean hasPoints = request.points() != null && request.points().length > 0;
        boolean hasGenerate = request.generate() != null;

        if (hasPoints && hasGenerate) {
            throw new IllegalArgumentException("Provide only one of \"points\" or \"generate\"");
        }
        if (!hasPoints && !hasGenerate) {
            throw new IllegalArgumentException("Provide either \"points\" or \"generate\"");
        }

        if (hasGenerate) {
            GenerateDatasetSpec g = request.generate();
            if (g.n() < 1) {
                throw new IllegalArgumentException("generate.n must be at least 1");
            }
            if (g.dimensions() < 1) {
                throw new IllegalArgumentException("generate.dimensions must be at least 1");
            }
            if (g.generatorClusters() < 1) {
                throw new IllegalArgumentException("generate.generatorClusters must be at least 1");
            }
            return DatasetGenerator.generateClustered(g.n(), g.dimensions(), g.generatorClusters(), seedOrDefault(request));
        }

        return request.points();
    }

    private static long seedOrDefault(KMeansClusterRequest request) {
        return request.seed() != null ? request.seed() : DEFAULT_SEED;
    }
}
