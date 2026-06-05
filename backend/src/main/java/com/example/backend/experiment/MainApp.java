package com.example.backend.experiment;

import com.example.backend.clustering.concurrent.KMeansConcurrent;
import com.example.backend.clustering.core.KMeansCore;
import com.example.backend.clustering.parallel.KMeansParallel;
import com.example.backend.clustering.sequential.KMeansSequential;
import com.example.backend.dataset.DatasetLoader;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class MainApp {

    private static final String DATASETS_DIR  = "../datasets";
    private static final String WALMART_CLEAN = DATASETS_DIR + "/walmart_rfm_clean.csv";
    private static final int    WALMART_K     = 5;   // RFM customer segments
    private static final int[]  THREAD_COUNTS = {1, 2, 4, 8};
    private static final int    MAX_ITER      = 300;
    private static final double TOL           = 1e-6;
    private static final long   SEED          = 42L;
    private static final long   CPU_SAMPLE_MS = 20L;

    /** Sun extension — exposes getProcessCpuLoad() on HotSpot / OpenJDK. */
    private static final com.sun.management.OperatingSystemMXBean OS_BEAN = resolveOsBean();

    private static com.sun.management.OperatingSystemMXBean resolveOsBean() {
        OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
        if (bean instanceof com.sun.management.OperatingSystemMXBean sun) {
            return sun;
        }
        System.err.println("[WARN] Process CPU metrics unavailable on this JVM.");
        return null;
    }

    /** Average and peak process CPU load (0–100%) sampled during a task. */
    private record CpuSample(double avgPercent, double peakPercent) {
        static CpuSample unavailable() {
            return new CpuSample(-1, -1);
        }

        String formatAvg() {
            return avgPercent < 0 ? "N/A" : String.format("%.1f", avgPercent);
        }
    }

    public static void main(String[] args) {
        warmUpCpuMetrics();

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

        System.out.printf("%-44s %-12s %-8s %-12s %-11s %-10s %-10s%n",
                "Dataset", "Algorithm", "Threads", "Time (ms)", "Mem (MB)", "CPU (%)", "Iterations");
        System.out.println("-".repeat(112));

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
                                   Supplier<KMeansCore.Result> task) {
        Runtime rt = Runtime.getRuntime();
        System.gc();
        long memBefore = rt.totalMemory() - rt.freeMemory();
        long startNs   = System.nanoTime();

        CpuSample cpu;
        KMeansCore.Result result;
        if (OS_BEAN != null) {
            var measured = runWithCpuSampling(task);
            result = measured.result();
            cpu = measured.cpu();
        } else {
            result = task.get();
            cpu = CpuSample.unavailable();
        }

        long timeMs     = (System.nanoTime() - startNs) / 1_000_000;
        long memDeltaMb = (rt.totalMemory() - rt.freeMemory() - memBefore) / (1024 * 1024);

        System.out.printf("%-44s %-12s %-8d %-12d %-11d %-10s %-10d%n",
                dataset, algo, threads, timeMs, memDeltaMb, cpu.formatAvg(), result.iterations());
    }

    private record MeasuredRun(KMeansCore.Result result, CpuSample cpu) {}

    /**
     * Polls {@link com.sun.management.OperatingSystemMXBean#getProcessCpuLoad()} while the
     * benchmark task runs. Returns average and peak load as a percentage of total system capacity.
     */
    private static MeasuredRun runWithCpuSampling(Supplier<KMeansCore.Result> task) {
        List<Double> samples = new ArrayList<>();
        AtomicBoolean running = new AtomicBoolean(true);

        Thread sampler = new Thread(() -> {
            while (running.get()) {
                double load = OS_BEAN.getProcessCpuLoad();
                if (load >= 0.0) {
                    samples.add(load);
                }
                try {
                    Thread.sleep(CPU_SAMPLE_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "cpu-sampler");
        sampler.setDaemon(true);
        sampler.start();

        KMeansCore.Result result = task.get();

        running.set(false);
        try {
            sampler.join(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (samples.isEmpty()) {
            return new MeasuredRun(result, CpuSample.unavailable());
        }

        double peak = samples.stream().mapToDouble(d -> d).max().orElse(0) * 100.0;
        double avg  = samples.stream().mapToDouble(d -> d).average().orElse(0) * 100.0;
        return new MeasuredRun(result, new CpuSample(avg, peak));
    }

    /** First calls often return -1 until the JVM collects process CPU counters. */
    private static void warmUpCpuMetrics() {
        if (OS_BEAN == null) {
            return;
        }
        for (int i = 0; i < 3; i++) {
            OS_BEAN.getProcessCpuLoad();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
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
