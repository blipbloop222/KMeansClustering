// Dataset CSV path builder and human-readable size labels for charts.

export function buildDatasetPath(datasetSize, fileDim, k) {
  return `../datasets/dataset_${datasetSize}points_${fileDim}D_K${k}.csv`
}

export function formatSize(n) {
  if (n >= 1_000_000) return '1M'
  if (n >= 1_000) return `${n / 1000}k`
  return String(n)
}
