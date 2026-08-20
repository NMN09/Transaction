import { ArrowLeft, FileQuestion } from 'lucide-react'
import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return (
    <main className="not-found" id="main-content">
      <FileQuestion aria-hidden="true" />
      <p className="eyebrow">404 · Route not found</p>
      <h1>This entry is not in the ledger.</h1>
      <p>The page may have moved, or the address may be incomplete.</p>
      <Link className="button button--primary" to="/app"><ArrowLeft aria-hidden="true" />Return to overview</Link>
    </main>
  )
}
