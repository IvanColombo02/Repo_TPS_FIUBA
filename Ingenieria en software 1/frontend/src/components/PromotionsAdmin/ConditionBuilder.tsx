import { useState, useMemo, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import type { Expression } from "@/models/Promotion"
import { SingleItemSelector } from "./SingleItemSelector"
import { useProducts } from "@/hooks/use-products"
import { useCombos } from "@/hooks/use-combos"
import { createFindItemById, getAvailableCategories, getAvailableTypes } from "./promotion-helpers"

interface ConditionBuilderProps {
  value: Expression | null
  onChange: (condition: Expression | null) => void
  depth?: number
}

const DAYS_OF_WEEK = [
  { value: "MONDAY", label: "Lunes" },
  { value: "TUESDAY", label: "Martes" },
  { value: "WEDNESDAY", label: "Miércoles" },
  { value: "THURSDAY", label: "Jueves" },
  { value: "FRIDAY", label: "Viernes" },
  { value: "SATURDAY", label: "Sábado" },
  { value: "SUNDAY", label: "Domingo" },
]

const OPERATORS = [
  { value: ">", label: "Mayor que (>)" },
  { value: ">=", label: "Mayor o igual que (>=)" },
  { value: "<", label: "Menor que (<)" },
  { value: "<=", label: "Menor o igual que (<=)" },
  { value: "==", label: "Igual a (==)" },
]

function SimpleConditionContent({ value, onChange }: { value: Expression | null; onChange: (expr: Expression) => void }) {
  const [simpleType, setSimpleType] = useState<string>(value?.type || "totalAmount")

  useEffect(() => {
    if (value?.type) {
      setSimpleType(value.type)
    }
  }, [value?.type])
  const [productDialogOpen, setProductDialogOpen] = useState(false)
  const { products = [] } = useProducts({ mode: "all" })
  const { combos = [] } = useCombos({ mode: "all" })

  const findItemById = useMemo(() => createFindItemById(products || [], combos || []), [products, combos])
  const availableCategories = useMemo(() => getAvailableCategories(products || [], combos || []), [products, combos])
  const availableTypes = useMemo(() => getAvailableTypes(products || [], combos || []), [products, combos])

  const handleSimpleConditionChange = (type: string, props: Record<string, unknown>) => {
    onChange({
      type,
      ...props,
    } as Expression)
  }

  return (
    <CardContent className="space-y-3">
      <div className="space-y-2">
        <Label>Tipo de Condición</Label>
        <Select
          value={simpleType}
          onValueChange={(val) => {
            setSimpleType(val)
            onChange({ type: val } as Expression)
          }}
        >
          <SelectTrigger>
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="totalAmount">Monto Total</SelectItem>
            <SelectItem value="dayOfWeek">Día de la Semana</SelectItem>
            <SelectItem value="time">Hora del Día</SelectItem>
            <SelectItem value="mailContains">Email Contiene</SelectItem>
            <SelectItem value="productInCart">Producto en Carrito</SelectItem>
            <SelectItem value="productType">Tipo/Categoría de Producto</SelectItem>
            <SelectItem value="quantity">Cantidad de Producto</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {simpleType === "totalAmount" && (
        <div className="grid grid-cols-2 gap-3">
          <div className="space-y-2">
            <Label>Operador</Label>
            <Select
              value={(value as Expression & { operator?: string })?.operator || ">"}
              onValueChange={(val) => {
                handleSimpleConditionChange("totalAmount", {
                  operator: val,
                  value: (value as Expression & { value?: number })?.value || 0,
                })
              }}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {OPERATORS.map((op) => (
                  <SelectItem key={op.value} value={op.value}>
                    {op.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-2">
            <Label>Valor</Label>
            <Input
              type="number"
              value={(value as Expression & { value?: number })?.value || ""}
              onChange={(e) => {
                handleSimpleConditionChange("totalAmount", {
                  operator: (value as Expression & { operator?: string })?.operator || ">",
                  value: parseFloat(e.target.value) || 0,
                })
              }}
              placeholder="30000"
            />
          </div>
        </div>
      )}

      {simpleType === "dayOfWeek" && (
        <div className="space-y-2">
          <Label>Día de la Semana</Label>
          <Select
            value={(value as Expression & { day?: string })?.day || ""}
            onValueChange={(val) => {
              handleSimpleConditionChange("dayOfWeek", { day: val })
            }}
          >
            <SelectTrigger>
              <SelectValue placeholder="Seleccionar día" />
            </SelectTrigger>
            <SelectContent>
              {DAYS_OF_WEEK.map((day) => (
                <SelectItem key={day.value} value={day.value}>
                  {day.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      )}

      {simpleType === "time" && (
        <div className="grid grid-cols-2 gap-3">
          <div className="space-y-2">
            <Label>Operador</Label>
            <Select
              value={(value as Expression & { operator?: string })?.operator || ">"}
              onValueChange={(val) => {
                handleSimpleConditionChange("time", {
                  operator: val,
                  hour: (value as Expression & { hour?: string })?.hour || "12:00:00",
                })
              }}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value=">">Después de (&gt;)</SelectItem>
                <SelectItem value="<">Antes de (&lt;)</SelectItem>
                <SelectItem value="=">Exactamente a (=)</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-2">
            <Label>Hora</Label>
            <Input
              type="time"
              value={(() => {
                const hour = (value as Expression & { hour?: string })?.hour || "12:00:00"
                return hour.substring(0, 5)
              })()}
              onChange={(e) => {
                const timeValue = e.target.value ? `${e.target.value}:00` : "12:00:00"
                handleSimpleConditionChange("time", {
                  operator: (value as Expression & { operator?: string })?.operator || ">",
                  hour: timeValue,
                })
              }}
              className="[&::-webkit-calendar-picker-indicator]:invert [&::-webkit-calendar-picker-indicator]:cursor-pointer"
            />
          </div>
        </div>
      )}

      {simpleType === "mailContains" && (
        <div className="space-y-2">
          <Label>Patrón de Email</Label>
          <Input
            type="text"
            value={(value as Expression & { mail?: string })?.mail || ""}
            onChange={(e) => {
              handleSimpleConditionChange("mailContains", { mail: e.target.value })
            }}
            placeholder="Ej: @fi.uba.ar o .personal@fi.uba.ar"
          />
          <p className="text-xs text-muted-foreground">
            El email del usuario debe contener este texto para que aplique la promoción.
            Ejemplo: ".personal@fi.uba.ar" para personal de FIUBA.
          </p>
        </div>
      )}

      {simpleType === "productInCart" && (
        <div className="space-y-2">
          <Label>Producto o Combo</Label>
          <Button
            type="button"
            variant="outline"
            className="w-full justify-start"
            onClick={() => setProductDialogOpen(true)}
          >
            {(() => {
              const productId = (value as Expression & { productId?: number })?.productId
              if (!productId) return "Seleccionar producto o combo..."
              const item = findItemById(productId)
              return item ? `${item.name}${item.isCombo ? " (Combo)" : ""}` : "Seleccionar producto o combo..."
            })()}
          </Button>
          <SingleItemSelector
            isOpen={productDialogOpen}
            onClose={() => setProductDialogOpen(false)}
            onSelect={(item) => {
              if (item) {
                handleSimpleConditionChange("productInCart", {
                  productId: item.id,
                })
              }
            }}
            initialItemId={(value as Expression & { productId?: number })?.productId || null}
            allowProducts={true}
            allowCombos={true}
            title="Seleccionar Producto o Combo"
            description="Selecciona el producto o combo que debe estar en el carrito"
          />
        </div>
      )}

      {simpleType === "productType" && (
        <div className="space-y-3">
          <div className="space-y-2">
            <Label>Tipo de Filtro</Label>
            <Select
              value={
                (value as Expression & { filterType?: "category" | "type" })?.filterType ||
                ((value as Expression & { category?: string })?.category ? "category" : undefined) ||
                ((value as Expression & { productType?: string })?.productType ? "type" : undefined) ||
                "category"
              }
              onValueChange={(val) => {
                const currentValue = value as Expression & {
                  category?: string
                  productType?: string
                }
                handleSimpleConditionChange("productType", {
                  filterType: val as "category" | "type",
                  category: val === "category" ? currentValue?.category || "" : undefined,
                  productType: val === "type" ? currentValue?.productType || "" : undefined,
                })
              }}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="category">Categoría</SelectItem>
                <SelectItem value="type">Tipo</SelectItem>
              </SelectContent>
            </Select>
          </div>
          {(() => {
            const filterType = (value as Expression & { filterType?: "category" | "type" })?.filterType
            const hasCategory = !!(value as Expression & { category?: string })?.category
            const hasProductType = !!(value as Expression & { productType?: string })?.productType

            const shouldShowType = filterType === "type" || (!filterType && hasProductType && !hasCategory)

            if (shouldShowType) {
              return (
                <div className="space-y-2">
                  <Label>Tipo</Label>
                  <Select
                    value={(value as Expression & { productType?: string })?.productType || ""}
                    onValueChange={(val) => {
                      handleSimpleConditionChange("productType", {
                        filterType: "type",
                        productType: val,
                        category: undefined,
                      })
                    }}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Seleccionar tipo" />
                    </SelectTrigger>
                    <SelectContent>
                      {availableTypes && availableTypes.length > 0 ? (
                        availableTypes.map((type) => (
                          <SelectItem key={`type-${type}`} value={type}>
                            {type}
                          </SelectItem>
                        ))
                      ) : (
                        <div className="px-2 py-1.5 text-sm text-muted-foreground">
                          No hay tipos disponibles
                        </div>
                      )}
                    </SelectContent>
                  </Select>
                </div>
              )
            }

            return (
              <div className="space-y-2">
                <Label>Categoría</Label>
                <Select
                  value={(value as Expression & { category?: string })?.category || ""}
                  onValueChange={(val) => {
                    handleSimpleConditionChange("productType", {
                      filterType: "category",
                      category: val,
                      productType: undefined,
                    })
                  }}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Seleccionar categoría" />
                  </SelectTrigger>
                  <SelectContent>
                    {availableCategories && availableCategories.length > 0 ? (
                      availableCategories.map((cat) => (
                        <SelectItem key={`cat-${cat}`} value={cat}>
                          {cat}
                        </SelectItem>
                      ))
                    ) : (
                      <div className="px-2 py-1.5 text-sm text-muted-foreground">
                        No hay categorías disponibles
                      </div>
                    )}
                  </SelectContent>
                </Select>
              </div>
            )
          })()}
        </div>
      )
      }

      {
        simpleType === "quantity" && (
          <>
            <div className="space-y-2">
              <Label>Producto o Combo</Label>
              <Button
                type="button"
                variant="outline"
                className="w-full justify-start"
                onClick={() => setProductDialogOpen(true)}
              >
                {(() => {
                  const productId = (value as Expression & { productId?: number })?.productId
                  if (!productId) return "Seleccionar producto o combo..."
                  const item = findItemById(productId)
                  return item ? `${item.name}${item.isCombo ? " (Combo)" : ""}` : "Seleccionar producto o combo..."
                })()}
              </Button>
              <SingleItemSelector
                isOpen={productDialogOpen}
                onClose={() => setProductDialogOpen(false)}
                onSelect={(item) => {
                  if (item) {
                    handleSimpleConditionChange("quantity", {
                      productId: item.id,
                      minQuantity: (value as Expression & { minQuantity?: number })?.minQuantity || 0,
                    })
                  }
                }}
                initialItemId={(value as Expression & { productId?: number })?.productId || null}
                allowProducts={true}
                allowCombos={true}
                title="Seleccionar Producto o Combo"
                description="Selecciona el producto o combo para la condición de cantidad"
              />
            </div>
            <div className="space-y-2">
              <Label>Cantidad Mínima</Label>
              <Input
                type="number"
                value={(value as Expression & { minQuantity?: number })?.minQuantity || ""}
                onChange={(e) => {
                  handleSimpleConditionChange("quantity", {
                    productId: (value as Expression & { productId?: number })?.productId || 0,
                    minQuantity: parseInt(e.target.value) || 0,
                  })
                }}
                placeholder="3"
              />
            </div>
          </>
        )
      }
    </CardContent >
  )
}

export function ConditionBuilder({ value, onChange, depth = 0 }: ConditionBuilderProps) {
  const MAX_DEPTH = 2
  const isLogical = value?.type === "and" || value?.type === "or"

  if (isLogical && depth < MAX_DEPTH) {
    const logicalValue = value as { type: "and" | "or"; left?: Expression; right?: Expression }
    const leftValue = logicalValue.left || ({ type: "totalAmount", operator: ">", value: 0 } as Expression)
    const rightValue = logicalValue.right || ({ type: "totalAmount", operator: ">", value: 0 } as Expression)

    return (
      <Card className="border-2 border-primary/20">
        <CardHeader>
          <div className="flex items-center justify-between gap-2">
            <CardTitle className="text-sm">Condición Multiple</CardTitle>
            <div className="flex gap-2">
              <Select
                value={logicalValue.type}
                onValueChange={(val) => {
                  onChange({
                    type: val as "and" | "or",
                    left: leftValue,
                    right: rightValue,
                  } as Expression)
                }}
              >
                <SelectTrigger className="w-32">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="and">Y (AND)</SelectItem>
                  <SelectItem value="or">O (OR)</SelectItem>
                </SelectContent>
              </Select>
              {depth === 0 && (
                <Button
                  type="button"
                  variant="ghost"
                  size="sm"
                  onClick={() => {
                    onChange({
                      type: "totalAmount",
                      operator: ">",
                      value: 0,
                    } as Expression)
                  }}
                >
                  Cambiar a Simple
                </Button>
              )}
              {depth > 0 && (
                <Button type="button" variant="ghost" size="sm" onClick={() => onChange(null)}>
                  Eliminar
                </Button>
              )}
            </div>
          </div>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="space-y-2">
            <Label className="text-sm font-medium">Condición Izquierda</Label>
            <div className="pl-2 border-l-2 border-primary/30">
              <ConditionBuilder
                value={leftValue}
                onChange={(left) => {
                  onChange({
                    type: logicalValue.type,
                    left: left || leftValue,
                    right: rightValue,
                  } as Expression)
                }}
                depth={depth + 1}
              />
            </div>
          </div>
          <div className="space-y-2">
            <Label className="text-sm font-medium">Condición Derecha</Label>
            <div className="pl-2 border-l-2 border-primary/30">
              <ConditionBuilder
                value={rightValue}
                onChange={(right) => {
                  onChange({
                    type: logicalValue.type,
                    left: leftValue,
                    right: right || rightValue,
                  } as Expression)
                }}
                depth={depth + 1}
              />
            </div>
          </div>
        </CardContent>
      </Card>
    )
  }

  const defaultValue = value || ({ type: "totalAmount", operator: ">", value: 0 } as Expression)

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle className="text-sm">Condición Simple</CardTitle>
          {depth === 0 && (
            <Button
              type="button"
              variant="ghost"
              size="sm"
              onClick={() => {
                onChange({
                  type: "and",
                  left: { type: "totalAmount", operator: ">", value: 0 } as Expression,
                  right: { type: "totalAmount", operator: ">", value: 0 } as Expression,
                } as Expression)
              }}
            >
              Cambiar a Multiple
            </Button>
          )}
        </div>
      </CardHeader>
      <SimpleConditionContent value={defaultValue} onChange={onChange} />
    </Card>
  )
}


