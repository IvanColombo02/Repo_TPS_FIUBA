import { useState, useEffect, useMemo, useRef } from "react"
import { useAppForm } from "@/config/use-app-form"
import { PromotionCreateSchema, PromotionCreateForm, PromotionUpdateForm, type Expression, type Action } from "@/models/Promotion"
import { FormValidateOrFn } from "@tanstack/react-form"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { ErrorMessage } from "@/components/ui/error-message"
import { ConditionBuilder } from "./ConditionBuilder"
import { ActionBuilder } from "./ActionBuilder"
import { Plus, Save } from "lucide-react"
import type { PromotionDTO } from "@/lib/api/promotions"
import { Upload, X, Image as ImageIcon } from "lucide-react"
import { validateAndConvertImage } from "@/lib/utils/image-utils"


interface PromotionFormProps {
  onSave: (data: PromotionCreateForm | PromotionUpdateForm) => Promise<void>
  isSubmitting?: boolean
  initialData?: PromotionDTO | null
}

export function PromotionFormComponent({ onSave, isSubmitting = false, initialData = null }: PromotionFormProps) {
  const defaultCondition = useMemo<Expression>(() => ({ type: "totalAmount", operator: ">", value: 0 }), [])
  const defaultAction = useMemo<Action>(() => ({ type: "fixedDiscount", targetType: "ORDER", amount: 0 }), [])

  const parseInitialExpression = (): { condition: Expression | null; action: Action | null } => {
    if (initialData?.expression) {
      try {
        const parsed = JSON.parse(initialData.expression)
        if (parsed.condition && parsed.action) {
          return { condition: parsed.condition, action: parsed.action }
        }
      } catch (e) {
        console.error("Error parsing expression:", e)
      }
    }
    return { condition: null, action: null }
  }

  const initialParsed = parseInitialExpression()

  const [condition, setCondition] = useState<Expression | null>(
    initialParsed.condition || (initialData ? null : defaultCondition)
  )
  const [action, setAction] = useState<Action | null>(
    initialParsed.action || (initialData ? null : defaultAction)
  )
  const [error, setError] = useState<string | null>(null)
  const [localIsSubmitting, setLocalIsSubmitting] = useState(false)
  const [imagePreview, setImagePreview] = useState<string | null>(
    initialData?.base64Image && initialData.base64Image.trim() !== "" 
      ? initialData.base64Image 
      : null
  )
  const [imageError, setImageError] = useState<string | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const effectiveIsSubmitting = localIsSubmitting || isSubmitting

  const handleImageChange = async (e: React.ChangeEvent<HTMLInputElement>,
                                   field: { handleChange: (value: string) => void }) => {
    const file = e.target.files?.[0]
    if (!file) {
      return
    }

    const result = await validateAndConvertImage(file)

    if (!result.valid) {
      setImageError(result.error || "Error al procesar la imagen")
      return
    }

    setImageError(null)
    const base64Value = result.base64 || ""
    setImagePreview(base64Value || null)
    field.handleChange(base64Value)
    form.setFieldValue("base64Image", base64Value)
  }

  const handleRemoveImage = (field: { handleChange: (value: string) => void }) => {
    setImagePreview(null)
    field.handleChange("")
    form.setFieldValue("base64Image", "")
  }

  useEffect(() => {
    if (initialData?.expression) {
      try {
        const parsed = JSON.parse(initialData.expression)
        if (parsed.condition && parsed.action) {
          setCondition(parsed.condition || null)
          setAction(parsed.action || null)
        }
      } catch (e) {
        console.error("Error parsing expression:", e)
      }
    } else if (!initialData) {
      setCondition(defaultCondition)
      setAction(defaultAction)
    }
    
    if (initialData) {
      const base64Image = initialData.base64Image
      form.setFieldValue("base64Image", base64Image || "")
      const previewValue = base64Image && base64Image.trim() !== "" ? base64Image : null
      setImagePreview(previewValue)
      setImageError(null)
    } else {
      setImagePreview(null)
      setImageError(null)
    }
  }, [initialData?.id, defaultCondition, defaultAction])

  const buildExpression = (): string => {
    if (!condition || !action) {
      throw new Error("Debe completar la condición y la acción")
    }
    return JSON.stringify({
      condition,
      action,
    })
  }

  const form = useAppForm({
    defaultValues: {
      name: initialData?.name || "",
      description: initialData?.description || "",
      fromDate: initialData?.fromDate || "",
      toDate: initialData?.toDate || "",
      base64Image: initialData?.base64Image || "",
      expression: "",
    } as PromotionCreateForm,
    onSubmit: async ({ value }) => {
      try {
        setLocalIsSubmitting(true)
        setError(null)
        const expression = buildExpression()
        const submitData = {
          ...value,
          expression,
        }
        await onSave(submitData)
        if (!initialData) {
          form.reset()
          setCondition(defaultCondition)
          setAction(defaultAction)
          setImagePreview(null)
        }
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "Error al guardar la promoción"
        setError(errorMessage)
      } finally {
        setLocalIsSubmitting(false)
      }
    },
    validators: {
      onSubmit: PromotionCreateSchema as unknown as FormValidateOrFn<PromotionCreateForm>,
    },
  })

  const isFormValid = () => {
    const values = form.state.values
    const hasBasicFields =
      values.name.trim().length >= 3 &&
      values.description.trim().length >= 10 &&
      values.fromDate &&
      values.toDate &&
      new Date(values.fromDate) <= new Date(values.toDate)
    const isValid = hasBasicFields && !!condition && !!action
    return isValid
  }

  useEffect(() => {
    if (condition && action) {
      try {
        const expression = JSON.stringify({
          condition,
          action,
        })
        form.setFieldValue("expression", expression)
      } catch (error) {
        console.error("Error building expression:", error)
      }
    } else {
      form.setFieldValue("expression", "")
    }
  }, [condition, action, form])

  return (
    <div className="space-y-4">
      {error && (
        <div className="p-3 text-sm text-red-600 bg-red-50 border border-red-200 rounded-md">
          {error}
        </div>
      )}
      <form
        onSubmit={(e) => {
          e.preventDefault()
          e.stopPropagation()
          form.handleSubmit?.(e)
        }}
        className="space-y-4"
      >
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <form.Field
              name="name"
              children={(field) => (
                  <div className="space-y-2">
                    <Label htmlFor="name">Nombre *</Label>
                    <Input
                        id="name"
                        placeholder="Ej: Descuento en compras grandes"
                        value={field.state.value}
                        onChange={(e) => field.handleChange(e.target.value)}
                        onBlur={field.handleBlur}
                    />
                    {field.state.meta.errors.length > 0 && (
                        <ErrorMessage error={field.state.meta.errors[0] || null}/>
                    )}
                  </div>
              )}
          />

          <form.Field
              name="base64Image"
              children={(field) => (
                  <div className="flex flex-col items-center space-y-2">
                    <div className="relative">
                      <div
                          className="w-28 h-28 rounded-full border-4 border-border overflow-hidden bg-muted flex items-center justify-center"
                      >
                        {imagePreview ? (
                            <img
                                src={imagePreview}
                                alt="Preview"
                                className="w-full h-full object-cover"
                            />
                        ) : (
                            <ImageIcon className="w-14 h-14 text-muted-foreground"/>
                        )}
                      </div>

                      {imagePreview && (
                          <button
                              type="button"
                              onClick={() => handleRemoveImage(field)}
                              className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full p-1.5 hover:bg-red-600 transition-colors"
                          >
                            <X className="w-4 h-4"/>
                          </button>
                      )}

                      <Label
                          htmlFor="base64Image"
                          className="absolute bottom-1 right-1 bg-primary text-primary-foreground rounded-full p-2 cursor-pointer hover:bg-primary/90 transition-colors shadow-lg"
                          onClick={(e) => {
                            e.preventDefault()
                            if (fileInputRef.current) {
                              fileInputRef.current.click()
                            }
                          }}
                      >
                        <Upload className="w-4 h-4"/>
                      </Label>

                      <Input
                          ref={fileInputRef}
                          id="base64Image"
                          type="file"
                          accept="image/*"
                          className="hidden"
                          onChange={(e) => handleImageChange(e, field)}
                      />
                    </div>

                    {imageError && (
                        <p className="text-sm text-red-400 font-medium">{imageError}</p>
                    )}

                    <p className="text-xs text-muted-foreground">
                      Haz clic en el ícono para subir una foto (opcional, máx. 2MB)
                    </p>

                    {field.state.meta.errors.length > 0 && (
                        <ErrorMessage error={field.state.meta.errors[0] || null}/>
                    )}
                  </div>
              )}
          />

        </div>

        <form.Field
            name="description"
            children={(field) => (
                <div className="space-y-2">
                  <Label htmlFor="description">Descripción *</Label>
                  <Input
                      id="description"
                      placeholder="Ej: $5000 de descuento en compras mayores a $30000"
                      value={field.state.value}
                      onChange={(e) => field.handleChange(e.target.value)}
                      onBlur={field.handleBlur}
                  />
                  {field.state.meta.errors.length > 0 && (
                      <ErrorMessage error={field.state.meta.errors[0] || null}/>
                  )}
                </div>
            )}
        />

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <form.Field
              name="fromDate"
              children={(field) => (
                  <div className="space-y-2">
                    <Label htmlFor="fromDate">Fecha de Inicio *</Label>
                    <Input
                        id="fromDate"
                        type="date"
                        value={field.state.value}
                        onChange={(e) => field.handleChange(e.target.value)}
                        onBlur={field.handleBlur}
                        className="[&::-webkit-calendar-picker-indicator]:invert [&::-webkit-calendar-picker-indicator]:cursor-pointer"
                    />
                    {field.state.meta.errors.length > 0 && (
                        <ErrorMessage error={field.state.meta.errors[0] || null}/>
                    )}
                  </div>
              )}
          />

          <form.Field
              name="toDate"
              children={(field) => (
                  <div className="space-y-2">
                    <Label htmlFor="toDate">Fecha de Fin *</Label>
                    <Input
                        id="toDate"
                        type="date"
                        value={field.state.value}
                        onChange={(e) => field.handleChange(e.target.value)}
                        onBlur={field.handleBlur}
                        className="[&::-webkit-calendar-picker-indicator]:invert [&::-webkit-calendar-picker-indicator]:cursor-pointer"
                    />
                    {field.state.meta.errors.length > 0 && (
                        <ErrorMessage error={field.state.meta.errors[0] || null}/>
                    )}
                  </div>
              )}
          />
        </div>

        <div className="space-y-3">
          <Label className="text-sm font-semibold">Condición de la Promoción *</Label>
          <ConditionBuilder value={condition} onChange={setCondition}/>
          {!condition && (
              <p className="text-sm text-muted-foreground">
                Configure la condición que debe cumplirse para aplicar la promoción
              </p>
          )}
        </div>

        <div className="space-y-3">
          <Label className="text-sm font-semibold">Acción de la Promoción *</Label>
          <ActionBuilder value={action} onChange={setAction} />
          {!action && (
            <p className="text-sm text-muted-foreground">
              Configure la acción que se aplicará cuando se cumpla la condición
            </p>
          )}
        </div>

        <Button
          type="submit"
          disabled={effectiveIsSubmitting || !isFormValid()}
          className="w-full"
        >
          {effectiveIsSubmitting ? (
            <>
              <div className="mr-2 h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
              {initialData ? "Guardando..." : "Creando..."}
            </>
          ) : (
            <>
              {initialData ? (
                <>
                  <Save className="mr-2 h-4 w-4" />
                  Guardar Cambios
                </>
              ) : (
                <>
                  <Plus className="mr-2 h-4 w-4" />
                  Crear Promoción
                </>
              )}
            </>
          )}
        </Button>
      </form>
    </div>
  )
}

