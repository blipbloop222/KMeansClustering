// Reusable UI primitives: form controls, status indicators, section headers, chart wrapper.

import { ResponsiveContainer } from 'recharts'
import { ChevronDown } from 'lucide-react'

export function Label({ children }) {
  return (
    <label className="block text-[11px] font-mono uppercase tracking-wider text-[var(--text-muted)] mb-2">
      {children}
    </label>
  )
}

export function Select({ value, onChange, options }) {
  return (
    <div className="relative">
      <select
        value={value}
        onChange={(e) => onChange(Number(e.target.value))}
        className="w-full appearance-none bg-[var(--surface2)] border border-[var(--border)] text-[var(--text)] text-sm font-mono px-3 py-2 pr-8 rounded-none focus:outline-none focus:border-[var(--purple)]"
      >
        {options.map((o) => (
          <option key={o.value} value={o.value}>
            {o.label}
          </option>
        ))}
      </select>
      <ChevronDown
        size={14}
        className="absolute right-2.5 top-1/2 -translate-y-1/2 text-[var(--text-muted)] pointer-events-none"
      />
    </div>
  )
}

export function Slider({ value, onChange, min, max, step = 1 }) {
  return (
    <div className="flex items-center gap-3">
      <input
        type="range"
        min={min}
        max={max}
        step={step}
        value={value}
        onChange={(e) => onChange(Number(e.target.value))}
        className="flex-1 accent-[var(--purple)] h-1 bg-[var(--border)] rounded-none appearance-none cursor-pointer"
      />
      <span className="font-mono text-sm text-[var(--text)] w-8 text-right">{value}</span>
    </div>
  )
}

export function StatusDot({ status }) {
  const colors = {
    idle: 'bg-[var(--text-muted)]',
    running: 'bg-[var(--amber)] pulse-dot',
    done: 'bg-[var(--teal)]',
    failed: 'bg-[var(--red)]',
  }
  return <span className={`inline-block w-2 h-2 rounded-full ${colors[status] || colors.idle}`} />
}

export function MetricChip({ label, value, ok }) {
  return (
    <div className="flex items-center gap-2 px-3 py-2 bg-[var(--surface2)] border border-[var(--border)] text-sm">
      <span className="text-[var(--text-muted)] font-mono text-xs uppercase">{label}</span>
      <span
        className="font-mono font-medium"
        style={{ color: ok === false ? 'var(--red)' : ok === true ? 'var(--teal)' : 'var(--text)' }}
      >
        {value}
      </span>
    </div>
  )
}

export function SectionTitle({ icon: Icon, children }) {
  return (
    <h2 className="flex items-center gap-2 text-sm font-medium text-[var(--text)] mb-5 tracking-tight">
      <Icon size={15} className="text-[var(--text-muted)]" />
      {children}
    </h2>
  )
}

export function ChartFrame({ children, height = 280 }) {
  return (
    <div
      className="border border-[var(--border)] bg-[var(--surface)] p-3"
      style={{ height }}
    >
      <ResponsiveContainer width="100%" height="100%">
        {children}
      </ResponsiveContainer>
    </div>
  )
}
