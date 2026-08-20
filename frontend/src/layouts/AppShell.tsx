import {
  ArrowLeftRight,
  LayoutDashboard,
  LogOut,
  Menu,
  ShieldCheck,
  UserRound,
  WalletCards,
  X,
} from 'lucide-react'
import { useState } from 'react'
import { NavLink, Outlet, useLocation } from 'react-router-dom'
import { useWalletApp } from '../hooks/useWalletApp'
import { StatusBadge } from '../components/StatusBadge'

const navItems = [
  { to: '/app', label: 'Overview', icon: LayoutDashboard, end: true },
  { to: '/app/transactions', label: 'Transactions', icon: ArrowLeftRight },
  { to: '/app/profile', label: 'Profile', icon: UserRound },
]

export function AppShell() {
  const { session, wallet, logout } = useWalletApp()
  const [open, setOpen] = useState(false)
  const location = useLocation()

  const closeNav = () => setOpen(false)

  return (
    <div className="app-shell">
      <aside className={`sidebar ${open ? 'sidebar--open' : ''}`} aria-label="Primary navigation">
        <div className="brand">
          <span className="brand__mark" aria-hidden="true"><WalletCards /></span>
          <span><strong>VaultPay</strong><small>Living ledger</small></span>
        </div>
        <button className="sidebar__close icon-button" onClick={closeNav} aria-label="Close navigation">
          <X aria-hidden="true" />
        </button>
        <nav>
          <p className="sidebar__label">Personal wallet</p>
          {navItems.map(({ to, label, icon: Icon, end }) => (
            <NavLink key={to} to={to} end={end} onClick={closeNav}>
              <Icon aria-hidden="true" />
              <span>{label}</span>
            </NavLink>
          ))}
          {session?.user.role === 'ADMIN' ? (
            <>
              <p className="sidebar__label">Administration</p>
              <NavLink to="/app/admin" onClick={closeNav}>
                <ShieldCheck aria-hidden="true" />
                <span>Admin console</span>
              </NavLink>
            </>
          ) : null}
        </nav>
        <div className="sidebar__account">
          <div className="avatar" aria-hidden="true">{session?.user.name.split(' ').map((part) => part[0]).slice(0, 2).join('')}</div>
          <div>
            <strong>{session?.user.name}</strong>
            <span>{session?.user.role === 'ADMIN' ? 'Administrator' : 'Wallet member'}</span>
          </div>
          <button className="icon-button" onClick={logout} aria-label="Sign out">
            <LogOut aria-hidden="true" />
          </button>
        </div>
      </aside>
      {open ? <button className="sidebar-scrim" onClick={closeNav} aria-label="Close navigation overlay" /> : null}
      <div className="app-main">
        <header className="topbar">
          <button className="menu-button icon-button" onClick={() => setOpen(true)} aria-label="Open navigation">
            <Menu aria-hidden="true" />
          </button>
          <div>
            <p className="eyebrow">{location.pathname.includes('admin') ? 'Platform operations' : 'Personal wallet'}</p>
            <span className="topbar__greeting">Good to see you, {session?.user.name.split(' ')[0]}</span>
          </div>
          <div className="topbar__status">
            {wallet ? <StatusBadge status={wallet.status} /> : null}
            <span className="avatar avatar--small" aria-hidden="true">{session?.user.name[0]}</span>
          </div>
        </header>
        <main id="main-content" tabIndex={-1}>
          <Outlet />
        </main>
      </div>
    </div>
  )
}
