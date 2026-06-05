// Maps KMeansClusterResponse JSON into the shape used by dashboard components.

export function normalizeBackendResponse(algo, data, points) {
  return {
    algorithm: algo,
    executionTimeMs: data.executionTimeMs,
    memoryMb: null,
    iterations: data.iterations,
    converged: true,
    centroids: data.centroids,
    assignments: data.labels,
    points: points ?? null,
    inertia: data.inertia,
  }
}
