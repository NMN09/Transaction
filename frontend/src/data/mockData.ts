import type {
  Role,
  TransactionStatus,
  TransactionType,
  WalletStatus,
} from '../types/wallet'

export interface MockUser {
  id: number
  name: string
  email: string
  phone: string
  password: string
  role: Role
  createdAt: string
}

export interface MockWallet {
  id: number
  userId: number
  balance: number
  status: WalletStatus
}

export interface MockTransaction {
  referenceId: string
  senderWalletId: number | null
  receiverWalletId: number | null
  amount: number
  type: TransactionType
  status: TransactionStatus
  remarks: string
  createdAt: string
}

export interface MockDatabase {
  users: MockUser[]
  wallets: MockWallet[]
  transactions: MockTransaction[]
}

export const seedDatabase: MockDatabase = {
  users: [
    {
      id: 1,
      name: 'Naman Sharma',
      email: 'naman@vaultpay.dev',
      phone: '9876543210',
      password: 'Password123!',
      role: 'USER',
      createdAt: '2026-07-18T09:30:00Z',
    },
    {
      id: 2,
      name: 'Rahul Mehta',
      email: 'rahul@vaultpay.dev',
      phone: '9876543211',
      password: 'Password123!',
      role: 'USER',
      createdAt: '2026-07-22T11:10:00Z',
    },
    {
      id: 3,
      name: 'Aditi Rao',
      email: 'aditi@vaultpay.dev',
      phone: '9876543212',
      password: 'Password123!',
      role: 'USER',
      createdAt: '2026-08-01T07:45:00Z',
    },
    {
      id: 99,
      name: 'VaultPay Admin',
      email: 'admin@vaultpay.dev',
      phone: '9999999999',
      password: 'Admin123!',
      role: 'ADMIN',
      createdAt: '2026-07-01T06:00:00Z',
    },
  ],
  wallets: [
    { id: 101, userId: 1, balance: 24750, status: 'ACTIVE' },
    { id: 102, userId: 2, balance: 8200, status: 'ACTIVE' },
    { id: 103, userId: 3, balance: 4900, status: 'FROZEN' },
    { id: 199, userId: 99, balance: 0, status: 'ACTIVE' },
  ],
  transactions: [
    {
      referenceId: 'TX-81C9A2B4E731',
      senderWalletId: 101,
      receiverWalletId: 102,
      amount: 1250,
      type: 'TRANSFER',
      status: 'SUCCESS',
      remarks: 'Project lunch',
      createdAt: '2026-08-19T15:24:00Z',
    },
    {
      referenceId: 'TX-237B47D9C002',
      senderWalletId: null,
      receiverWalletId: 101,
      amount: 10000,
      type: 'ADD_MONEY',
      status: 'SUCCESS',
      remarks: 'Added money to wallet',
      createdAt: '2026-08-18T10:02:00Z',
    },
    {
      referenceId: 'TX-9AF024B138A0',
      senderWalletId: 102,
      receiverWalletId: 101,
      amount: 2000,
      type: 'TRANSFER',
      status: 'SUCCESS',
      remarks: 'Weekend tickets',
      createdAt: '2026-08-16T17:42:00Z',
    },
    {
      referenceId: 'TX-A73D943F570D',
      senderWalletId: 101,
      receiverWalletId: null,
      amount: 3000,
      type: 'WITHDRAW',
      status: 'SUCCESS',
      remarks: 'Withdrew money from wallet',
      createdAt: '2026-08-14T08:15:00Z',
    },
    {
      referenceId: 'TX-62FD0B917C31',
      senderWalletId: 101,
      receiverWalletId: 103,
      amount: 1750,
      type: 'TRANSFER',
      status: 'SUCCESS',
      remarks: 'Shared subscription',
      createdAt: '2026-08-10T13:20:00Z',
    },
  ],
}
