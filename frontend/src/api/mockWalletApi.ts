import { ApiError, type WalletApi } from './walletApi'
import { seedDatabase, type MockDatabase, type MockTransaction } from '../data/mockData'
import type {
  AdminUserResponse,
  AmountRequest,
  ErrorResponse,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  TransactionDirection,
  TransactionResponse,
  TransferRequest,
  UpdateProfileRequest,
  UserProfileResponse,
  WalletResponse,
} from '../types/wallet'

const DB_KEY = 'vaultpay_mock_database_v1'
const MOCK_DELAY = 180

const cloneSeed = (): MockDatabase => structuredClone(seedDatabase)

const wait = () => new Promise((resolve) => window.setTimeout(resolve, MOCK_DELAY))

const money = (value: number) => Math.round((value + Number.EPSILON) * 100) / 100

const makeError = (status: number, code: string, message: string, path: string) =>
  new ApiError({
    timestamp: new Date().toISOString(),
    status,
    code,
    message,
    path,
  } satisfies ErrorResponse)

export class MockWalletApi implements WalletApi {
  private token: string | null = null

  setAccessToken(token: string | null) {
    this.token = token
  }

  async register(request: RegisterRequest): Promise<RegisterResponse> {
    await wait()
    const db = this.readDb()
    const email = request.email.trim().toLowerCase()
    const phone = request.phone.trim()

    if (db.users.some((user) => user.email === email)) {
      throw makeError(409, 'CONFLICT', 'Email already exists', '/api/auth/register')
    }
    if (db.users.some((user) => user.phone === phone)) {
      throw makeError(409, 'CONFLICT', 'Phone number already exists', '/api/auth/register')
    }

    const nextUserId = Math.max(...db.users.map((user) => user.id)) + 1
    const nextWalletId = Math.max(...db.wallets.map((wallet) => wallet.id)) + 1
    db.users.push({
      id: nextUserId,
      name: request.name.trim(),
      email,
      phone,
      password: request.password,
      role: 'USER',
      createdAt: new Date().toISOString(),
    })
    db.wallets.push({ id: nextWalletId, userId: nextUserId, balance: 0, status: 'ACTIVE' })
    this.writeDb(db)
    return { message: 'Registration successful' }
  }

  async login(request: LoginRequest): Promise<LoginResponse> {
    await wait()
    const email = request.email.trim().toLowerCase()
    const user = this.readDb().users.find((candidate) => candidate.email === email)
    if (!user || user.password !== request.password) {
      throw makeError(401, 'UNAUTHORIZED', 'Invalid email or password', '/api/auth/login')
    }
    return { token: `mock-session:${user.id}`, expiresIn: 3600 }
  }

  async getProfile(): Promise<UserProfileResponse> {
    await wait()
    return this.toProfile(this.currentUser(this.readDb()))
  }

  async updateProfile(request: UpdateProfileRequest): Promise<UserProfileResponse> {
    await wait()
    const db = this.readDb()
    const user = this.currentUser(db)
    const phone = request.phone.trim()
    if (db.users.some((candidate) => candidate.id !== user.id && candidate.phone === phone)) {
      throw makeError(409, 'CONFLICT', 'Phone number already exists', '/api/users/me')
    }
    user.name = request.name.trim()
    user.phone = phone
    this.writeDb(db)
    return this.toProfile(user)
  }

  async getWallet(): Promise<WalletResponse> {
    await wait()
    const db = this.readDb()
    return this.toWallet(this.currentWallet(db))
  }

  async addMoney(request: AmountRequest): Promise<TransactionResponse> {
    await wait()
    const db = this.readDb()
    const wallet = this.currentWallet(db)
    this.assertAmount(request.amount, '/api/wallet/add-money')
    this.assertActive(wallet.status, 'Wallet is frozen', '/api/wallet/add-money')
    wallet.balance = money(wallet.balance + request.amount)
    const transaction = this.createTransaction({
      senderWalletId: null,
      receiverWalletId: wallet.id,
      amount: request.amount,
      type: 'ADD_MONEY',
      remarks: 'Added money to wallet',
    })
    db.transactions.unshift(transaction)
    this.writeDb(db)
    return this.toTransaction(transaction, wallet.id, db)
  }

