import { Search } from 'lucide-react'
import { useMemo, useState } from 'react'
import { Field, SelectField } from '../components/Field'
import { TransactionTable } from '../components/TransactionTable'
import { useWalletApp } from '../hooks/useWalletApp'

export function TransactionsPage() {
  const { transactions } = useWalletApp()
  const [query, setQuery] = useState('')
  const [flow, setFlow] = useState('ALL')
  const [type, setType] = useState('ALL')

  const filtered = useMemo(() => {
    const normalized = query.trim().toLowerCase()
    return transactions.filter((transaction) => {
      const matchesQuery = !normalized || [
        transaction.referenceId,
        transaction.remarks,
        transaction.counterpartyEmail,
        transaction.counterpartyName,
      ].some((value) => value?.toLowerCase().includes(normalized))
      const matchesFlow = flow === 'ALL' || transaction.direction === flow
      const matchesType = type === 'ALL' || transaction.type === type
      return matchesQuery && matchesFlow && matchesType
    })
  }, [flow, query, transactions, type])

  return (
    <div className="page">
      <header className="page-heading">
        <div><p className="eyebrow">Complete record</p><h1>Transactions</h1></div>
        <p>Search and review every movement involving your wallet. Filters apply to the loaded transaction history.</p>
      </header>
      <section className="panel ledger-panel" aria-labelledby="ledger-title">
        <div className="ledger-panel__heading">
          <div><h2 id="ledger-title">Wallet ledger</h2><span>{filtered.length} of {transactions.length} entries</span></div>
          <div className="ledger-filters">
            <Field
              className="search-field"
              label="Search transactions"
              name="transaction-search"
              type="search"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder="Reference, person, or remarks"
              suffix={<Search aria-hidden="true" />}
            />
            <SelectField label="Flow" name="flow" value={flow} onChange={(event) => setFlow(event.target.value)}>
              <option value="ALL">All flows</option>
              <option value="CREDIT">Credits</option>
              <option value="DEBIT">Debits</option>
            </SelectField>
            <SelectField label="Type" name="type" value={type} onChange={(event) => setType(event.target.value)}>
              <option value="ALL">All types</option>
              <option value="ADD_MONEY">Add money</option>
              <option value="WITHDRAW">Withdrawals</option>
              <option value="TRANSFER">Transfers</option>
            </SelectField>
          </div>
        </div>
        <TransactionTable transactions={filtered} />
      </section>
    </div>
  )
}
