import { LoaderCircle, type LucideIcon } from 'lucide-react'
import type { ButtonHTMLAttributes } from 'react'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  tone?: 'primary' | 'secondary' | 'ghost' | 'danger'
  icon?: LucideIcon
  busy?: boolean
}

export function Button({
  tone = 'primary',
  icon: Icon,
  busy = false,
  className = '',
  children,
  disabled,
  ...props
}: ButtonProps) {
  return (
    <button
      className={`button button--${tone} ${className}`.trim()}
      disabled={disabled || busy}
      aria-busy={busy}
      {...props}
    >
      {busy ? <LoaderCircle className="button__spinner" aria-hidden="true" /> : Icon ? <Icon aria-hidden="true" /> : null}
      <span>{children}</span>
    </button>
  )
}
