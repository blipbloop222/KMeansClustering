// Compares cluster assignments and centroids across algorithm runs for correctness chips.

import { EPSILON } from '../constants/benchmark.js'

export function compareAssignments(a, b) {
  if (!a || !b || a.length !== b.length) return false
  return a.every((v, i) => v === b[i])
}

export function compareCentroids(c1, c2, eps = EPSILON) {
  if (!c1 || !c2 || c1.length !== c2.length) return false
  for (let i = 0; i < c1.length; i++) {
    for (let j = 0; j < c1[i].length; j++) {
      if (Math.abs(c1[i][j] - c2[i][j]) > eps) return false
    }
  }
  return true
}
