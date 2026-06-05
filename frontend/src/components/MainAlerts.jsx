// Top-of-main alerts when the backend is offline or a benchmark run fails (with retry).

import { AlertTriangle, RefreshCw, ServerOff } from 'lucide-react'
import { BACKEND_OFFLINE_MSG } from '../constants/benchmark.js'

export function MainAlerts({ backendOnline, error, onRetry }) {
  return (
    <>
      {backendOnline === false && (
        <div className="flex items-center gap-2 mb-4 px-4 py-3 border border-[var(--red)] bg-[var(--surface)] text-sm">
          <ServerOff size={16} className="text-[var(--red)] shrink-0" />
          <span>{BACKEND_OFFLINE_MSG}</span>
        </div>
      )}

      {error && (
        <div className="flex items-center justify-between gap-3 mb-4 px-4 py-3 border border-[var(--red)] bg-[var(--surface)] text-sm">
          <div className="flex items-center gap-2">
            <AlertTriangle size={16} className="text-[var(--red)] shrink-0" />
            <span className="font-mono text-xs">{error}</span>
          </div>
          <button
            onClick={onRetry}
            className="flex items-center gap-1 font-mono text-xs border border-[var(--border)] px-2 py-1 hover:border-[var(--purple)]"
          >
            <RefreshCw size={12} /> Retry
          </button>
        </div>
      )}
    </>
  )
}
