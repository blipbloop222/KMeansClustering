// HSL color palette with one distinct hue per cluster id (0 … k−1).

export function getClusterColorPalette(k) {
  const n = Math.max(1, Math.min(k, 20))
  return Array.from({ length: n }, (_, i) => {
    const hue = Math.round((i * 360) / n)
    return `hsl(${hue} 62% 48%)`
  })
}
