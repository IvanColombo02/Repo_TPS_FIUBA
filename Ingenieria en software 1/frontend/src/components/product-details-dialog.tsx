"use client"

import { ShoppingCart, UtensilsCrossed, Clock, Gift } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Separator } from "@/components/ui/separator"

import type { MenuItem } from "@/lib/types"
import type { PromotionDTO } from "@/lib/api/promotions"
import { fetchProductById } from "@/lib/api/products"
import { fetchComboById } from "@/lib/api/combos"
import { mapProductToMenuItem } from "@/lib/utils/item-helpers"
import { useState, useEffect } from "react"

interface ProductDetailsDialogProps {
  product: MenuItem | null
  open: boolean
  onOpenChange: (open: boolean) => void
  onAddToCart: () => void

  promotions?: PromotionDTO[]
  accessToken: string | null
}

const getPromotionDetails = (promotion: PromotionDTO, productPrice: number, originalPrice?: number) => {
  try {
    const expression = JSON.parse(promotion.expression)
    const action = expression.action
    let badge: string | null = "Promo"
    let description = promotion.description
    let amount = "Promoción"

    if (action?.type === "percentageDiscount" && action?.percentage) {
      badge = `-${action.percentage}%`
      description = `${action.percentage}% de descuento`
      amount = originalPrice
        ? `-$${((originalPrice - productPrice)).toFixed(2)} ARS`
        : `-${action.percentage}%`
    } else if (action?.type === "fixedDiscount" && action?.amount) {
      badge = `-$${action.amount}`
      description = `$${action.amount} de descuento`
      amount = `-$${action.amount} ARS`
    } else if (action?.type === "quantityDiscount") {
      badge = `${action.buyQuantity}x${action.payQuantity}`
      description = `Lleva ${action.buyQuantity}, paga ${action.payQuantity}`
      amount = badge
    } else if (action?.type === "freeProduct") {
      badge = "Gratis"
      description = "Producto gratis incluido"
      amount = "Gratis"
    }

    return { badge, description, amount }
  } catch {
    return { badge: "Promo", description: promotion.description, amount: "Promoción" }
  }
}

