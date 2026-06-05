//Line chart of execution time vs dataset size (10k / 100k / 1M) from run history.

import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend } from 'recharts'
import { Trash2, TrendingUp } from 'lucide-react'
import { ALGO_META } from '../../constants/benchmark.js'
import { ChartFrame, SectionTitle } from '../ui/index.jsx'
import { ALGO_KEYS, chartGrid, chartTick, chartTooltipStyle, chartLegendStyle } from './chartTheme.js'

export function DatasetSizeChart({ lineChartData, runCount, onClearHistory }) {
  return (
    <div>
      <div className="flex items-center justify-between mb-5">
        <SectionTitle icon={TrendingUp}>Execution Time by Dataset Size</SectionTitle>
        <button
          onClick={onClearHistory}
          className="flex items-center gap-1 font-mono text-[10px] text-[var(--text-muted)] border border-[var(--border)] px-2 py-1 hover:text-[var(--text)]"
        >
          <Trash2 size={11} /> Clear history
        </button>
      </div>
      <p className="text-[10px] font-mono text-[var(--text-muted)] mb-3">
        Points fill in as you run benchmarks at different dataset sizes ({runCount} runs logged)
      </p>
      <ChartFrame height={300}>
        <LineChart data={lineChartData} margin={{ top: 10, right: 10, bottom: 20, left: 0 }}>
          <CartesianGrid {...chartGrid} />
          <XAxis dataKey="size" tick={chartTick} />
          <YAxis
            tick={chartTick}
            label={{ value: 'ms', angle: -90, position: 'insideLeft', fill: '#000000', fontSize: 10 }}
          />
          <Tooltip contentStyle={chartTooltipStyle} />
          <Legend wrapperStyle={chartLegendStyle} />
          {ALGO_KEYS.map((algo) => (
            <Line
              key={algo}
              type="monotone"
              dataKey={algo}
              name={ALGO_META[algo].label}
              stroke={ALGO_META[algo].hex}
              strokeWidth={2}
              dot={{ r: 4, fill: ALGO_META[algo].hex }}
              connectNulls
            />
          ))}
        </LineChart>
      </ChartFrame>
    </div>
  )
}
