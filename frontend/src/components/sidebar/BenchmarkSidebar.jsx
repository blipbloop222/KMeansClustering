// Left sidebar: dataset/K/thread controls, algorithm checkboxes, and Run benchmark button.

import { Play } from 'lucide-react'
import { ALGO_META, K_OPTIONS, SIZE_OPTIONS, DIM_OPTIONS } from '../../constants/benchmark.js'
import { Label, Select, Slider, StatusDot } from '../ui/index.jsx'

export function BenchmarkSidebar({
  sidebarOpen,
  setSidebarOpen,
  config,
  setConfig,
  algoStatus,
  isRunning,
  onRunBenchmark,
  onToggleAlgo,
}) {
  return (
    <aside
      className={`${
        sidebarOpen ? 'block' : 'hidden'
      } md:block w-full md:w-[240px] md:min-w-[240px] shrink-0 border-b md:border-b-0 md:border-r border-[var(--border)] bg-[var(--surface)] p-5 flex flex-col gap-5`}
    >
      <div className="border-b border-[var(--border)] pb-4 mb-2">
        <p className="font-mono text-[10px] uppercase tracking-[0.2em] text-[var(--text-muted)]">
          k-means clustering
        </p>
        <h1 className="text-base font-semibold text-[var(--text)] mt-1 leading-tight">
          Benchmark Console
        </h1>
      </div>

      <div>
        <Label>Dataset size</Label>
        <Select
          value={config.datasetSize}
          onChange={(v) => setConfig((c) => ({ ...c, datasetSize: v }))}
          options={SIZE_OPTIONS}
        />
      </div>

      <div>
        <Label>Dimensions</Label>
        <Select
          value={config.dimensions}
          onChange={(v) => setConfig((c) => ({ ...c, dimensions: v }))}
          options={DIM_OPTIONS}
        />
      </div>

      <div>
        <Label>K clusters</Label>
        <Select
          value={config.k}
          onChange={(v) => setConfig((c) => ({ ...c, k: v }))}
          options={K_OPTIONS}
        />
      </div>

      <div className="flex flex-col gap-8 mt-2 pt-4 border-t border-[var(--border)]">
        <div>
          <Label>Thread pool — {config.threads}</Label>
          <Slider
            value={config.threads}
            onChange={(v) => setConfig((c) => ({ ...c, threads: v }))}
            min={1}
            max={16}
          />
          <p className="text-[10px] font-mono text-[var(--text-muted)] mt-1">
            Sent to backend for Concurrent / Parallel. Re-run at 1, 2, 4, 8 to fill Time vs Threads.
          </p>
        </div>

        <div>
          <Label>Algorithms</Label>
          <div className="space-y-3">
            {Object.entries(ALGO_META).map(([key, meta]) => (
              <label
                key={key}
                className="flex items-center gap-2 cursor-pointer text-sm text-[var(--text)]"
              >
                <input
                  type="checkbox"
                  checked={config.algorithms.includes(key)}
                  onChange={() => onToggleAlgo(key)}
                  className="accent-[var(--purple)]"
                />
                <span style={{ color: meta.color }} className="font-mono text-xs">
                  {meta.label}
                </span>
                <StatusDot status={algoStatus[key]} />
                <span className="text-[10px] font-mono text-[var(--text-muted)] ml-auto capitalize">
                  {algoStatus[key] === 'running'
                    ? 'Running…'
                    : algoStatus[key] === 'done'
                      ? 'Done'
                      : algoStatus[key] === 'failed'
                        ? 'Failed'
                        : ''}
                </span>
              </label>
            ))}
          </div>
        </div>
      </div>

      <div className="mt-auto pt-10 w-full">
        <button
          onClick={onRunBenchmark}
          disabled={isRunning || config.algorithms.length === 0}
          className="flex items-center justify-center gap-2 w-full py-2.5 bg-[var(--surface2)] border border-[var(--border)] hover:border-[var(--purple)] disabled:opacity-40 disabled:cursor-not-allowed font-mono text-sm uppercase tracking-wide transition-colors"
        >
          <Play size={14} />
          {isRunning ? 'Running…' : 'Run benchmark'}
        </button>
      </div>

      <button
        onClick={() => setSidebarOpen((o) => !o)}
        className="md:hidden text-xs font-mono text-[var(--text-muted)]"
      >
        Hide config
      </button>
    </aside>
  )
}
