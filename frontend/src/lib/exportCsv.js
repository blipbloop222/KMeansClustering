// Downloads the raw-data table as a CSV file in the browser.

export function exportBenchmarkCsv(tableRows) {
  const header = 'Algorithm,Time (ms),Memory (MB),Iterations,Speedup,Converged\n'
  const rows = tableRows
    .map((r) =>
      [r.algorithm, r.time, r.memory, r.iterations, r.speedup, r.converged].join(','),
    )
    .join('\n')
  const blob = new Blob([header + rows], { type: 'text/csv' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'kmeans-benchmark.csv'
  a.click()
  URL.revokeObjectURL(url)
}
