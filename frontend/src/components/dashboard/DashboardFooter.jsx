// Footer with API endpoint reference for the Java backend on port 8080.
export function DashboardFooter() {
  return (
    <footer className="pt-6 border-t border-[var(--border)] font-mono text-[10px] text-[var(--text-muted)]">
      API: POST /api/kmeans/&#123;sequential|concurrent|parallel&#125; · POST /api/datasets/load · port
      8080
    </footer>
  )
}
