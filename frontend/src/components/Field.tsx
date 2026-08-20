import type { InputHTMLAttributes, ReactNode, SelectHTMLAttributes } from 'react'

interface FieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string
  error?: string
  hint?: string
  suffix?: ReactNode
}

export function Field({ label, error, hint, suffix, id, className = '', ...props }: FieldProps) {
  const fieldId = id ?? props.name
  const descriptionId = error ? `${fieldId}-error` : hint ? `${fieldId}-hint` : undefined
  return (
    <div className={`field ${className}`.trim()}>
      <label htmlFor={fieldId}>{label}</label>
      <div className="field__control">
        <input
          id={fieldId}
          aria-invalid={Boolean(error)}
          aria-describedby={descriptionId}
          {...props}
        />
        {suffix}
      </div>
      {error ? <p className="field__error" id={descriptionId} role="alert">{error}</p> : null}
      {!error && hint ? <p className="field__hint" id={descriptionId}>{hint}</p> : null}
    </div>
  )
}

interface SelectFieldProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label: string
  children: ReactNode
}

export function SelectField({ label, id, children, ...props }: SelectFieldProps) {
  const fieldId = id ?? props.name
  return (
    <div className="field">
      <label htmlFor={fieldId}>{label}</label>
      <div className="field__control">
        <select id={fieldId} {...props}>{children}</select>
      </div>
    </div>
  )
}
