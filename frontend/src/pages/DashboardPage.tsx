import { ArrowDownToLine, ArrowRight, ArrowUpRight, Plus, ReceiptText } from 'lucide-react'
import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { TransactionTable } from '../components/TransactionTable'
import { WalletActionModal, type WalletAction } from '../components/WalletActionModal'
import { useWalletApp } from '../hooks/useWalletApp'

const currency = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  minimumFractionDigits: 2,
})

export function DashboardPage() {
  const { wallet, transactions, session } = useWalletApp()
  const [action, setAction] = useState<WalletAction | null>(null)

  const movement = useMemo(() => {
    return transactions.reduce(
      (totals, transaction) => {
        if (transaction.direction === 'CREDIT') totals.incoming += transaction.amount
        if (transaction.direction === 'DEBIT') totals.outgoing += transaction.amount
        return totals
      },
      { incoming: 0, outgoing: 0 },
    )
  }, [transactions])

  return (
    <div className="page dashboard-page">
      <section className="balance-hero" aria-labelledby="balance-title">
        <div className="balance-hero__rail" aria-hidden="true">
          <span /><span /><span /><span />
        </div>
        <div className="balance-hero__main">
          <p className="eyebrow">Available balance</p>
          <h1 id="balance-title">{currency.format(wallet?.balance ?? 0)}</h1>
          <div className="balance-hero__meta">
            <span>Wallet #{wallet?.walletId}</span>
            <span>Updated just now</span>
          </div>
        </div>
        <div className="balance-hero__note">
          <ReceiptText aria-hidden="true" />
          <div><strong>Every movement is visible.</strong><span>Your ledger records deposits, withdrawals, and transfers.</span></div>
        </div>
      </section>

      <section className="quick-actions" aria-labelledby="quick-actions-title">
        <div className="section-heading">
          <div><p className="eyebrow">Move money</p><h2 id="quick-actions-title">Wallet actions</h2></div>
          {wallet?.status === 'FROZEN' ? <p className="section-note">Your wallet is frozen. Ask an administrator to reactivate it.</p> : null}
        </div>
        <div className="action-grid">
          <button onClick={() => setAction('add')} disabled={wallet?.status === 'FROZEN'}>
            <span className="action-grid__icon"><Plus aria-hidden="true" /></span>
            <span><strong>Add money</strong><small>Increase your simulated balance</small></span>
            <ArrowRight aria-hidden="true" />
          </button>
          <button onClick={() => setAction('transfer')} disabled={wallet?.status === 'FROZEN'}>
            <span className="action-grid__icon"><ArrowUpRight aria-hidden="true" /></span>
            <span><strong>Send money</strong><small>Transfer to a registered email</small></span>
            <ArrowRight aria-hidden="true" />
          </button>
          <button onClick={() => setAction('withdraw')} disabled={wallet?.status === 'FROZEN'}>
            <span className="action-grid__icon"><ArrowDownToLine aria-hidden="true" /></span>
            <span><strong>Withdraw</strong><small>Remove funds from this wallet</small></span>
            <ArrowRight aria-hidden="true" />
          </button>
        </div>
      </section>

      <section className="movement-grid" aria-label="Wallet movement summary">
        <article><span>Money in</span><strong className="money--credit">+{currency.format(movement.incoming)}</strong><small>Across {transactions.filter((tx) => tx.direction === 'CREDIT').length} ledger entries</small></article>
        <article><span>Money out</span><strong className="money--debit">−{currency.format(movement.outgoing)}</strong><small>Across {transactions.filter((tx) => tx.direction === 'DEBIT').length} ledger entries</small></article>
        <article><span>Account role</span><strong>{session?.user.role === 'ADMIN' ? 'Administrator' : 'Wallet member'}</strong><small>Controls are matched to your permissions</small></article>
      </section>

      <section className="panel recent-panel" aria-labelledby="recent-title">
        <div className="section-heading section-heading--row">
          <div><p className="eyebrow">Living ledger</p><h2 id="recent-title">Recent activity</h2></div>
          <Link className="text-link" to="/app/transactions">View all <ArrowRight aria-hidden="true" /></Link>
        </div>
        <TransactionTable transactions={transactions.slice(0, 5)} compact />
      </section>
      <WalletActionModal action={action} onClose={() => setAction(null)} />
    </div>
  )
}
