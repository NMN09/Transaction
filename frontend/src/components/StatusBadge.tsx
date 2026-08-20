import { CircleCheck, CircleOff } from 'lucide-react'
import type { WalletStatus } from '../types/wallet'

export function StatusBadge({ status }: { status: WalletStatus }) {
  const Icon = status === 'ACTIVE' ? CircleCheck : CircleOff
  return (
    <span className={`status status--${status.toLowerCase()}`}>
      <Icon aria-hidden="true" />
      {status === 'ACTIVE' ? 'Active' : 'Frozen'}
    </span>
  )
}
