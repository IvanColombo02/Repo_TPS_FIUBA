import { useState, useMemo } from "react"
import { Label } from "@/components/ui/label"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { SingleItemSelector } from "./SingleItemSelector"
import { useProducts } from "@/hooks/use-products"
import { useCombos } from "@/hooks/use-combos"
import { createFindItemById, getAvailableCategories, getAvailableTypes } from "./promotion-helpers"
import type { Action } from "@/models/Promotion"

interface ActionBuilderProps {
  value: Action | null
  onChange: (action: Action | null) => void
}

export function ActionBuilder({ value, onChange }: ActionBuilderProps) {
  const [actionType, setActionType] = useState<string>(value?.type || "fixedDiscount")
  const [productDialogOpen, setProductDialogOpen] = useState(false)
  const [targetItemDialogOpen, setTargetItemDialogOpen] = useState(false)
  const { products } = useProducts({ mode: "all" })
  const { combos } = useCombos({ mode: "all" })

  const findItemById = useMemo(() => createFindItemById(products, combos), [products, combos])
  const availableCategories = useMemo(() => getAvailableCategories(products, combos), [products, combos])
  const availableTypes = useMemo(() => getAvailableTypes(products, combos), [products, combos])

  const getProductId = (action: Action | null): number | undefined => {
    return action?.type === "freeProduct" ? action.productId : undefined
  }

  const getTargetItemId = (action: Action | null): number | undefined => {
    if (!action) return undefined
    return action.type === "fixedDiscount" || action.type === "percentageDiscount" ? action.targetItemId : undefined
  }

  const getAmount = (action: Action | null): number | undefined => {
    return action?.type === "fixedDiscount" ? action.amount : undefined
  }

  const getPercentage = (action: Action | null): number | undefined => {
    return action?.type === "percentageDiscount" ? action.percentage : undefined
  }

  const getQuantity = (action: Action | null): number | undefined => {
    return action?.type === "freeProduct" ? action.quantity : undefined
  }

  const getBuyQuantity = (action: Action | null): number | undefined => {
    return action?.type === "quantityDiscount" ? action.buyQuantity : undefined
  }

  const getPayQuantity = (action: Action | null): number | undefined => {
    return action?.type === "quantityDiscount" ? action.payQuantity : undefined
  }

  const getTargetFilterType = (action: Action | null): "product" | "category" | "type" | undefined => {
    if (!action) return undefined
    return action.type === "fixedDiscount" || action.type === "percentageDiscount" ? action.targetFilterType : undefined
  }

  const getTargetCategory = (action: Action | null): string | undefined => {
    if (!action) return undefined
    return action.type === "fixedDiscount" || action.type === "percentageDiscount" ? action.targetCategory : undefined
  }

  const getTargetProductType = (action: Action | null): string | undefined => {
    if (!action) return undefined
    return action.type === "fixedDiscount" || action.type === "percentageDiscount" ? action.targetProductType : undefined
  }

  // Item selected for freeProduct
  const selectedItem = useMemo(() => findItemById(getProductId(value)), [findItemById, value])

  // Item selected for specific item discounts
  const selectedTargetItem = useMemo(() => findItemById(getTargetItemId(value)), [findItemById, value])

  const handleActionChange = (type: string, props: Record<string, unknown>) => {
    onChange({
      type,
      ...props,
    } as Action)
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-sm">Acción de Promoción</CardTitle>
      </CardHeader>
      <CardContent className="space-y-3">
        <div className="space-y-2">
          <Label>Tipo de Acción</Label>
          <Select
            value={actionType}
            onValueChange={(val) => {
              setActionType(val)
              if (val === "fixedDiscount") {
                onChange({ type: "fixedDiscount", targetType: "ORDER", amount: 0 })
              } else if (val === "percentageDiscount") {
                onChange({ type: "percentageDiscount", targetType: "ORDER", percentage: 0 })
              } else if (val === "freeProduct") {
                onChange({ type: "freeProduct", targetType: "ORDER", productId: 0, quantity: 1 })
              } else if (val === "quantityDiscount") {
                onChange({ type: "quantityDiscount", targetType: "ORDER_ITEM", buyQuantity: 0, payQuantity: 0 })
              }
            }}
          >
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="fixedDiscount">Descuento Fijo</SelectItem>
              <SelectItem value="percentageDiscount">Descuento Porcentual</SelectItem>
              <SelectItem value="freeProduct">Producto Gratis</SelectItem>
              <SelectItem value="quantityDiscount">Descuento por Cantidad</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {actionType === "fixedDiscount" && (
          <>
            <div className="space-y-2">
              <Label>Target</Label>
              <Select
                value={value?.targetType || "ORDER"}
                onValueChange={(val) => {
                  handleActionChange("fixedDiscount", {
                    targetType: val,
                    amount: getAmount(value) || 0,
                    targetItemId: val === "ORDER_ITEM" ? getTargetItemId(value) : undefined,
                  })
                }}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ORDER">Orden Completa</SelectItem>
                  <SelectItem value="ORDER_ITEM">Items Específicos</SelectItem>
                </SelectContent>
              </Select>
            </div>
            {value?.targetType === "ORDER_ITEM" && (
              <div className="space-y-3">
                <div className="space-y-2">
                  <Label>Filtrar Items Por</Label>
                  <Select
                    value={getTargetFilterType(value) || "product"}
                    onValueChange={(val) => {
                      handleActionChange("fixedDiscount", {
                        targetType: "ORDER_ITEM",
                        targetFilterType: val as "product" | "category" | "type",
                        targetItemId: val === "product" ? getTargetItemId(value) : undefined,
                        targetCategory: val === "category" ? getTargetCategory(value) : undefined,
                        targetProductType: val === "type" ? getTargetProductType(value) : undefined,
                        amount: getAmount(value) || 0,
                      })
                    }}
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="product">Producto/Combo Específico</SelectItem>
                      <SelectItem value="category">Categoría</SelectItem>
                      <SelectItem value="type">Tipo</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                {getTargetFilterType(value) === "product" || !getTargetFilterType(value) ? (
                  <div className="space-y-2">
                    <Label>Producto o Combo</Label>
                    <Button
                      type="button"
                      variant="outline"
                      className="w-full justify-start"
                      onClick={() => setTargetItemDialogOpen(true)}
                    >
                      {selectedTargetItem
                        ? `${selectedTargetItem.name}${selectedTargetItem.isCombo ? " (Combo)" : ""}`
                        : "Seleccionar producto o combo..."}
                    </Button>
                    <SingleItemSelector
                      isOpen={targetItemDialogOpen}
                      onClose={() => setTargetItemDialogOpen(false)}
                      onSelect={(item) => {
                        if (item) {
                          handleActionChange("fixedDiscount", {
                            targetType: "ORDER_ITEM",
                            targetFilterType: "product",
                            targetItemId: item.id,
                            amount: getAmount(value) || 0,
                          })
                        }
                      }}
                      initialItemId={getTargetItemId(value) || null}
                      allowProducts={true}
                      allowCombos={true}
                      title="Seleccionar Producto o Combo"
                      description="Selecciona el producto o combo al que se aplicará el descuento"
                    />
                  </div>
                ) : getTargetFilterType(value) === "category" ? (
                  <div className="space-y-2">
                    <Label>Categoría</Label>
                    <Select
                      value={getTargetCategory(value) || ""}
                      onValueChange={(val) => {
                        handleActionChange("fixedDiscount", {
                          targetType: "ORDER_ITEM",
                          targetFilterType: "category",
                          targetCategory: val,
                          amount: getAmount(value) || 0,
                        })
                      }}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Seleccionar categoría" />
                      </SelectTrigger>
                      <SelectContent>
                        {availableCategories.length > 0 ? (
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
                ) : (
                  <div className="space-y-2">
                    <Label>Tipo</Label>
                    <Select
                      value={getTargetProductType(value) || ""}
                      onValueChange={(val) => {
                        handleActionChange("fixedDiscount", {
                          targetType: "ORDER_ITEM",
                          targetFilterType: "type",
                          targetProductType: val,
                          amount: getAmount(value) || 0,
                        })
                      }}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Seleccionar tipo" />
                      </SelectTrigger>
                      <SelectContent>
                        {availableTypes.length > 0 ? (
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
                )}
              </div>
            )}
            <div className="space-y-2">
              <Label>Monto del Descuento</Label>
              <Input
                type="number"
                step="0.01"
                value={getAmount(value) || ""}
                onChange={(e) => {
                  handleActionChange("fixedDiscount", {
                    targetType: value?.targetType || "ORDER",
                    targetItemId: getTargetItemId(value),
                    amount: parseFloat(e.target.value) || 0,
                  })
                }}
                placeholder="5000"
              />
            </div>
          </>
        )}

        {actionType === "percentageDiscount" && (
          <>
            <div className="space-y-2">
              <Label>Target</Label>
              <Select
                value={value?.targetType || "ORDER"}
                onValueChange={(val) => {
                  handleActionChange("percentageDiscount", {
                    targetType: val,
                    percentage: getPercentage(value) || 0,
                    targetItemId: val === "ORDER_ITEM" ? getTargetItemId(value) : undefined,
                  })
                }}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ORDER">Orden Completa</SelectItem>
                  <SelectItem value="ORDER_ITEM">Items Específicos</SelectItem>
                </SelectContent>
              </Select>
            </div>
            {value?.targetType === "ORDER_ITEM" && (
              <div className="space-y-3">
                <div className="space-y-2">
                  <Label>Filtrar Items Por</Label>
                  <Select
                    value={getTargetFilterType(value) || "product"}
                    onValueChange={(val) => {
                      handleActionChange("percentageDiscount", {
                        targetType: "ORDER_ITEM",
                        targetFilterType: val as "product" | "category" | "type",
                        targetItemId: val === "product" ? getTargetItemId(value) : undefined,
                        targetCategory: val === "category" ? getTargetCategory(value) : undefined,
                        targetProductType: val === "type" ? getTargetProductType(value) : undefined,
                        percentage: getPercentage(value) || 0,
                      })
                    }}
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="product">Producto/Combo Específico</SelectItem>
                      <SelectItem value="category">Categoría</SelectItem>
                      <SelectItem value="type">Tipo</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                {getTargetFilterType(value) === "product" || !getTargetFilterType(value) ? (
                  <div className="space-y-2">
                    <Label>Producto o Combo</Label>
                    <Button
                      type="button"
                      variant="outline"
                      className="w-full justify-start"
                      onClick={() => setTargetItemDialogOpen(true)}
                    >
                      {selectedTargetItem
                        ? `${selectedTargetItem.name}${selectedTargetItem.isCombo ? " (Combo)" : ""}`
                        : "Seleccionar producto o combo..."}
                    </Button>
                    <SingleItemSelector
                      isOpen={targetItemDialogOpen}
                      onClose={() => setTargetItemDialogOpen(false)}
                      onSelect={(item) => {
                        if (item) {
                          handleActionChange("percentageDiscount", {
                            targetType: "ORDER_ITEM",
                            targetFilterType: "product",
                            targetItemId: item.id,
                            percentage: getPercentage(value) || 0,
                          })
                        }
                      }}
                      initialItemId={getTargetItemId(value) || null}
                      allowProducts={true}
                      allowCombos={true}
                      title="Seleccionar Producto o Combo"
                      description="Selecciona el producto o combo al que se aplicará el descuento porcentual"
                    />
                  </div>
                ) : getTargetFilterType(value) === "category" ? (
                  <div className="space-y-2">
                    <Label>Categoría</Label>
                    <Select
                      value={getTargetCategory(value) || ""}
                      onValueChange={(val) => {
                        handleActionChange("percentageDiscount", {
                          targetType: "ORDER_ITEM",
                          targetFilterType: "category",
                          targetCategory: val,
                          percentage: getPercentage(value) || 0,
                        })
                      }}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Seleccionar categoría" />
                      </SelectTrigger>
                      <SelectContent>
                        {availableCategories.length > 0 ? (
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
                ) : (
                  <div className="space-y-2">
                    <Label>Tipo</Label>
                    <Select
                      value={getTargetProductType(value) || ""}
                      onValueChange={(val) => {
                        handleActionChange("percentageDiscount", {
                          targetType: "ORDER_ITEM",
                          targetFilterType: "type",
                          targetProductType: val,
                          percentage: getPercentage(value) || 0,
                        })
                      }}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Seleccionar tipo" />
                      </SelectTrigger>
                      <SelectContent>
                        {availableTypes.length > 0 ? (
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
                )}
              </div>
            )}
            <div className="space-y-2">
              <Label>Porcentaje de Descuento</Label>
              <Input
                type="number"
                min="0"
                max="100"
                value={getPercentage(value) || ""}
                onChange={(e) => {
                  handleActionChange("percentageDiscount", {
                    targetType: value?.targetType || "ORDER",
                    targetItemId: getTargetItemId(value),
                    percentage: parseFloat(e.target.value) || 0,
                  })
                }}
                placeholder="20"
              />
            </div>
          </>
        )}

        {actionType === "freeProduct" && (
          <>
            <div className="space-y-2">
              <Label>Target</Label>
              <Select value="ORDER" disabled>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ORDER">Orden Completa</SelectItem>
                </SelectContent>
              </Select>
              <p className="text-xs text-muted-foreground">
                Los productos gratis se aplican a nivel de orden
              </p>
            </div>
            <div className="space-y-2">
              <Label>Producto o Combo Gratis</Label>
              <Button
                type="button"
                variant="outline"
                className="w-full justify-start"
                onClick={() => setProductDialogOpen(true)}
              >
                {selectedItem
                  ? `${selectedItem.name}${selectedItem.isCombo ? " (Combo)" : ""}`
                  : "Seleccionar producto o combo gratis..."}
              </Button>
              <SingleItemSelector
                isOpen={productDialogOpen}
                onClose={() => setProductDialogOpen(false)}
                onSelect={(item) => {
                  if (item) {
                    handleActionChange("freeProduct", {
                      targetType: "ORDER",
                      productId: item.id,
                      quantity: getQuantity(value) || 1,
                    })
                  }
                }}
                initialItemId={getProductId(value) || null}
                allowProducts={true}
                allowCombos={true}
                title="Seleccionar Producto o Combo Gratis"
                description="Selecciona el producto o combo que se dará gratis"
              />
            </div>
            <div className="space-y-2">
              <Label>Cantidad</Label>
              <Input
                type="number"
                min="1"
                value={getQuantity(value) || ""}
                onChange={(e) => {
                  handleActionChange("freeProduct", {
                    targetType: "ORDER",
                    productId: getProductId(value) || 0,
                    quantity: parseInt(e.target.value) || 1,
                  })
                }}
                placeholder="1"
              />
            </div>
          </>
        )}

        {actionType === "quantityDiscount" && (
          <>
            <div className="space-y-2">
              <Label>Target</Label>
              <Select value="ORDER_ITEM" disabled>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ORDER_ITEM">Items Específicos</SelectItem>
                </SelectContent>
              </Select>
              <p className="text-xs text-muted-foreground">
                Los descuentos por cantidad se aplican a items específicos
              </p>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label>Lleva (Cantidad)</Label>
                <Input
                  type="number"
                  min="1"
                  value={getBuyQuantity(value) || ""}
                  onChange={(e) => {
                    handleActionChange("quantityDiscount", {
                      targetType: "ORDER_ITEM",
                      buyQuantity: parseInt(e.target.value) || 0,
                      payQuantity: getPayQuantity(value) || 0,
                    })
                  }}
                  placeholder="3"
                />
              </div>
              <div className="space-y-2">
                <Label>Paga (Cantidad)</Label>
                <Input
                  type="number"
                  min="1"
                  value={getPayQuantity(value) || ""}
                  onChange={(e) => {
                    handleActionChange("quantityDiscount", {
                      targetType: "ORDER_ITEM",
                      buyQuantity: getBuyQuantity(value) || 0,
                      payQuantity: parseInt(e.target.value) || 0,
                    })
                  }}
                  placeholder="2"
                />
              </div>
            </div>
          </>
        )}
      </CardContent>
    </Card>
  )
}


