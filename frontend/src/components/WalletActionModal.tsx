import { useState, type FormEvent } from 'react'
import { ArrowDownToLine, ArrowUpRight, Plus } from 'lucide-react'
import { ApiError } from '../api/walletApi'
import { useWalletApp } from '../hooks/useWalletApp'
import { Button } from './Button'
import { Field } from './Field'
import { Modal } from './Modal'

export type WalletAction = 'add' | 'withdraw' | 'transfer'

interface WalletActionModalProps {
  action: WalletAction | null
  onClose(): void
}

const actionMeta = {
  add: {
    title: 'Add money',
    description: 'Add simulated funds to your VaultPay balance.',
    button: 'Add money',
    icon: Plus,
  },
  withdraw: {
    title: 'Withdraw money',
    description: 'Remove simulated funds from your wallet balance.',
    button: 'Withdraw money',
    icon: ArrowDownToLine,
  },
  transfer: {
    title: 'Send money',
    description: 'Transfer funds to another registered VaultPay user.',
    button: 'Send money',
    icon: ArrowUpRight,
  },
}

export function WalletActionModal({ action, onClose }: WalletActionModalProps) {
  const { addMoney, withdraw, transfer, wallet, session, working } = useWalletApp()
  const [amount, setAmount] = useState('')
  const [receiverEmail, setReceiverEmail] = useState('')
  const [remarks, setRemarks] = useState('')
  const [errors, setErrors] = useState<Record<string, string>>({})

  const resetForm = () => {
    setAmount('')
    setReceiverEmail('')
    setRemarks('')
    setErrors({})
  }

  const close = () => {
    resetForm()
    onClose()
  }

  if (!action) return null
  const meta = actionMeta[action]

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    const nextErrors: Record<string, string> = {}
    const numericAmount = Number(amount)
    if (!amount || numericAmount <= 0) nextErrors.amount = 'Enter an amount greater than 0.'
    else if (!/^\d+(\.\d{1,2})?$/.test(amount)) nextErrors.amount = 'Use no more than 2 decimal places.'
    else if (action !== 'add' && wallet && numericAmount > wallet.balance) nextErrors.amount = 'Amount exceeds your available balance.'
    if (action === 'transfer') {
      if (!receiverEmail.trim()) nextErrors.receiverEmail = 'Enter the recipient email.'
      else if (!/^\S+@\S+\.\S+$/.test(receiverEmail)) nextErrors.receiverEmail = 'Enter a valid email address.'
      else if (receiverEmail.trim().toLowerCase() === session?.user.email.toLowerCase()) {
        nextErrors.receiverEmail = 'You cannot send money to yourself.'
      }
    }
    setErrors(nextErrors)
    if (Object.keys(nextErrors).length) return

    try {
      if (action === 'add') await addMoney(numericAmount)
      if (action === 'withdraw') await withdraw(numericAmount)
      if (action === 'transfer') {
        await transfer({ receiverEmail, amount: numericAmount, remarks: remarks || undefined })
      }
      close()
    } catch (error) {
      if (error instanceof ApiError) {
        setErrors({ form: error.response.message })
      }
    }
  }

  return (
    <Modal open title={meta.title} description={meta.description} onClose={close}>
      <form className="modal__body action-form" onSubmit={submit} noValidate>
        {errors.form ? <div className="form-alert" role="alert">{errors.form}</div> : null}
        {action === 'transfer' ? (
          <Field
            label="Recipient email"
            name="receiverEmail"
            type="email"
            inputMode="email"
            autoComplete="email"
            value={receiverEmail}
            onChange={(event) => setReceiverEmail(event.target.value)}
            error={errors.receiverEmail}
            hint="Enter the email address of another registered wallet user."
          />
        ) : null}
        <Field
          label="Amount"
          name="amount"
          type="text"
          inputMode="decimal"
          value={amount}
          onChange={(event) => setAmount(event.target.value.replace(/[^\d.]/g, ''))}
          error={errors.amount}
          hint={wallet ? `Available balance: ₹${wallet.balance.toLocaleString('en-IN', { minimumFractionDigits: 2 })}` : undefined}
          suffix={<span className="field__suffix">INR</span>}
        />
        {action === 'add' ? (
          <div className="quick-amounts" aria-label="Quick amount choices">
            {[500, 1000, 5000, 10000].map((value) => (
              <button type="button" key={value} onClick={() => setAmount(String(value))}>+₹{value.toLocaleString('en-IN')}</button>
            ))}
          </div>
        ) : null}
        {action === 'transfer' ? (
          <Field
            label="Remarks (optional)"
            name="remarks"
            maxLength={255}
            value={remarks}
            onChange={(event) => setRemarks(event.target.value)}
            hint={`${remarks.length}/255 characters`}
          />
        ) : null}
        <div className="modal__actions">
          <Button type="button" tone="ghost" onClick={close}>Cancel</Button>
          <Button type="submit" icon={meta.icon} busy={working}>{meta.button}</Button>
        </div>
      </form>
    </Modal>
  )
}
