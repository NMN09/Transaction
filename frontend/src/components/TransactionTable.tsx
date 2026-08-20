import { ArrowDownLeft, ArrowUpRight, Landmark } from 'lucide-react'
import type { TransactionResponse } from '../types/wallet'

const currency = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  minimumFractionDigits: 2,
})

const dateTime = new Intl.DateTimeFormat('en-IN', {
  dateStyle: 'medium',
  timeStyle: 'short',
})

export function TransactionTable({
  transactions,
  compact = false,
  admin = false,
}: {
  transactions: TransactionResponse[]
  compact?: boolean
  admin?: boolean
}) {
  if (!transactions.length) {
    return (
      <div className="empty-state">
        <Landmark aria-hidden="true" />
        <h3>No transactions yet</h3>
        <p>Your completed wallet activity will appear here.</p>
      </div>
    )
  }

  return (
    <div className="transaction-table-wrap">
      <table className={`transaction-table ${compact ? 'transaction-table--compact' : ''}`}>
        <thead>
          <tr>
            {!admin ? <th scope="col">Flow</th> : null}
            <th scope="col">Transaction</th>
            <th scope="col">Reference</th>
            <th scope="col">Date</th>
            <th scope="col" className="align-right">Amount</th>
          </tr>
        </thead>
        <tbody>
          {transactions.map((transaction) => {
            const credit = transaction.direction === 'CREDIT'
            const FlowIcon = credit ? ArrowDownLeft : ArrowUpRight
            return (
              <tr key={transaction.referenceId}>
                {!admin ? (
                  <td data-label="Flow">
                    <span className={`flow flow--${credit ? 'credit' : 'debit'}`}>
                      <FlowIcon aria-hidden="true" />
                      {credit ? 'Credit' : 'Debit'}
                    </span>
                  </td>
                ) : null}
                <td data-label="Transaction">
                  <strong>{transaction.counterpartyName || transaction.type.replace('_', ' ')}</strong>
                  <span>{transaction.remarks || 'No remarks'}</span>
                </td>
                <td data-label="Reference">
                  <code>{transaction.referenceId}</code>
                </td>
                <td data-label="Date">
                  <time dateTime={transaction.createdAt}>{dateTime.format(new Date(transaction.createdAt))}</time>
                </td>
                <td data-label="Amount" className={`money align-right ${!admin ? (credit ? 'money--credit' : 'money--debit') : ''}`}>
                  {!admin ? (credit ? '+' : '−') : ''}{currency.format(transaction.amount)}
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}
