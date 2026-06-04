//HTTP client for Spring Boot: health check, dataset load, and K-Means algorithm endpoints.
import { API_BASE } from '../constants/benchmark.js'

export async function checkBackend() {
  try {
    const res = await fetch(`${API_BASE}/api/datasets/load`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ filePath: '__health_check__', normalize: false }),
    })
    // Any HTTP response means the server is up (400/500 from bad path is fine).
    return res.ok || res.status >= 400
  } catch {
    return false
  }
}

export async function loadDatasetPoints(filePath) {
  const res = await fetch(`${API_BASE}/api/datasets/load`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ filePath, normalize: false }),
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({}))
    throw new Error(err.error || `Failed to load dataset (${res.status})`)
  }
  const data = await res.json()
  return data.points
}

export async function runAlgorithm(algo, body) {
  const res = await fetch(`${API_BASE}/api/kmeans/${algo}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({}))
    throw new Error(err.error || `${algo} failed (${res.status})`)
  }
  return res.json()
}
