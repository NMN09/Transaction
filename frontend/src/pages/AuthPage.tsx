import { Eye, EyeOff, LockKeyhole, ShieldCheck, WalletCards } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { Navigate } from 'react-router-dom'
import { ApiError } from '../api/walletApi'
import { Button } from '../components/Button'
import { Field } from '../components/Field'
import { useWalletApp } from '../hooks/useWalletApp'

type AuthMode = 'login' | 'register'

export function AuthPage() {
  const { session, login, register, working } = useWalletApp()
  const [mode, setMode] = useState<AuthMode>('login')
  const [showPassword, setShowPassword] = useState(false)
  const [values, setValues] = useState({
    name: '',
    email: '',
    phone: '',
    password: '',
  })
  const [errors, setErrors] = useState<Record<string, string>>({})

  if (session) return <Navigate to="/app" replace />

  const update = (field: keyof typeof values, value: string) => {
    setValues((current) => ({ ...current, [field]: value }))
  }

  const switchMode = (nextMode: AuthMode) => {
    setMode(nextMode)
    setErrors({})
  }

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    const nextErrors: Record<string, string> = {}
    if (mode === 'register' && values.name.trim().length < 2) nextErrors.name = 'Enter at least 2 characters.'
    if (!/^\S+@\S+\.\S+$/.test(values.email)) nextErrors.email = 'Enter a valid email address.'
    if (mode === 'register' && !/^[6-9]\d{9}$/.test(values.phone)) nextErrors.phone = 'Enter a valid 10-digit Indian mobile number.'
    if (!values.password) nextErrors.password = 'Enter your password.'
    setErrors(nextErrors)
    if (Object.keys(nextErrors).length) return

    try {
      if (mode === 'login') await login({ email: values.email, password: values.password })
      else {
        await register(values)
        setMode('login')
        setValues((current) => ({ ...current, name: '', phone: '' }))
      }
    } catch (error) {
      setErrors({ form: error instanceof ApiError ? error.response.message : 'Try again.' })
    }
  }

  return (
    <main className="auth-page" id="main-content">
      <section className="auth-story" aria-labelledby="auth-story-title">
        <div className="brand brand--light">
          <span className="brand__mark" aria-hidden="true"><WalletCards /></span>
          <span><strong>VaultPay</strong><small>Living ledger</small></span>
        </div>
        <div className="auth-story__copy">
          <p className="eyebrow">Your balance has a story</p>
          <h1 id="auth-story-title">See every rupee <em>find its place.</em></h1>
          <p>A clear, secure interface for simulated wallet activity—built around the ledger, not hidden behind it.</p>
        </div>
        <div className="ledger-preview" aria-hidden="true">
          <span className="ledger-preview__rail" />
          <div><small>Balance after transfer</small><strong>₹24,750.00</strong></div>
          <div><small>TX-81C9A2B4E731</small><span>− ₹1,250.00</span></div>
          <div><small>TX-237B47D9C002</small><span>+ ₹10,000.00</span></div>
        </div>
        <div className="auth-trust"><ShieldCheck aria-hidden="true" /><span>JWT-ready flows · Role-aware navigation · Accessible forms</span></div>
      </section>
      <section className="auth-panel" aria-labelledby="auth-title">
        <div className="auth-panel__inner">
          <p className="eyebrow">Secure wallet access</p>
          <h2 id="auth-title">{mode === 'login' ? 'Sign in to your wallet' : 'Create your wallet'}</h2>
          <p className="auth-panel__intro">{mode === 'login' ? 'Enter the credentials for your wallet account.' : 'Registration creates a wallet with a ₹0.00 balance.'}</p>
          <div className="segmented" role="tablist" aria-label="Authentication mode">
            <button role="tab" aria-selected={mode === 'login'} onClick={() => switchMode('login')}>Sign in</button>
            <button role="tab" aria-selected={mode === 'register'} onClick={() => switchMode('register')}>Create account</button>
          </div>
          <form onSubmit={submit} noValidate>
            {errors.form ? <div className="form-alert" role="alert"><LockKeyhole aria-hidden="true" />{errors.form}</div> : null}
            {mode === 'register' ? (
              <Field label="Full name" name="name" autoComplete="name" value={values.name} onChange={(event) => update('name', event.target.value)} error={errors.name} />
            ) : null}
            <Field label="Email address" name="email" type="email" autoComplete="email" value={values.email} onChange={(event) => update('email', event.target.value)} error={errors.email} />
            {mode === 'register' ? (
              <Field label="Phone number" name="phone" type="tel" inputMode="numeric" autoComplete="tel" maxLength={10} value={values.phone} onChange={(event) => update('phone', event.target.value.replace(/\D/g, ''))} error={errors.phone} hint="10 digits, starting with 6–9" />
            ) : null}
            <Field
              label="Password"
              name="password"
              type={showPassword ? 'text' : 'password'}
              autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
              value={values.password}
              onChange={(event) => update('password', event.target.value)}
              error={errors.password}
              suffix={
                <button type="button" className="field__icon" onClick={() => setShowPassword((visible) => !visible)} aria-label={showPassword ? 'Hide password' : 'Show password'}>
                  {showPassword ? <EyeOff aria-hidden="true" /> : <Eye aria-hidden="true" />}
                </button>
              }
            />
            <Button className="button--full" type="submit" busy={working}>{mode === 'login' ? 'Sign in' : 'Create account'}</Button>
          </form>
          <p className="mock-note">Connected to the Spring Boot wallet API.</p>
        </div>
      </section>
    </main>
  )
}
