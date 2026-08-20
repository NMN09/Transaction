import type {
  AdminUserResponse,
  AmountRequest,
  ErrorResponse,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  TransactionResponse,
  TransferRequest,
  UpdateProfileRequest,
  UserProfileResponse,
  WalletResponse,
} from '../types/wallet'
import { ApiError, type WalletApi } from './walletApi'

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, '')

const fallbackError = (response: Response): ErrorResponse => ({
  timestamp: new Date().toISOString(),
  status: response.status,
  code: response.status === 401 ? 'UNAUTHORIZED' : 'REQUEST_FAILED',
  message: response.statusText || 'The server could not complete the request.',
  path: new URL(response.url).pathname,
})

export class HttpWalletApi implements WalletApi {
  private accessToken: string | null = null

  setAccessToken(token: string | null) {
    this.accessToken = token
  }

  register(request: RegisterRequest) {
    return this.request<RegisterResponse>('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify(request),
    })
  }

  login(request: LoginRequest) {
    return this.request<LoginResponse>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify(request),
    })
  }

  getProfile() {
    return this.request<UserProfileResponse>('/api/users/me')
  }

  updateProfile(request: UpdateProfileRequest) {
    return this.request<UserProfileResponse>('/api/users/me', {
      method: 'PUT',
      body: JSON.stringify(request),
    })
  }

  getWallet() {
    return this.request<WalletResponse>('/api/wallet')
  }

  addMoney(request: AmountRequest) {
    return this.request<TransactionResponse>('/api/wallet/add-money', {
      method: 'POST',
      body: JSON.stringify(request),
    })
  }

  withdraw(request: AmountRequest) {
    return this.request<TransactionResponse>('/api/wallet/withdraw', {
      method: 'POST',
      body: JSON.stringify(request),
    })
  }

  transfer(request: TransferRequest) {
    return this.request<TransactionResponse>('/api/transactions/transfer', {
      method: 'POST',
      body: JSON.stringify(request),
    })
  }

  getTransactions() {
    return this.request<TransactionResponse[]>('/api/transactions')
  }

  getAdminUsers() {
    return this.request<AdminUserResponse[]>('/api/admin/users')
  }

  getAdminTransactions() {
    return this.request<TransactionResponse[]>('/api/admin/transactions')
  }

  freezeWallet(walletId: number) {
    return this.request<WalletResponse>(`/api/admin/wallets/${walletId}/freeze`, { method: 'PUT' })
  }

  activateWallet(walletId: number) {
    return this.request<WalletResponse>(`/api/admin/wallets/${walletId}/activate`, { method: 'PUT' })
  }

  private async request<T>(path: string, options: RequestInit = {}): Promise<T> {
    if (!apiBaseUrl) {
      throw new ApiError({
        timestamp: new Date().toISOString(),
        status: 500,
        code: 'CONFIGURATION_ERROR',
        message: 'VITE_API_BASE_URL is not configured. Add it to .env.development.',
        path,
      })
    }

    let response: Response
    try {
      response = await fetch(`${apiBaseUrl}${path}`, {
        ...options,
        headers: {
          Accept: 'application/json',
          ...(options.body ? { 'Content-Type': 'application/json' } : {}),
          ...(this.accessToken ? { Authorization: `Bearer ${this.accessToken}` } : {}),
          ...options.headers,
        },
      })
    } catch {
      throw new ApiError({
        timestamp: new Date().toISOString(),
        status: 0,
        code: 'NETWORK_ERROR',
        message: 'Cannot reach the wallet server. Start Spring Boot and try again.',
        path,
      })
    }

    const body = await this.readBody(response)
    if (!response.ok) {
      throw new ApiError(this.isErrorResponse(body) ? body : fallbackError(response))
    }

    return body as T
  }

  private async readBody(response: Response): Promise<unknown> {
    if (response.status === 204) return undefined
    const contentType = response.headers.get('content-type') ?? ''
    return contentType.includes('application/json') ? response.json() : response.text()
  }

  private isErrorResponse(value: unknown): value is ErrorResponse {
    return typeof value === 'object'
      && value !== null
      && 'status' in value
      && 'code' in value
      && 'message' in value
      && 'path' in value
  }
}

export const httpWalletApi = new HttpWalletApi()