export function ProductDetailsDialog({
  product,
  open,
  onOpenChange,
  onAddToCart,
  promotions = [],
  accessToken,
}: ProductDetailsDialogProps) {
  const [details, setDetails] = useState<MenuItem | null>(null)
  const [loadingDetails, setLoadingDetails] = useState(false)

  useEffect(() => {
    if (!product || !open) {
      setDetails(null)
      return
    }

    const hasIngredients = !product.isCombo && product.ingredients && product.ingredients.length > 0
    const hasComboProducts = product.isCombo && product.comboProducts && product.comboProducts.length > 0

    if (hasIngredients || hasComboProducts) {
      setDetails(product)
      return
    }

    if (!accessToken) return

    let cancelled = false
    setLoadingDetails(true)

    const loadDetails = async () => {
      try {
        let fullItem: MenuItem
        if (product.isCombo) {
          const comboId = product.comboId || (product.id as number)
          const comboFull = await fetchComboById(accessToken, comboId)
          fullItem = {
            ...product,
            comboProducts: comboFull.products.map(p => ({
              id: p.id,
              name: p.name,
              quantity: p.quantity
            }))
          }
        } else {
          const productFull = await fetchProductById(accessToken, product.id as number)
          fullItem = mapProductToMenuItem(productFull)
        }

        if (!cancelled) {
          setDetails(fullItem)
        }
      } catch (error) {
        console.error("Error loading details:", error)

        if (!cancelled) {
          setDetails(product)
        }
      } finally {
        if (!cancelled) {
          setLoadingDetails(false)
        }
      }
    }

    loadDetails()

    return () => {
      cancelled = true
      setLoadingDetails(false)
    }
  }, [product, open, accessToken])

  const displayProduct = details || product
  const handleAddToCart = () => {
    onAddToCart()
    onOpenChange(false)
  }

  if (!product || !displayProduct) return null

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto">
        <DialogHeader className="space-y-3">
          <div className="flex items-start justify-between gap-4">
            <div className="flex-1">
              <DialogTitle className="text-xl md:text-2xl lg:text-3xl font-bold text-foreground mb-2">
                {displayProduct.name}
              </DialogTitle>
              <DialogDescription className="text-sm lg:text-base">{displayProduct.description}</DialogDescription>
            </div>
            <Badge variant="secondary" className="text-xs lg:text-sm px-2 lg:px-3 py-1 shrink-0">
              {displayProduct.category}
            </Badge>
          </div>
          {promotions.length > 0 && (
            <div className="flex flex-wrap gap-2">
              {promotions.map((promo) => {
                const { badge } = getPromotionDetails(promo, displayProduct.price, displayProduct.originalPrice)
                return (
                  <Badge key={promo.id} className="flex items-center gap-1 bg-primary text-primary-foreground">
                    <Gift className="h-3 w-3" />
                    {badge}: {promo.name}
                  </Badge>
                )
              })}
            </div>
          )}
        </DialogHeader>

        <div className="space-y-4 md:space-y-6 mt-4">
          <div className="relative overflow-hidden rounded-xl border-2 border-border shadow-lg">
            <img
              src={displayProduct.image || "/placeholder.svg"}
              alt={displayProduct.name}
              className="w-full h-56 md:h-72 object-cover"
            />
            {displayProduct.discount && (
              <Badge className="absolute top-3 md:top-4 right-3 md:right-4 bg-primary text-primary-foreground text-sm lg:text-base px-2 lg:px-3 py-1 shadow-md">
                -{displayProduct.discount}% OFF
              </Badge>
            )}
          </div>

          <div className="flex flex-col sm:flex-row items-start justify-between gap-4 p-3 md:p-4 bg-muted/30 rounded-lg border border-border">
            <div className="flex-1 w-full">
              <div className="flex items-baseline gap-2 md:gap-3 mb-2">
                <p className="text-2xl md:text-3xl lg:text-4xl font-bold text-primary">{displayProduct.price.toFixed(2)}ARS</p>

              </div>
              <div className="flex items-center gap-2 text-sm lg:text-base text-muted-foreground mt-2">
                <Clock className="h-4 w-4 lg:h-5 lg:w-5" />
                <span>Tiempo de preparación aproximado: {typeof displayProduct.preparationTime === 'number' ? `${displayProduct.preparationTime} min` : displayProduct.preparationTime}</span>
              </div>
            </div>
          </div>

          {promotions.length > 0 && (
            <div className="space-y-3 md:space-y-4 p-4 md:p-5 bg-card rounded-lg border border-border shadow-sm">
              <h3 className="text-base md:text-lg lg:text-xl font-bold text-foreground flex items-center gap-2">
                <Gift className="h-4 w-4 lg:h-5 lg:w-5 text-primary" />
                Promociones Aplicadas
              </h3>
              <Separator />
              <div className="space-y-2">
                {promotions.map((promo) => {
                  const { description, amount } = getPromotionDetails(promo, displayProduct.price, displayProduct.originalPrice)

                  return (
                    <div
                      key={promo.id}
                      className="flex items-center justify-between p-3 md:p-4 rounded-md bg-muted/20 border border-border/50"
                    >
                      <div className="flex items-center gap-3">
                        <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center">
                          <Gift className="h-5 w-5 text-primary" />
                        </div>
                        <div className="flex flex-col">
                          <span className="text-sm lg:text-base font-medium text-foreground">{promo.name}</span>
                          <span className="text-xs lg:text-sm text-muted-foreground">{description}</span>
                        </div>
                      </div>
                      <Badge variant="secondary" className="text-xs lg:text-sm bg-primary text-primary-foreground">
                        {amount}
                      </Badge>
                    </div>
                  )
                })}
              </div>
            </div>
          )}

          {displayProduct.isCombo && (
            <div className="space-y-3 md:space-y-4 p-4 md:p-5 bg-card rounded-lg border border-border shadow-sm">
              <h3 className="text-base md:text-lg lg:text-xl font-bold text-foreground flex items-center gap-2">
                <UtensilsCrossed className="h-4 w-4 lg:h-5 lg:w-5 text-primary" />
                Productos incluidos
              </h3>
              <Separator />
              {loadingDetails ? (
                <div className="flex justify-center py-4">
                  <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary"></div>
                </div>
              ) : (
                <div className="space-y-2">
                  {displayProduct.comboProducts && displayProduct.comboProducts.length > 0 ? (
                    displayProduct.comboProducts.map((comboProduct) => (
                      <div
                        key={comboProduct.id}
                        className="flex items-center justify-between p-3 md:p-4 rounded-md bg-muted/20 border border-border/50"
                      >
                        <div className="flex items-center gap-3">
                          <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center">
                            <UtensilsCrossed className="h-5 w-5 text-primary" />
                          </div>
                          <span className="text-sm lg:text-base font-medium text-foreground">{comboProduct.name}</span>
                        </div>
                        <Badge variant="secondary" className="text-xs lg:text-sm">
                          x{comboProduct.quantity}
                        </Badge>
                      </div>
                    ))
                  ) : (
                    <p className="text-sm text-muted-foreground">No hay información de productos disponible.</p>
                  )}
                </div>
              )}
            </div>
          )}


          {!displayProduct.isCombo && (
            <div className="space-y-3 md:space-y-4 p-4 md:p-5 bg-card rounded-lg border border-border shadow-sm">
              <h3 className="text-base md:text-lg lg:text-xl font-bold text-foreground flex items-center gap-2">
                <UtensilsCrossed className="h-4 w-4 lg:h-5 lg:w-5 text-primary" />
                Ingredientes
              </h3>
              <Separator />
              {loadingDetails ? (
                <div className="flex justify-center py-4">
                  <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary"></div>
                </div>
              ) : (
                <div className="space-y-2">
                  {displayProduct.ingredients && displayProduct.ingredients.length > 0 ? (
                    displayProduct.ingredients.map((ingredient) => (
                      <div
                        key={ingredient.id}
                        className="flex items-center justify-between p-3 md:p-4 rounded-md bg-muted/20 border border-border/50"
                      >
                        <div className="flex items-center gap-3">
                          <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center">
                            <UtensilsCrossed className="h-5 w-5 text-primary" />
                          </div>
                          <span className="text-sm lg:text-base font-medium text-foreground">{ingredient.name}</span>
                        </div>
                        {ingredient.quantity && (
                          <Badge variant="secondary" className="text-xs lg:text-sm">
                            x{ingredient.quantity}
                          </Badge>
                        )}
                      </div>
                    ))
                  ) : (
                    <p className="text-sm text-muted-foreground">No hay información de ingredientes disponible.</p>
                  )}
                </div>
              )}
            </div>
          )}
        </div>

        <DialogFooter className="flex flex-col sm:flex-row gap-2 sm:gap-3 mt-4 md:mt-6 pt-4 md:pt-6 border-t border-border">
          <Button
            variant="outline"
            onClick={() => onOpenChange(false)}
            className="text-foreground bg-transparent w-full sm:w-auto"
          >
            Cancelar
          </Button>
          <Button onClick={handleAddToCart} className="w-full sm:w-auto" size="lg">
            <ShoppingCart className="h-4 w-4 mr-2" />
            Añadir al carrito
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
