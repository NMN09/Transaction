import { useContext } from 'react'
import { WalletAppContext } from '../context/walletAppContextValue'

export function useWalletApp() {
  const context = useContext(WalletAppContext)
  if (!context) throw new Error('useWalletApp must be used inside WalletAppProvider')
  return context
}
