import {
  useCallback,
  useEffect,
  useMemo,
  useState,
  type PropsWithChildren,
} from 'react'
import { httpWalletApi } from '../api/httpWalletApi'
import { ApiError, type WalletApi } from '../api/walletApi'
import type {
  AdminUserResponse,
  LoginRequest,
  RegisterRequest,
  Session,
  ToastMessage,
  TransactionResponse,
  TransferRequest,
  UpdateProfileRequest,
  WalletResponse,
} from '../types/wallet'
import { WalletAppContext, type WalletAppValue } from './walletAppContextValue'

const SESSION_KEY = 'vaultpay_session_v1'

const errorMessage = (error: unknown) =>
  error instanceof ApiError ? error.response.message : 'Something went wrong. Try again.'

export function WalletAppProvider({ children }: PropsWithChildren) {
  const api: WalletApi = httpWalletApi
  const [session, setSession] = useState<Session | null>(null)
  const [wallet, setWallet] = useState<WalletResponse | null>(null)
  const [transactions, setTransactions] = useState<TransactionResponse[]>([])
  const [adminUsers, setAdminUsers] = useState<AdminUserResponse[]>([])
  const [adminTransactions, setAdminTransactions] = useState<TransactionResponse[]>([])
  const [toasts, setToasts] = useState<ToastMessage[]>([])
  const [booting, setBooting] = useState(true)
  const [working, setWorking] = useState(false)

  const addToast = useCallback((toast: Omit<ToastMessage, 'id'>) => {
    const id = crypto.randomUUID()
    setToasts((current) => [...current, { ...toast, id }])
    window.setTimeout(() => {
      setToasts((current) => current.filter((item) => item.id !== id))
    }, 4500)
  }, [])

  const dismissToast = useCallback((id: string) => {
    setToasts((current) => current.filter((item) => item.id !== id))
  }, [])

  const clearSession = useCallback(() => {
    localStorage.removeItem(SESSION_KEY)
    api.setAccessToken(null)
    setSession(null)
    setWallet(null)
    setTransactions([])
    setAdminUsers([])
    setAdminTransactions([])
  }, [api])

  const refreshDashboard = useCallback(async () => {
    const [nextWallet, nextTransactions] = await Promise.all([
      api.getWallet(),
      api.getTransactions(),
    ])
    setWallet(nextWallet)
    setTransactions(nextTransactions)
  }, [api])

  useEffect(() => {
    const restore = async () => {
      const stored = localStorage.getItem(SESSION_KEY)
      if (!stored) {
        setBooting(false)
        return
      }
      try {
        const restored = JSON.parse(stored) as Session
        if (restored.expiresAt <= Date.now()) {
          clearSession()
          return
        }
        api.setAccessToken(restored.token)
        const user = await api.getProfile()
        const nextSession = { ...restored, user }
        setSession(nextSession)
        await refreshDashboard()
      } catch {
        clearSession()
      } finally {
        setBooting(false)
      }
    }
    void restore()
  }, [api, clearSession, refreshDashboard])

  useEffect(() => {
    if (!session) return
    const remaining = session.expiresAt - Date.now()
    const timer = window.setTimeout(() => {
      clearSession()
      addToast({
        tone: 'info',
        title: 'Session ended',
        message: 'Sign in again to continue.',
      })
    }, Math.max(0, remaining))
    return () => window.clearTimeout(timer)
  }, [addToast, clearSession, session])

  const login = useCallback(
    async (request: LoginRequest) => {
      setWorking(true)
      try {
        const response = await api.login(request)
        api.setAccessToken(response.token)
        const user = await api.getProfile()
        const nextSession: Session = {
          token: response.token,
          expiresAt: Date.now() + response.expiresIn * 1000,
          user,
        }
        localStorage.setItem(SESSION_KEY, JSON.stringify(nextSession))
        setSession(nextSession)
        await refreshDashboard()
        addToast({ tone: 'success', title: `Welcome back, ${user.name.split(' ')[0]}` })
      } finally {
        setWorking(false)
      }
    },
    [addToast, api, refreshDashboard],
  )

  const register = useCallback(
    async (request: RegisterRequest) => {
      setWorking(true)
      try {
        await api.register(request)
        addToast({
          tone: 'success',
          title: 'Account created',
          message: 'Sign in with your new account.',
        })
      } finally {
        setWorking(false)
      }
    },
    [addToast, api],
  )

  const logout = useCallback(() => {
    clearSession()
    addToast({ tone: 'info', title: 'Signed out' })
  }, [addToast, clearSession])

  const runMoneyAction = useCallback(
    async (action: () => Promise<TransactionResponse>, successTitle: string) => {
      setWorking(true)
      try {
        const transaction = await action()
        await refreshDashboard()
        addToast({ tone: 'success', title: successTitle, message: transaction.referenceId })
        return transaction
      } catch (error) {
        addToast({ tone: 'error', title: 'Action not completed', message: errorMessage(error) })
        throw error
      } finally {
        setWorking(false)
      }
    },
    [addToast, refreshDashboard],
  )

  const addMoney = useCallback(
    (amount: number) => runMoneyAction(() => api.addMoney({ amount }), 'Money added'),
    [api, runMoneyAction],
  )

  const withdraw = useCallback(
    (amount: number) => runMoneyAction(() => api.withdraw({ amount }), 'Withdrawal complete'),
    [api, runMoneyAction],
  )

  const transfer = useCallback(
    (request: TransferRequest) => runMoneyAction(() => api.transfer(request), 'Transfer complete'),
    [api, runMoneyAction],
  )

  const updateProfile = useCallback(
    async (request: UpdateProfileRequest) => {
      setWorking(true)
      try {
        const user = await api.updateProfile(request)
        setSession((current) => {
          if (!current) return current
          const next = { ...current, user }
          localStorage.setItem(SESSION_KEY, JSON.stringify(next))
          return next
        })
        addToast({ tone: 'success', title: 'Profile updated' })
      } catch (error) {
        addToast({ tone: 'error', title: 'Profile not updated', message: errorMessage(error) })
        throw error
      } finally {
        setWorking(false)
      }
    },
    [addToast, api],
  )

  const loadAdminData = useCallback(async () => {
    const [users, platformTransactions] = await Promise.all([
      api.getAdminUsers(),
      api.getAdminTransactions(),
    ])
    setAdminUsers(users)
    setAdminTransactions(platformTransactions)
  }, [api])

  const setWalletStatus = useCallback(
    async (walletId: number, status: 'ACTIVE' | 'FROZEN') => {
      setWorking(true)
      try {
        if (status === 'FROZEN') await api.freezeWallet(walletId)
        else await api.activateWallet(walletId)
        await loadAdminData()
        addToast({
          tone: 'success',
          title: status === 'FROZEN' ? 'Wallet frozen' : 'Wallet activated',
          message: `Wallet #${walletId} is now ${status.toLowerCase()}.`,
        })
      } finally {
        setWorking(false)
      }
    },
    [addToast, api, loadAdminData],
  )

  const value = useMemo<WalletAppValue>(
    () => ({
      session,
      wallet,
      transactions,
      adminUsers,
      adminTransactions,
      toasts,
      booting,
      working,
      login,
      register,
      logout,
      refreshDashboard,
      addMoney,
      withdraw,
      transfer,
      updateProfile,
      loadAdminData,
      setWalletStatus,
      dismissToast,
    }),
    [
      session,
      wallet,
      transactions,
      adminUsers,
      adminTransactions,
      toasts,
      booting,
      working,
      login,
      register,
      logout,
      refreshDashboard,
      addMoney,
      withdraw,
      transfer,
      updateProfile,
      loadAdminData,
      setWalletStatus,
      dismissToast,
    ],
  )

  return <WalletAppContext.Provider value={value}>{children}</WalletAppContext.Provider>
}
