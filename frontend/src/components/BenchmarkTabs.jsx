//Tabbed panel: Time vs Threads chart and Raw Data table (derives data from history/results).

import { Table2, TrendingUp } from 'lucide-react'
import { formatSize } from '../lib/benchmarkPaths.js'
import { useThreadComparisonData, useTableRows } from '../hooks/useBenchmarkDerived.js'
import { ThreadComparisonChart } from './charts/ThreadComparisonChart.jsx'
import { RawDataTable } from './RawDataTable.jsx'

const TABS = [
  { id: 0, label: 'Time vs Threads', icon: TrendingUp },
  { id: 1, label: 'Raw Data', icon: Table2 },
]

export function BenchmarkTabs({
  config,
  currentResults,
  runHistory,
  activeTab,
  onTabChange,
}) {
  const threadComparisonData = useThreadComparisonData(runHistory, config)
  const tableRows = useTableRows(currentResults)

  return (
    <section>
      <div className="border border-[var(--border)] bg-[var(--surface)]">
        <div className="flex border-b border-[var(--border)] overflow-x-auto">
          {TABS.map((tab) => (
            <button
              key={tab.id}
              onClick={() => onTabChange(tab.id)}
              className={`flex items-center gap-1.5 px-4 py-2.5 font-mono text-xs uppercase tracking-wide whitespace-nowrap border-r border-[var(--border)] ${
                activeTab === tab.id
                  ? 'bg-[var(--surface2)] text-[var(--text)]'
                  : 'text-[var(--text-muted)] hover:text-[var(--text)]'
              }`}
            >
              <tab.icon size={13} />
              {tab.label}
            </button>
          ))}
        </div>

        <div className="p-5 md:p-6">
          {activeTab === 0 && (
            <>
              <p className="text-[10px] font-mono text-[var(--text-muted)] mb-4">
                Same dataset ({formatSize(config.datasetSize)} pts, {config.dimensions}D, K=
                {config.k}). Change thread pool and re-run. Each line plots the time from that run at
                each thread setting (Sequential is still single-threaded on the backend; variation is
                run-to-run noise).
              </p>
              <ThreadComparisonChart data={threadComparisonData} />
            </>
          )}

          {activeTab === 1 && <RawDataTable tableRows={tableRows} />}
        </div>
      </div>
    </section>
  )
}
