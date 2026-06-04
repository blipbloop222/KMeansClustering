//Memoized selectors for charts and summary: speedup, scatter, line/thread data, table rows.

import { useMemo } from 'react'
import { ALGO_META } from '../constants/benchmark.js'
import { formatSize } from '../lib/benchmarkPaths.js'
import { subsamplePoints } from '../lib/subsample.js'
import { compareAssignments, compareCentroids } from '../lib/compare.js'
import { getClusterColorPalette } from '../lib/clusterColors.js'

/** Speedup and assignment/centroid correctness from the latest run. */
export function usePerformanceMetrics(currentResults) {
  const seqTime = currentResults.sequential?.executionTimeMs ?? 0
  const concTime = currentResults.concurrent?.executionTimeMs
  const parTime = currentResults.parallel?.executionTimeMs

  const speedupConc = seqTime && concTime ? (seqTime / concTime).toFixed(2) : '—'
  const speedupPar = seqTime && parTime ? (seqTime / parTime).toFixed(2) : '—'

  const labelsMatch =
    currentResults.sequential && currentResults.concurrent && currentResults.parallel
      ? compareAssignments(
          currentResults.sequential.assignments,
          currentResults.concurrent.assignments,
        ) &&
        compareAssignments(
          currentResults.sequential.assignments,
          currentResults.parallel.assignments,
        )
      : currentResults.sequential && currentResults.concurrent
        ? compareAssignments(
            currentResults.sequential.assignments,
            currentResults.concurrent.assignments,
          )
        : null

  const centroidsMatch =
    currentResults.sequential?.centroids && currentResults.parallel?.centroids
      ? compareCentroids(currentResults.sequential.centroids, currentResults.parallel.centroids)
      : null

  const correctnessOk = labelsMatch === true || centroidsMatch === true
  const correctnessLabel =
    labelsMatch === true
      ? 'Assignments match'
      : labelsMatch === false
        ? 'Assignments differ'
        : centroidsMatch === true
          ? 'Centroids match (ε)'
          : centroidsMatch === false
            ? 'Centroids differ'
            : '—'

  return { seqTime, speedupConc, speedupPar, correctnessOk, correctnessLabel }
}

/** Point/centroid coordinates for the cluster scatter chart (2D projection). */
export function useScatterData(currentResults, scatterAlgo, config) {
  const clusterColors = useMemo(() => getClusterColorPalette(config.k), [config.k])

  return useMemo(() => {
    const result = currentResults[scatterAlgo]
    if (!result?.points || !result?.assignments) {
      return { scatter: [], centroids: [], subsampled: false, clusterColors }
    }

    const { points, assignments, subsampled } = subsamplePoints(
      result.points,
      result.assignments,
    )

    const scatter = points.map((p, i) => ({
      x: p[0],
      y: p[1],
      cluster: assignments[i],
    }))

    const centroids = (result.centroids ?? []).map((c, i) => ({
      x: c[0],
      y: c[1],
      cluster: i,
      isCentroid: true,
    }))

    return { scatter, centroids, subsampled, clusterColors }
  }, [currentResults, scatterAlgo, clusterColors])
}

/** Latest timing per algorithm for each dataset size (10k, 100k, 1M). */
export function useLineChartData(runHistory) {
  return useMemo(() => {
    const sizes = [10000, 100000, 1000000]
    return sizes.map((size) => {
      const entry = [...runHistory]
        .reverse()
        .find((r) => r.config.datasetSize === size)
      const row = { size: formatSize(size) }
      if (entry) {
        for (const algo of ['sequential', 'concurrent', 'parallel']) {
          row[algo] = entry.results[algo]?.executionTimeMs ?? null
        }
      }
      return row
    })
  }, [runHistory])
}

/** Timing per algorithm grouped by thread count for the current sidebar config. */
export function useThreadComparisonData(runHistory, config) {
  return useMemo(() => {
    const matching = runHistory.filter(
      (r) =>
        r.config.datasetSize === config.datasetSize &&
        r.config.dimensions === config.dimensions &&
        r.config.k === config.k,
    )

    const byThreads = new Map()
    for (const run of matching) {
      const t = run.config.threads
      const row = byThreads.get(t) ?? { threads: t }
      for (const algo of ['sequential', 'concurrent', 'parallel']) {
        const ms = run.results[algo]?.executionTimeMs
        if (ms != null) row[algo] = ms
      }
      byThreads.set(t, row)
    }
    return [...byThreads.values()].sort((a, b) => a.threads - b.threads)
  }, [runHistory, config.datasetSize, config.dimensions, config.k])
}

/** Rows for the Raw Data table and CSV export. */
export function useTableRows(currentResults) {
  return useMemo(() => {
    const seq = currentResults.sequential?.executionTimeMs
    return ['sequential', 'concurrent', 'parallel']
      .filter((a) => currentResults[a])
      .map((algo) => {
        const r = currentResults[algo]
        return {
          algorithm: ALGO_META[algo].label,
          time: r.executionTimeMs,
          memory: r.memoryMb != null ? r.memoryMb : '—',
          iterations: r.iterations,
          speedup: seq && algo !== 'sequential' ? (seq / r.executionTimeMs).toFixed(2) + '×' : '1.00×',
          converged: r.converged ? 'Yes' : 'No',
        }
      })
  }, [currentResults])
}
