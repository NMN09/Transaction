import { Navigate, Route, Routes, useLocation } from 'react-router-dom'
import { useEffect, type PropsWithChildren } from 'react'
import { ToastRegion } from './components/ToastRegion'
import { useWalletApp } from './hooks/useWalletApp'
import { AppShell } from './layouts/AppShell'
import { AdminPage } from './pages/AdminPage'
import { AuthPage } from './pages/AuthPage'
import { DashboardPage } from './pages/DashboardPage'
import { NotFoundPage } from './pages/NotFoundPage'
import { ProfilePage } from './pages/ProfilePage'
import { TransactionsPage } from './pages/TransactionsPage'

function RouteFocus() {
  const location = useLocation()
  useEffect(() => {
    window.requestAnimationFrame(() => document.getElementById('main-content')?.focus())
  }, [location.pathname])
  return null
}

function ProtectedRoute({ children }: PropsWithChildren) {
  const { session, booting } = useWalletApp()
  if (booting) return <div className="app-loading" role="status"><span /><p>Opening your ledger…</p></div>
  if (!session) return <Navigate to="/login" replace />
  return children
}

function AdminRoute({ children }: PropsWithChildren) {
  const { session } = useWalletApp()
  if (session?.user.role !== 'ADMIN') return <Navigate to="/app" replace />
  return children
}

export default function App() {
  return (
    <>
      <RouteFocus />
      <Routes>
        <Route path="/login" element={<AuthPage />} />
        <Route path="/" element={<Navigate to="/app" replace />} />
        <Route path="/app" element={<ProtectedRoute><AppShell /></ProtectedRoute>}>
          <Route index element={<DashboardPage />} />
          <Route path="transactions" element={<TransactionsPage />} />
          <Route path="profile" element={<ProfilePage />} />
          <Route path="admin" element={<AdminRoute><AdminPage /></AdminRoute>} />
        </Route>
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
      <ToastRegion />
    </>
  )
}
