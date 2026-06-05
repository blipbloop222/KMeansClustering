//Shared benchmark constants: API base URL, algorithm metadata, dropdown options, error messages.

/** Proxied to http://localhost:8080 via vite.config.js */
export const API_BASE = ''

export const SEED = 42
export const TOLERANCE = 1e-6
export const EPSILON = 1e-3

export const ALGO_META = {
  sequential: { label: 'Sequential', color: 'var(--purple)', hex: '#8b83f7' },
  concurrent: { label: 'Concurrent', color: 'var(--teal)', hex: '#34d9a0' },
  parallel: { label: 'Parallel', color: 'var(--amber)', hex: '#f5a623' },
}

export const K_OPTIONS = [
  { label: 'K = 5', value: 5 },
  { label: 'K = 10', value: 10 },
]

export const SIZE_OPTIONS = [
  { label: '10k points', value: 10000 },
  { label: '100k points', value: 100000 },
  { label: '1M points', value: 1000000 },
]

export const DIM_OPTIONS = [
  { label: '3D', value: 3, fileDim: 3 },
  { label: '5D', value: 5, fileDim: 5 },
  { label: '10D', value: 10, fileDim: 10 },
]

export const BACKEND_OFFLINE_MSG =
  'API unreachable on port 8080. Start Spring Boot (BackendApplication) — not the MainApp CLI benchmark.'
