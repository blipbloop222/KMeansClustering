// Mobile-only control to reopen the config sidebar.

export function MobileConfigBar({ onOpenSidebar }) {
  return (
    <div className="flex items-center justify-between mb-4 md:hidden">
      <button
        onClick={onOpenSidebar}
        className="font-mono text-xs text-[var(--text-muted)] border border-[var(--border)] px-2 py-1"
      >
        Config
      </button>
    </div>
  )
}
