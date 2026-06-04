//Main dashboard column: composes alerts, summary, charts row, tabs, and footer.

import { useState } from 'react'
import { MainAlerts } from '../MainAlerts.jsx'
import { PerformanceSummary } from '../PerformanceSummary.jsx'
import { BenchmarkTabs } from '../BenchmarkTabs.jsx'
import { MobileConfigBar } from './MobileConfigBar.jsx'
import { ChartsRow } from './ChartsRow.jsx'
import { DashboardFooter } from './DashboardFooter.jsx'

export function BenchmarkMain({
  config,
  currentResults,
  runHistory,
  algoStatus,
  error,
  backendOnline,
  onRunBenchmark,
  onClearHistory,
  onOpenSidebar,
}) {
  const [activeTab, setActiveTab] = useState(0)

  return (
    <main className="flex-1 p-5 md:p-8 overflow-auto">
      <MobileConfigBar onOpenSidebar={onOpenSidebar} />

      <MainAlerts
        backendOnline={backendOnline}
        error={error}
        onRetry={onRunBenchmark}
      />

      <div className="flex flex-col gap-12 md:gap-14">
        <PerformanceSummary
          config={config}
          currentResults={currentResults}
          runHistory={runHistory}
          algoStatus={algoStatus}
        />

        <ChartsRow
          config={config}
          currentResults={currentResults}
          runHistory={runHistory}
          onClearHistory={onClearHistory}
        />

        <BenchmarkTabs
          config={config}
          currentResults={currentResults}
          runHistory={runHistory}
          activeTab={activeTab}
          onTabChange={setActiveTab}
        />

        <DashboardFooter />
      </div>
    </main>
  )
}
