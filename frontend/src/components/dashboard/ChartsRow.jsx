//Two-column charts row: cluster scatter (left) and execution time by dataset size (right).

import { useState } from 'react'
import { useScatterData, useLineChartData } from '../../hooks/useBenchmarkDerived.js'
import { ClusterScatterChart } from '../charts/ClusterScatterChart.jsx'
import { DatasetSizeChart } from '../charts/DatasetSizeChart.jsx'

export function ChartsRow({ config, currentResults, runHistory, onClearHistory }) {
  const [scatterAlgo, setScatterAlgo] = useState('sequential')
  const scatterData = useScatterData(currentResults, scatterAlgo, config)
  const lineChartData = useLineChartData(runHistory)

  return (
    <section>
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 lg:gap-8">
        <ClusterScatterChart
          scatterData={scatterData}
          scatterAlgo={scatterAlgo}
          onScatterAlgoChange={setScatterAlgo}
          dimensions={config.dimensions}
        />
        <DatasetSizeChart
          lineChartData={lineChartData}
          runCount={runHistory.length}
          onClearHistory={onClearHistory}
        />
      </div>
    </section>
  )
}
