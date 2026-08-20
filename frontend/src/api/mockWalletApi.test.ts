import { beforeEach, describe, expect, it } from 'vitest'
import { MockWalletApi, resetMockDatabase } from './mockWalletApi'

describe('MockWalletApi', () => {
  let api: MockWalletApi

  beforeEach(() => {
    localStorage.clear()
    resetMockDatabase()
    api = new MockWalletApi()
  })

  it('authenticates a seeded user and returns their profile', async () => {
    const login = await api.login({
      email: 'naman@vaultpay.dev',
      password: 'Password123!',
    })
    api.setAccessToken(login.token)

    const profile = await api.getProfile()

    expect(profile.email).toBe('naman@vaultpay.dev')
    expect(profile.role).toBe('USER')
    expect(login.expiresIn).toBe(3600)
  })

  it('updates both mock wallets and writes one transfer entry', async () => {
    const login = await api.login({
      email: 'naman@vaultpay.dev',
      password: 'Password123!',
    })
    api.setAccessToken(login.token)
    const before = await api.getWallet()

    const transfer = await api.transfer({
      receiverEmail: 'rahul@vaultpay.dev',
      amount: 500,
      remarks: 'Test transfer',
    })
    const after = await api.getWallet()
    const matching = (await api.getTransactions()).filter(
      (transaction) => transaction.referenceId === transfer.referenceId,
    )

    expect(after.balance).toBe(before.balance - 500)
    expect(transfer.direction).toBe('DEBIT')
    expect(matching).toHaveLength(1)
  })

  it('blocks a normal user from admin operations', async () => {
    const login = await api.login({
      email: 'naman@vaultpay.dev',
      password: 'Password123!',
    })
    api.setAccessToken(login.token)

    await expect(api.getAdminUsers()).rejects.toMatchObject({
      response: { status: 403, code: 'FORBIDDEN' },
    })
  })

  it('prevents transfers to a frozen receiver wallet', async () => {
    const login = await api.login({
      email: 'naman@vaultpay.dev',
      password: 'Password123!',
    })
    api.setAccessToken(login.token)

    await expect(
      api.transfer({ receiverEmail: 'aditi@vaultpay.dev', amount: 100 }),
    ).rejects.toMatchObject({
      response: { status: 403, code: 'WALLET_FROZEN' },
    })
  })
})
