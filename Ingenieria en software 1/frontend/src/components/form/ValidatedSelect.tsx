import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { ErrorMessage } from "@/components/ui/error-message"

interface SelectOption {
  value: string
  label: string
}

interface ValidatedSelectProps {
  id: string
  label: string
  value: string
  onChange: (value: string) => void
  options: SelectOption[]
  placeholder?: string
  error?: string
  className?: string
}

/**
 * Displays a label, a select with a red border if there is an error, and an error message.
 */
export const ValidatedSelect = ({
  id,
  label,
  value,
  onChange,
  options,
  placeholder = "Selecciona una opción",
  error,
  className = ""
}: ValidatedSelectProps) => {
  const hasError = !!error

  return (
    <div className="space-y-2">
      <Label htmlFor={id} className="text-foreground">{label}</Label>
      <Select value={value} onValueChange={onChange}>
        <SelectTrigger
          id={id}
          className={`w-full text-foreground ${hasError ? 'border-red-500 focus-visible:ring-red-500' : ''} ${className}`}
        >
          <SelectValue placeholder={placeholder} />
        </SelectTrigger>
        <SelectContent>
          {options.map((option) => (
            <SelectItem key={option.value} value={option.value}>
              {option.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
      <ErrorMessage error={error || null} />
    </div>
  )
}

