//2D cluster scatter plot (dims 0×1) with per-algorithm toggle and centroid markers.

import {
  ScatterChart,
  Scatter,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Cell,
  ZAxis,
} from 'recharts'
import { Activity } from 'lucide-react'
import { ALGO_META } from '../../constants/benchmark.js'
import { ChartFrame, SectionTitle } from '../ui/index.jsx'
import { chartGrid, chartTick, chartTooltipStyle } from './chartTheme.js'

export function ClusterScatterChart({
  scatterData,
  scatterAlgo,
  onScatterAlgoChange,
  dimensions,
}) {
  return (
    <div>
      <div className="flex items-center justify-between mb-5">
        <SectionTitle icon={Activity}>Cluster Projection (dim 0 × dim 1)</SectionTitle>
        <div className="flex gap-1">
          {['sequential', 'concurrent', 'parallel'].map((a) => (
            <button
              key={a}
              onClick={() => onScatterAlgoChange(a)}
              className={`font-mono text-[10px] px-2 py-1 border ${
                scatterAlgo === a
                  ? 'border-[var(--purple)] text-[var(--text)]'
                  : 'border-[var(--border)] text-[var(--text-muted)]'
              }`}
            >
              {ALGO_META[a].label}
            </button>
          ))}
        </div>
      </div>
      <p className="text-[10px] font-mono text-[var(--text-muted)] mb-2">
        {dimensions}D data — 2D projection using dimensions 0 and 1 only
      </p>
      <ChartFrame height={300}>
        <ScatterChart margin={{ top: 10, right: 10, bottom: 20, left: 0 }}>
          <CartesianGrid {...chartGrid} />
          <XAxis type="number" dataKey="x" tick={chartTick} />
          <YAxis type="number" dataKey="y" tick={chartTick} />
          <ZAxis range={[20, 20]} />
          <Tooltip contentStyle={chartTooltipStyle} />
          <Scatter data={scatterData.scatter} fill="#8b83f7">
            {scatterData.scatter.map((pt, i) => (
              <Cell
                key={i}
                fill={scatterData.clusterColors[pt.cluster] ?? '#888'}
                fillOpacity={0.55}
              />
            ))}
          </Scatter>
          <Scatter
            data={scatterData.centroids}
            fill="#000000"
            shape={(props) => {
              const { cx, cy } = props
              return (
                <g>
                  <circle cx={cx} cy={cy} r={10} fill="none" stroke="#000000" strokeWidth={1.5} />
                  <path
                    d={`M${cx},${cy - 6} L${cx + 5},${cy + 4} L${cx - 5},${cy + 4} Z`}
                    fill="#000000"
                  />
                </g>
              )
            }}
          />
        </ScatterChart>
      </ChartFrame>
    </div>
  )
}
