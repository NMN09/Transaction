export type Role = 'USER' | 'ADMIN'
export type WalletStatus = 'ACTIVE' | 'FROZEN'
export type TransactionType = 'ADD_MONEY' | 'WITHDRAW' | 'TRANSFER'
export type TransactionStatus = 'SUCCESS' | 'FAILED'
export type TransactionDirection = 'CREDIT' | 'DEBIT'

export interface RegisterRequest {
  name: string
  email: string
  phone: string
  password: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  token: string
  expiresIn: number
}

export interface RegisterResponse {
  message: string
}

export interface UpdateProfileRequest {
  name: string
  phone: string
}

export interface AmountRequest {
  amount: number
}

export interface TransferRequest {
  receiverEmail: string
  amount: number
  remarks?: string
}

export interface UserProfileResponse {
  id: number
  name: string
  email: string
  phone: string
  role: Role
  createdAt: string
}

export interface WalletResponse {
  walletId: number
  balance: number
  status: WalletStatus
}

export interface TransactionResponse {
  referenceId: string
  type: TransactionType
  amount: number
  direction?: TransactionDirection
  counterpartyEmail?: string | null
  counterpartyName?: string | null
  status: TransactionStatus
  remarks?: string | null
  createdAt: string
}

export interface AdminUserResponse extends UserProfileResponse {
  walletId: number | null
  walletStatus: WalletStatus | null
}

export interface ErrorResponse {
  timestamp: string
  status: number
  code: string
  message: string
  path: string
}

export interface Session {
  token: string
  expiresAt: number
  user: UserProfileResponse
}

export interface ToastMessage {
  id: string
  tone: 'success' | 'error' | 'info'
  title: string
  message?: string
}
