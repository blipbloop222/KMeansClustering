// Runs a full benchmark: check backend, load CSV, execute selected algorithms, append to history.

import { useCallback } from 'react'
import { SEED, TOLERANCE, DIM_OPTIONS, BACKEND_OFFLINE_MSG } from '../constants/benchmark.js'
import { checkBackend, loadDatasetPoints, runAlgorithm } from '../api/benchmark.js'
import { buildDatasetPath } from '../lib/benchmarkPaths.js'
import { normalizeBackendResponse } from '../lib/normalize.js'

export function useRunBenchmark({ config, dispatch }) {
  const dimOption = DIM_OPTIONS.find((d) => d.value === config.dimensions) ?? DIM_OPTIONS[1]
  const selectedAlgos = config.algorithms

  return useCallback(async () => {
    dispatch({ type: 'RESET_RUN' })

    const online = await checkBackend()
    dispatch({ type: 'SET_BACKEND', online })
    if (!online) {
      dispatch({ type: 'SET_ERROR', error: BACKEND_OFFLINE_MSG })
      return
    }

    const filePath = buildDatasetPath(config.datasetSize, dimOption.fileDim, config.k)
    const requestBody = {
      k: config.k,
      seed: SEED,
      tolerance: TOLERANCE,
      datasetFilePath: filePath,
      threads: config.threads,
    }

    let points = null
    try {
      points = await loadDatasetPoints(filePath)
    } catch (e) {
      dispatch({
        type: 'SET_ERROR',
        error: `${e.message}. Path: ${filePath}. Run DatasetMain to generate CSVs, or pick k∈{5,10} with dims 3, 5, or 10.`,
      })
      return
    }

    const results = {}
    for (const algo of selectedAlgos) {
      dispatch({ type: 'SET_ALGO_STATUS', algo, status: 'running' })
      try {
        const data = await runAlgorithm(algo, requestBody)
        const normalized = normalizeBackendResponse(algo, data, points)
        results[algo] = normalized
        dispatch({ type: 'SET_RESULT', algo, result: normalized })
        dispatch({ type: 'SET_ALGO_STATUS', algo, status: 'done' })
      } catch (e) {
        dispatch({ type: 'SET_ALGO_STATUS', algo, status: 'failed' })
        dispatch({ type: 'SET_ERROR', error: e.message })
      }
    }

    if (Object.keys(results).length > 0) {
      dispatch({
        type: 'ADD_HISTORY',
        entry: { config: { ...config }, results, timestamp: Date.now() },
      })
    }
  }, [config, dimOption.fileDim, selectedAlgos, dispatch])
}
