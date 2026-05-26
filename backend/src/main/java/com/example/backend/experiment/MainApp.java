package com.example.backend.experiment;

import com.example.backend.clustering.concurrent.KMeansConcurrent;
import com.example.backend.clustering.parallel.KMeansParallel;
import com.example.backend.clustering.sequential.KMeansSequential;
import com.example.backend.dataset.DatasetLoader;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class MainApp {

    private static final String DATASETS_DIR  = "../datasets";
    private static final String WALMART_CLEAN = DATASETS_DIR + "/walmart_rfm_clean.csv";
    private static final int    WALMART_K     = 5;   // RFM customer segments
    private static final int[]  THREAD_COUNTS = {1, 2, 4, 8};
    private static final int    MAX_ITER      = 300;
    private static final double TOL           = 1e-6;
    private static final long   SEED          = 42L;

    public static void main(String[] args) {
        File dir = new File(DATASETS_DIR);
        // Exclude the walmart clean file from the synthetic sweep
        File[] files = dir.listFiles((d, name) ->
                name.endsWith(".csv") && !name.equals("walmart_rfm_clean.csv"));

        if (files == null || files.length == 0) {
            System.err.println("No CSV files found in: " + dir.getAbsolutePath());
            System.err.println("Run DatasetMain first, or check the datasets/ folder location.");
            return;
        }

        Arrays.sort(files);

        System.out.printf("%-44s %-12s %-8s %-12s %-11s %-10s%n",
                "Dataset", "Algorithm", "Threads", "Time (ms)", "Mem (MB)", "Iterations");
        System.out.println("-".repeat(101));

        // Create one reusable ExecutorService per thread count.
        // These pools live for the entire benchmark run — no new threads are
        // spawned per dataset, which is what KMeansParallel (v3) is supposed to do.
        Map<Integer, ExecutorService> pools = new LinkedHashMap<>();
        for (int t : THREAD_COUNTS) {
            pools.put(t, Executors.newFixedThreadPool(t));
        }

        try {
            // --- Real-world Walmart RFM dataset ---
            File walmartFile = new File(WALMART_CLEAN);
            if (walmartFile.exists()) {
                runDataset("walmart_rfm_clean", walmartFile.getPath(), WALMART_K, pools);
                System.out.println();
            } else {
                System.out.println("[SKIP] walmart_rfm_clean.csv not found — run DatasetMain first.\n");
            }

            // --- Synthetic datasets ---
            for (File f : files) {
                String name = f.getName().replace(".csv", "");
                int k = extractK(name);
                runDataset(name, f.getPath(), k, pools);
                System.out.println();
            }

        } finally {
            // Shut down all pools cleanly after the full benchmark finishes
            pools.values().forEach(ExecutorService::shutdown);
        }
    }

    private static void runDataset(String label, String filePath, int k,
                                    Map<Integer, ExecutorService> pools) {
        System.gc();
        double[][] data = DatasetLoader.loadCSV(filePath);
        if (data == null || data.length == 0) {
            System.err.println("  [SKIP] Failed to load: " + filePath);
            return;
        }

        // Sequential (single-threaded, no pool needed)
        benchmark(label, "Sequential", 1,
                () -> KMeansSequential.cluster(data, k, MAX_ITER, TOL, SEED));

        // Concurrent — manual threads per call (intentional for v2 design)
        for (int t : THREAD_COUNTS) {
            benchmark(label, "Concurrent", t,
                    () -> KMeansConcurrent.cluster(data, k, MAX_ITER, TOL, SEED, t));
        }

        // Parallel — reuses the long-lived pool for this thread count (v3 design)
        for (int t : THREAD_COUNTS) {
            ExecutorService pool = pools.get(t);
            benchmark(label, "Parallel", t,
                    () -> KMeansParallel.cluster(data, k, MAX_ITER, TOL, SEED, pool));
        }
    }

    private static void benchmark(String dataset, String algo, int threads,
                                   Supplier<KMeansSequential.Result> task) {
        Runtime rt = Runtime.getRuntime();
        System.gc();
        long memBefore = rt.totalMemory() - rt.freeMemory();
        long startNs   = System.nanoTime();

        KMeansSequential.Result result = task.get();

        long timeMs     = (System.nanoTime() - startNs) / 1_000_000;
        long memDeltaMb = (rt.totalMemory() - rt.freeMemory() - memBefore) / (1024 * 1024);

        System.out.printf("%-44s %-12s %-8d %-12d %-11d %-10d%n",
                dataset, algo, threads, timeMs, memDeltaMb, result.iterations());
    }

    // Parses K from filenames like "dataset_10000points_3D_K5"
    private static int extractK(String filename) {
        int idx = filename.lastIndexOf("_K");
        if (idx < 0) return 5;
        try {
            return Integer.parseInt(filename.substring(idx + 2));
        } catch (NumberFormatException e) {
            return 5;
        }
    }
}