  async withdraw(request: AmountRequest): Promise<TransactionResponse> {
    await wait()
    const db = this.readDb()
    const wallet = this.currentWallet(db)
    this.assertAmount(request.amount, '/api/wallet/withdraw')
    this.assertActive(wallet.status, 'Wallet is frozen', '/api/wallet/withdraw')
    if (wallet.balance < request.amount) {
      throw makeError(400, 'INSUFFICIENT_BALANCE', 'Insufficient wallet balance', '/api/wallet/withdraw')
    }
    wallet.balance = money(wallet.balance - request.amount)
    const transaction = this.createTransaction({
      senderWalletId: wallet.id,
      receiverWalletId: null,
      amount: request.amount,
      type: 'WITHDRAW',
      remarks: 'Withdrew money from wallet',
    })
    db.transactions.unshift(transaction)
    this.writeDb(db)
    return this.toTransaction(transaction, wallet.id, db)
  }

  async transfer(request: TransferRequest): Promise<TransactionResponse> {
    await wait()
    const db = this.readDb()
    const sender = this.currentUser(db)
    const senderWallet = this.currentWallet(db)
    this.assertAmount(request.amount, '/api/transactions/transfer')
    const receiver = db.users.find(
      (candidate) => candidate.email === request.receiverEmail.trim().toLowerCase(),
    )
    if (!receiver) {
      throw makeError(404, 'NOT_FOUND', 'Receiver not found', '/api/transactions/transfer')
    }
    if (receiver.id === sender.id) {
      throw makeError(400, 'SELF_TRANSFER', 'Cannot transfer money to yourself', '/api/transactions/transfer')
    }
    const receiverWallet = db.wallets.find((wallet) => wallet.userId === receiver.id)
    if (!receiverWallet) {
      throw makeError(404, 'NOT_FOUND', 'Receiver wallet not found', '/api/transactions/transfer')
    }
    this.assertActive(senderWallet.status, 'Sender wallet is frozen', '/api/transactions/transfer')
    this.assertActive(receiverWallet.status, 'Receiver wallet is frozen', '/api/transactions/transfer')
    if (senderWallet.balance < request.amount) {
      throw makeError(400, 'INSUFFICIENT_BALANCE', 'Insufficient wallet balance', '/api/transactions/transfer')
    }
    senderWallet.balance = money(senderWallet.balance - request.amount)
    receiverWallet.balance = money(receiverWallet.balance + request.amount)
    const transaction = this.createTransaction({
      senderWalletId: senderWallet.id,
      receiverWalletId: receiverWallet.id,
      amount: request.amount,
      type: 'TRANSFER',
      remarks: request.remarks?.trim() || 'Money transfer',
    })
    db.transactions.unshift(transaction)
    this.writeDb(db)
    return this.toTransaction(transaction, senderWallet.id, db)
  }

  async getTransactions(): Promise<TransactionResponse[]> {
    await wait()
    const db = this.readDb()
    const wallet = this.currentWallet(db)
    return db.transactions
      .filter((tx) => tx.senderWalletId === wallet.id || tx.receiverWalletId === wallet.id)
      .sort((a, b) => Date.parse(b.createdAt) - Date.parse(a.createdAt))
      .map((tx) => this.toTransaction(tx, wallet.id, db))
  }

  async getAdminUsers(): Promise<AdminUserResponse[]> {
    await wait()
    const db = this.readDb()
    this.assertAdmin(db)
    return db.users.map((user) => {
      const wallet = db.wallets.find((candidate) => candidate.userId === user.id)
      return {
        ...this.toProfile(user),
        walletId: wallet?.id ?? null,
        walletStatus: wallet?.status ?? null,
      }
    })
  }

  async getAdminTransactions(): Promise<TransactionResponse[]> {
    await wait()
    const db = this.readDb()
    this.assertAdmin(db)
    return db.transactions
      .sort((a, b) => Date.parse(b.createdAt) - Date.parse(a.createdAt))
      .map((tx) => ({
        referenceId: tx.referenceId,
        type: tx.type,
        amount: tx.amount,
        status: tx.status,
        remarks: tx.remarks,
        createdAt: tx.createdAt,
      }))
  }

  async freezeWallet(walletId: number): Promise<WalletResponse> {
    return this.setWalletStatus(walletId, 'FROZEN', '/api/admin/wallets/{walletId}/freeze')
  }

  async activateWallet(walletId: number): Promise<WalletResponse> {
    return this.setWalletStatus(walletId, 'ACTIVE', '/api/admin/wallets/{walletId}/activate')
  }

