//Line chart of execution time vs thread pool for the current dataset/K/dimensions.

import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend } from 'recharts'
import { ALGO_META } from '../../constants/benchmark.js'
import { ChartFrame } from '../ui/index.jsx'
import { ALGO_KEYS, chartGrid, chartTick, chartTooltipStyle, chartLegendStyle } from './chartTheme.js'

export function ThreadComparisonChart({ data }) {
  if (data.length === 0) {
    return (
      <p className="font-mono text-xs text-[var(--text-muted)]">
        No runs logged for this dataset yet. Run a benchmark, then change threads and run again.
      </p>
    )
  }

  return (
    <ChartFrame height={300}>
      <LineChart data={data} margin={{ top: 40, right: 16, bottom: 36, left: 8 }}>
        <CartesianGrid {...chartGrid} />
        <XAxis
          dataKey="threads"
          tick={chartTick}
          label={{
            value: 'Thread pool',
            position: 'insideBottom',
            offset: -2,
            fill: '#000000',
            fontSize: 10,
          }}
        />
        <YAxis
          tick={chartTick}
          label={{ value: 'ms', angle: -90, position: 'insideLeft', fill: '#000000', fontSize: 10 }}
        />
        <Tooltip contentStyle={chartTooltipStyle} />
        <Legend
          verticalAlign="top"
          align="center"
          wrapperStyle={{ ...chartLegendStyle, top: 0 }}
        />
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
  )
}
