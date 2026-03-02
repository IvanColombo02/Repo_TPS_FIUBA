import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { ErrorMessage } from "@/components/ui/error-message"

interface ValidatedInputProps {
  id: string
  name?: string
  label: string
  type?: string
  placeholder?: string
  value: string
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void
  onBlur?: () => void
  error?: string
  autoComplete?: string
  className?: string
  max?: string
  min?: string
  style?: React.CSSProperties
}

/**
 * Displays a label, an input with a red border if there is an error, and an error message.
 */
export const ValidatedInput = ({
  id,
  name,
  label,
  type = "text",
  placeholder,
  value,
  onChange,
  onBlur,
  error,
  autoComplete,
  className = "",
  max,
  min,
  style
}: ValidatedInputProps) => {
  const hasError = !!error

  return (
    <div className="space-y-2">
      <Label htmlFor={id} className="text-foreground">{label}</Label>
      <Input
        id={id}
        name={name}
        type={type}
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        onBlur={onBlur}
        autoComplete={autoComplete}
        max={max}
        min={min}
        style={style}
        className={`text-foreground ${hasError ? 'border-red-500 focus-visible:ring-red-500' : ''} ${className}`}
      />
      <ErrorMessage error={error || null} />
    </div>
  )
}