  private async setWalletStatus(
    walletId: number,
    status: 'ACTIVE' | 'FROZEN',
    path: string,
  ): Promise<WalletResponse> {
    await wait()
    const db = this.readDb()
    this.assertAdmin(db)
    const wallet = db.wallets.find((candidate) => candidate.id === walletId)
    if (!wallet) throw makeError(404, 'NOT_FOUND', `Wallet not found with id: ${walletId}`, path)
    wallet.status = status
    this.writeDb(db)
    return this.toWallet(wallet)
  }

  private currentUser(db: MockDatabase) {
    const match = this.token?.match(/^mock-session:(\d+)$/)
    const user = match ? db.users.find((candidate) => candidate.id === Number(match[1])) : undefined
    if (!user) throw makeError(401, 'UNAUTHORIZED', 'Full authentication is required', '/api')
    return user
  }

  private currentWallet(db: MockDatabase) {
    const user = this.currentUser(db)
    const wallet = db.wallets.find((candidate) => candidate.userId === user.id)
    if (!wallet) throw makeError(404, 'NOT_FOUND', 'Wallet not found', '/api/wallet')
    return wallet
  }

  private assertAdmin(db: MockDatabase) {
    if (this.currentUser(db).role !== 'ADMIN') {
      throw makeError(403, 'FORBIDDEN', 'Access denied: insufficient permissions', '/api/admin')
    }
  }

  private assertAmount(amount: number, path: string) {
    if (!Number.isFinite(amount) || amount <= 0 || Math.round(amount * 100) !== amount * 100) {
      throw makeError(400, 'VALIDATION_ERROR', 'Enter an amount greater than 0 with at most 2 decimal places', path)
    }
  }

  private assertActive(status: 'ACTIVE' | 'FROZEN', message: string, path: string) {
    if (status === 'FROZEN') throw makeError(403, 'WALLET_FROZEN', message, path)
  }

  private createTransaction(
    input: Omit<MockTransaction, 'referenceId' | 'status' | 'createdAt'>,
  ): MockTransaction {
    return {
      ...input,
      referenceId: `TX-${crypto.randomUUID().replaceAll('-', '').slice(0, 12).toUpperCase()}`,
      status: 'SUCCESS',
      createdAt: new Date().toISOString(),
    }
  }

  private toProfile(user: MockDatabase['users'][number]): UserProfileResponse {
    return {
      id: user.id,
      name: user.name,
      email: user.email,
      phone: user.phone,
      role: user.role,
      createdAt: user.createdAt,
    }
  }

  private toWallet(wallet: MockDatabase['wallets'][number]): WalletResponse {
    return { walletId: wallet.id, balance: wallet.balance, status: wallet.status }
  }

  private toTransaction(
    tx: MockTransaction,
    viewerWalletId: number,
    db: MockDatabase,
  ): TransactionResponse {
    let direction: TransactionDirection
    let counterpartyEmail: string | null = null
    let counterpartyName: string | null
    if (tx.type === 'ADD_MONEY') {
      direction = 'CREDIT'
      counterpartyName = 'Deposit'
    } else if (tx.type === 'WITHDRAW') {
      direction = 'DEBIT'
      counterpartyName = 'Withdrawal'
    } else {
      direction = tx.senderWalletId === viewerWalletId ? 'DEBIT' : 'CREDIT'
      const counterpartyWalletId = direction === 'DEBIT' ? tx.receiverWalletId : tx.senderWalletId
      const counterpartyWallet = db.wallets.find((wallet) => wallet.id === counterpartyWalletId)
      const counterparty = db.users.find((user) => user.id === counterpartyWallet?.userId)
      counterpartyName = counterparty?.name ?? 'Wallet user'
      counterpartyEmail = counterparty?.email ?? null
    }
    return {
      referenceId: tx.referenceId,
      type: tx.type,
      amount: tx.amount,
      direction,
      counterpartyEmail,
      counterpartyName,
      status: tx.status,
      remarks: tx.remarks,
      createdAt: tx.createdAt,
    }
  }

  private readDb(): MockDatabase {
    const stored = localStorage.getItem(DB_KEY)
    if (!stored) {
      const seed = cloneSeed()
      this.writeDb(seed)
      return seed
    }
    return JSON.parse(stored) as MockDatabase
  }

  private writeDb(db: MockDatabase) {
    localStorage.setItem(DB_KEY, JSON.stringify(db))
  }
}

export const resetMockDatabase = () => localStorage.removeItem(DB_KEY)
export const mockWalletApi = new MockWalletApi()
