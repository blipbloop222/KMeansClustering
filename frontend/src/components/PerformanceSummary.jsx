//Performance Summary section: timing cards per algorithm, speedup and correctness chips.

import { Layers } from 'lucide-react'
import { ALGO_META } from '../constants/benchmark.js'
import { usePerformanceMetrics } from '../hooks/useBenchmarkDerived.js'
import { MetricChip, SectionTitle, StatusDot } from './ui/index.jsx'

export function PerformanceSummary({ config: _config, currentResults, runHistory: _runHistory, algoStatus }) {
  const { seqTime, speedupConc, speedupPar, correctnessOk, correctnessLabel } =
    usePerformanceMetrics(currentResults)

  return (
    <section>
      <SectionTitle icon={Layers}>Performance Summary</SectionTitle>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-5">
        {['sequential', 'concurrent', 'parallel'].map((algo) => {
          const r = currentResults[algo]
          const meta = ALGO_META[algo]
          const pct = r && seqTime ? Math.min(100, (r.executionTimeMs / seqTime) * 100) : 0
          return (
            <div
              key={algo}
              className="border border-[var(--border)] bg-[var(--surface)] p-4"
              style={{ borderTopColor: meta.color, borderTopWidth: 2 }}
            >
              <div className="flex items-center justify-between mb-3">
                <span className="font-mono text-xs uppercase" style={{ color: meta.color }}>
                  {meta.label}
                </span>
                <StatusDot status={algoStatus[algo]} />
              </div>
              {r ? (
                <>
                  <p className="font-mono text-2xl font-medium">{r.executionTimeMs.toLocaleString()}</p>
                  <p className="text-[10px] font-mono text-[var(--text-muted)] mb-3">ms execution</p>
                  <div className="space-y-1.5 text-xs font-mono">
                    <div className="flex justify-between">
                      <span className="text-[var(--text-muted)]">Memory</span>
                      <span>{r.memoryMb != null ? `${r.memoryMb} MB` : '—'}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-[var(--text-muted)]">Iterations</span>
                      <span>{r.iterations}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-[var(--text-muted)]">Inertia</span>
                      <span>{r.inertia?.toFixed(1) ?? '—'}</span>
                    </div>
                  </div>
                  <div className="mt-3 h-1.5 bg-[var(--surface2)]">
                    <div
                      className="h-full transition-all duration-500"
                      style={{ width: `${pct}%`, background: meta.color }}
                    />
                  </div>
                </>
              ) : (
                <p className="text-xs font-mono text-[var(--text-muted)]">No data — run benchmark</p>
              )}
            </div>
          )
        })}
      </div>

      <div className="flex flex-wrap gap-2">
        <MetricChip label="Speedup (conc/seq)" value={`${speedupConc}×`} />
        <MetricChip label="Speedup (par/seq)" value={`${speedupPar}×`} />
        <MetricChip
          label="Correctness"
          value={correctnessLabel}
          ok={correctnessOk === true ? true : correctnessOk === false ? false : undefined}
        />
      </div>
    </section>
  )
}
