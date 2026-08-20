import { CalendarDays, Mail, ShieldCheck } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { ApiError } from '../api/walletApi'
import { Button } from '../components/Button'
import { Field } from '../components/Field'
import { useWalletApp } from '../hooks/useWalletApp'

export function ProfilePage() {
  const { session, updateProfile, working } = useWalletApp()
  const user = session!.user
  const [name, setName] = useState(user.name)
  const [phone, setPhone] = useState(user.phone)
  const [errors, setErrors] = useState<Record<string, string>>({})

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    const nextErrors: Record<string, string> = {}
    if (name.trim().length < 2) nextErrors.name = 'Enter at least 2 characters.'
    if (!/^[6-9]\d{9}$/.test(phone)) nextErrors.phone = 'Enter a valid 10-digit Indian mobile number.'
    setErrors(nextErrors)
    if (Object.keys(nextErrors).length) return
    try {
      await updateProfile({ name, phone })
    } catch (error) {
      if (error instanceof ApiError) setErrors({ form: error.response.message })
    }
  }

  return (
    <div className="page profile-page">
      <header className="page-heading">
        <div><p className="eyebrow">Account settings</p><h1>Your profile</h1></div>
        <p>Keep your identity information current. Email and role are read-only in this MVP.</p>
      </header>
      <div className="profile-grid">
        <aside className="profile-card">
          <div className="profile-card__avatar">{user.name.split(' ').map((part) => part[0]).slice(0, 2).join('')}</div>
          <h2>{user.name}</h2>
          <p>{user.role === 'ADMIN' ? 'Administrator account' : 'Personal wallet account'}</p>
          <dl>
            <div><dt><Mail aria-hidden="true" /> Email</dt><dd>{user.email}</dd></div>
            <div><dt><ShieldCheck aria-hidden="true" /> Role</dt><dd>{user.role}</dd></div>
            <div><dt><CalendarDays aria-hidden="true" /> Member since</dt><dd>{new Intl.DateTimeFormat('en-IN', { dateStyle: 'long' }).format(new Date(user.createdAt))}</dd></div>
          </dl>
        </aside>
        <section className="panel profile-form" aria-labelledby="profile-form-title">
          <p className="eyebrow">Editable details</p>
          <h2 id="profile-form-title">Personal information</h2>
          <form onSubmit={submit} noValidate>
            {errors.form ? <div className="form-alert" role="alert">{errors.form}</div> : null}
            <Field label="Full name" name="name" autoComplete="name" value={name} onChange={(event) => setName(event.target.value)} error={errors.name} />
            <Field label="Phone number" name="phone" type="tel" inputMode="numeric" autoComplete="tel" maxLength={10} value={phone} onChange={(event) => setPhone(event.target.value.replace(/\D/g, ''))} error={errors.phone} hint="10 digits, starting with 6–9" />
            <Field label="Email address" name="email" type="email" value={user.email} readOnly hint="Email changes are not supported." />
            <Button type="submit" busy={working}>Save changes</Button>
          </form>
        </section>
      </div>
    </div>
  )
}
