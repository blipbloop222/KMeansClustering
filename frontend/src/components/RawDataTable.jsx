// Raw Data tab content: results table and Export CSV button.

import { Download } from 'lucide-react'
import { exportBenchmarkCsv } from '../lib/exportCsv.js'

const TABLE_HEADERS = [
  'Algorithm',
  'Time (ms)',
  'Memory (MB)',
  'Iterations',
  'Speedup',
  'Converged',
]

export function RawDataTable({ tableRows }) {
  return (
    <>
      <div className="flex justify-end mb-3">
        <button
          onClick={() => exportBenchmarkCsv(tableRows)}
          disabled={tableRows.length === 0}
          className="flex items-center gap-1 font-mono text-xs border border-[var(--border)] px-3 py-1.5 hover:border-[var(--purple)] disabled:opacity-40"
        >
          <Download size={12} /> Export CSV
        </button>
      </div>
      {tableRows.length === 0 ? (
        <p className="font-mono text-xs text-[var(--text-muted)]">No results yet.</p>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full text-left font-mono text-xs">
            <thead>
              <tr className="border-b border-[var(--border)] text-[var(--text-muted)] uppercase tracking-wide">
                {TABLE_HEADERS.map((h) => (
                  <th key={h} className="py-2 pr-4 font-medium">
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {tableRows.map((row) => (
                <tr key={row.algorithm} className="border-b border-[var(--border)]">
                  <td className="py-2.5 pr-4">{row.algorithm}</td>
                  <td className="py-2.5 pr-4">{row.time.toLocaleString()}</td>
                  <td className="py-2.5 pr-4">{row.memory}</td>
                  <td className="py-2.5 pr-4">{row.iterations}</td>
                  <td className="py-2.5 pr-4">{row.speedup}</td>
                  <td className="py-2.5 pr-4">{row.converged}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  )
}
