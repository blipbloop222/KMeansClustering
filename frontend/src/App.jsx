// Root layout: benchmark config state, reducer, and wiring between sidebar and main dashboard.

import { useReducer, useState, useEffect } from 'react'
import { checkBackend } from './api/benchmark.js'
import { benchmarkReducer, initialBenchmarkState } from './state/benchmarkReducer.js'
import { useRunBenchmark } from './hooks/useRunBenchmark.js'
import { BenchmarkSidebar } from './components/sidebar/BenchmarkSidebar.jsx'
import { BenchmarkMain } from './components/dashboard/BenchmarkMain.jsx'

export default function App() {
  const [sidebarOpen, setSidebarOpen] = useState(true)

  const [config, setConfig] = useState({
    datasetSize: 10000,
    dimensions: 3,
    k: 5,
    threads: 4,
    algorithms: ['sequential', 'concurrent', 'parallel'],
  })

  const [state, dispatch] = useReducer(benchmarkReducer, initialBenchmarkState)

  useEffect(() => {
    checkBackend().then((online) => dispatch({ type: 'SET_BACKEND', online }))
  }, [])

  const { runHistory, currentResults, algoStatus, error, backendOnline } = state
  const isRunning = Object.values(algoStatus).some((s) => s === 'running')

  const runBenchmark = useRunBenchmark({ config, dispatch })

  const toggleAlgo = (algo) => {
    setConfig((c) => ({
      ...c,
      algorithms: c.algorithms.includes(algo)
        ? c.algorithms.filter((a) => a !== algo)
        : [...c.algorithms, algo],
    }))
  }

  return (
    <div className="min-h-screen flex flex-col md:flex-row bg-[var(--bg)]">
      <BenchmarkSidebar
        sidebarOpen={sidebarOpen}
        setSidebarOpen={setSidebarOpen}
        config={config}
        setConfig={setConfig}
        algoStatus={algoStatus}
        isRunning={isRunning}
        onRunBenchmark={runBenchmark}
        onToggleAlgo={toggleAlgo}
      />

      <BenchmarkMain
        config={config}
        currentResults={currentResults}
        runHistory={runHistory}
        algoStatus={algoStatus}
        error={error}
        backendOnline={backendOnline}
        onRunBenchmark={runBenchmark}
        onClearHistory={() => dispatch({ type: 'CLEAR_HISTORY' })}
        onOpenSidebar={() => setSidebarOpen(true)}
      />
    </div>
  )
}
