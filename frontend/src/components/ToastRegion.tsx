import { CheckCircle2, CircleAlert, Info, X } from 'lucide-react'
import { useWalletApp } from '../hooks/useWalletApp'

export function ToastRegion() {
  const { toasts, dismissToast } = useWalletApp()
  return (
    <div className="toast-region" aria-live="polite" aria-label="Notifications">
      {toasts.map((toast) => {
        const Icon = toast.tone === 'success' ? CheckCircle2 : toast.tone === 'error' ? CircleAlert : Info
        return (
          <div className={`toast toast--${toast.tone}`} key={toast.id}>
            <Icon aria-hidden="true" />
            <div>
              <strong>{toast.title}</strong>
              {toast.message ? <p>{toast.message}</p> : null}
            </div>
            <button
              className="icon-button"
              onClick={() => dismissToast(toast.id)}
              aria-label="Dismiss notification"
            >
              <X aria-hidden="true" />
            </button>
          </div>
        )
      })}
    </div>
  )
}
