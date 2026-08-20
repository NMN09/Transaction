import { Search, ShieldCheck, ShieldOff, UsersRound } from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import { Button } from '../components/Button'
import { Field } from '../components/Field'
import { StatusBadge } from '../components/StatusBadge'
import { TransactionTable } from '../components/TransactionTable'
import { useWalletApp } from '../hooks/useWalletApp'

type AdminTab = 'users' | 'transactions'

export function AdminPage() {
  const {
    adminUsers,
    adminTransactions,
    loadAdminData,
    setWalletStatus,
    working,
  } = useWalletApp()
  const [tab, setTab] = useState<AdminTab>('users')
  const [query, setQuery] = useState('')

  useEffect(() => {
    void loadAdminData()
  }, [loadAdminData])

  const filteredUsers = useMemo(() => {
    const normalized = query.trim().toLowerCase()
    return adminUsers.filter((user) =>
      !normalized || [user.name, user.email, user.phone, String(user.walletId ?? '')].some((value) => value.toLowerCase().includes(normalized)),
    )
  }, [adminUsers, query])

  const filteredTransactions = useMemo(() => {
    const normalized = query.trim().toLowerCase()
    return adminTransactions.filter((transaction) =>
      !normalized || [transaction.referenceId, transaction.type, transaction.remarks].some((value) => value?.toLowerCase().includes(normalized)),
    )
  }, [adminTransactions, query])

  return (
    <div className="page admin-page">
      <header className="page-heading">
        <div><p className="eyebrow">Restricted workspace</p><h1>Admin console</h1></div>
        <p>Review users and the platform ledger, then freeze or activate wallets when required.</p>
      </header>
      <section className="admin-summary" aria-label="Platform summary">
        <article><UsersRound aria-hidden="true" /><span><strong>{adminUsers.length}</strong><small>Registered users</small></span></article>
        <article><ShieldCheck aria-hidden="true" /><span><strong>{adminUsers.filter((user) => user.walletStatus === 'ACTIVE').length}</strong><small>Active wallets</small></span></article>
        <article><ShieldOff aria-hidden="true" /><span><strong>{adminUsers.filter((user) => user.walletStatus === 'FROZEN').length}</strong><small>Frozen wallets</small></span></article>
      </section>
      <section className="panel admin-panel">
        <div className="admin-toolbar">
          <div className="segmented" role="tablist" aria-label="Admin data view">
            <button role="tab" aria-selected={tab === 'users'} onClick={() => { setTab('users'); setQuery('') }}>Users</button>
            <button role="tab" aria-selected={tab === 'transactions'} onClick={() => { setTab('transactions'); setQuery('') }}>Transactions</button>
          </div>
          <Field
            className="search-field"
            label={`Search ${tab}`}
            name="admin-search"
            type="search"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder={tab === 'users' ? 'Name, email, phone, wallet' : 'Reference, type, remarks'}
            suffix={<Search aria-hidden="true" />}
          />
        </div>
        {tab === 'users' ? (
          <div className="user-table-wrap">
            <table className="user-table">
              <thead><tr><th scope="col">User</th><th scope="col">Contact</th><th scope="col">Role</th><th scope="col">Wallet</th><th scope="col">Status</th><th scope="col"><span className="sr-only">Actions</span></th></tr></thead>
              <tbody>
                {filteredUsers.map((user) => (
                  <tr key={user.id}>
                    <td data-label="User"><strong>{user.name}</strong><span>User #{user.id}</span></td>
                    <td data-label="Contact"><strong>{user.email}</strong><span>{user.phone}</span></td>
                    <td data-label="Role"><span className="role-badge">{user.role}</span></td>
                    <td data-label="Wallet"><code>#{user.walletId ?? '—'}</code></td>
                    <td data-label="Status">{user.walletStatus ? <StatusBadge status={user.walletStatus} /> : '—'}</td>
                    <td data-label="Action" className="align-right">
                      {user.walletId && user.walletStatus ? (
                        <Button
                          tone={user.walletStatus === 'ACTIVE' ? 'danger' : 'secondary'}
                          busy={working}
                          onClick={() => void setWalletStatus(user.walletId!, user.walletStatus === 'ACTIVE' ? 'FROZEN' : 'ACTIVE')}
                        >
                          {user.walletStatus === 'ACTIVE' ? 'Freeze wallet' : 'Activate wallet'}
                        </Button>
                      ) : null}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <TransactionTable transactions={filteredTransactions} admin />
        )}
      </section>
    </div>
  )
}
