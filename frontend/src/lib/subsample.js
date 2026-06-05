//Randomly subsamples large point clouds so the scatter chart stays responsive.

export function subsamplePoints(points, assignments, max = 2000) {
  if (points.length <= 5000) return { points, assignments, subsampled: false }
  const indices = points.map((_, i) => i)
  for (let i = indices.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[indices[i], indices[j]] = [indices[j], indices[i]]
  }
  const picked = indices.slice(0, max)
  return {
    points: picked.map((i) => points[i]),
    assignments: picked.map((i) => assignments[i]),
    subsampled: true,
  }
}
