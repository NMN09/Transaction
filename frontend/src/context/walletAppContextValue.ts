import { createContext } from 'react'
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

export interface WalletAppValue {
  session: Session | null
  wallet: WalletResponse | null
  transactions: TransactionResponse[]
  adminUsers: AdminUserResponse[]
  adminTransactions: TransactionResponse[]
  toasts: ToastMessage[]
  booting: boolean
  working: boolean
  login(request: LoginRequest): Promise<void>
  register(request: RegisterRequest): Promise<void>
  logout(): void
  refreshDashboard(): Promise<void>
  addMoney(amount: number): Promise<TransactionResponse>
  withdraw(amount: number): Promise<TransactionResponse>
  transfer(request: TransferRequest): Promise<TransactionResponse>
  updateProfile(request: UpdateProfileRequest): Promise<void>
  loadAdminData(): Promise<void>
  setWalletStatus(walletId: number, status: 'ACTIVE' | 'FROZEN'): Promise<void>
  dismissToast(id: string): void
}

export const WalletAppContext = createContext<WalletAppValue | null>(null)
