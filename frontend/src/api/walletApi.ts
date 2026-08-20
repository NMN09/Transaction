import type {
  AdminUserResponse,
  AmountRequest,
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

/**
 * The only boundary the future Spring integration needs to replace.
 * Method names and payloads mirror the current backend controllers and DTOs.
 */
export interface WalletApi {
  setAccessToken(token: string | null): void
  register(request: RegisterRequest): Promise<RegisterResponse>
  login(request: LoginRequest): Promise<LoginResponse>
  getProfile(): Promise<UserProfileResponse>
  updateProfile(request: UpdateProfileRequest): Promise<UserProfileResponse>
  getWallet(): Promise<WalletResponse>
  addMoney(request: AmountRequest): Promise<TransactionResponse>
  withdraw(request: AmountRequest): Promise<TransactionResponse>
  transfer(request: TransferRequest): Promise<TransactionResponse>
  getTransactions(): Promise<TransactionResponse[]>
  getAdminUsers(): Promise<AdminUserResponse[]>
  getAdminTransactions(): Promise<TransactionResponse[]>
  freezeWallet(walletId: number): Promise<WalletResponse>
  activateWallet(walletId: number): Promise<WalletResponse>
}

export class ApiError extends Error {
  constructor(public readonly response: import('../types/wallet').ErrorResponse) {
    super(response.message)
    this.name = 'ApiError'
  }
}
