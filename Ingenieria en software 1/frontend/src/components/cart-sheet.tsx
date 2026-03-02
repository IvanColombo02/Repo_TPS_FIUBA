"use client"

import { useMemo, useState, useEffect } from "react"
import { ShoppingCart, X, Gift } from "lucide-react"

import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle, SheetTrigger } from "@/components/ui/sheet"
import { Separator } from "@/components/ui/separator"
import { PAYMENT_METHOD_OPTIONS } from "@/models/orders"
import type { CartItem, OrderPricingSummary, PaymentMethod } from "@/lib/types"
import { calculateCartDiscounts, buildOrderItemsPayload, type CartDiscountCalculationDTO } from "@/lib/api/orders"
import { useToken } from "@/services/TokenContext"

interface CartSheetProps {
  cart: CartItem[]
  onUpdateQuantity: (customizationId: string, quantity: number) => void
  onRemoveItem: (customizationId: string) => void
  onCheckout: (payload: { paymentMethod: PaymentMethod; summary: OrderPricingSummary }) => Promise<void> | void
  isProcessing?: boolean
}

export function CartSheet({ cart, onUpdateQuantity, onRemoveItem, onCheckout, isProcessing = false }: CartSheetProps) {
  const [open, setOpen] = useState(false)
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>(PAYMENT_METHOD_OPTIONS[0]?.value ?? "CASH")
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [discountCalculation, setDiscountCalculation] = useState<CartDiscountCalculationDTO | null>(null)
  const [isCalculatingDiscounts, setIsCalculatingDiscounts] = useState(false)
  const [tokenState] = useToken()
  const accessToken = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : null

  const cartItemCount = useMemo(() => cart.reduce((count, item) => count + item.quantity, 0), [cart])


  useEffect(() => {
    const calculateDiscounts = async () => {
      const chargeableItems = cart.filter((item) => !item.customizationId?.startsWith("free-"))
      if (chargeableItems.length === 0 || !accessToken) {
        setDiscountCalculation(null)
        return
      }

      try {
        setIsCalculatingDiscounts(true)
        const itemsPayload = buildOrderItemsPayload(chargeableItems)
        const result = await calculateCartDiscounts(accessToken, itemsPayload)
        setDiscountCalculation(result)
      } catch (error) {
        console.error("Error calculating discounts:", error)
        setDiscountCalculation(null)
      } finally {
        setIsCalculatingDiscounts(false)
      }
    }

    if (open) {
      calculateDiscounts()
    }
  }, [cart, open, accessToken])

  const originalCartSubtotal = discountCalculation?.originalSubtotal ?? 0
  const totalDiscount = discountCalculation?.totalDiscount ?? 0
  const cartTotal = discountCalculation?.finalTotal ?? 0
  const appliedDiscounts = discountCalculation?.appliedDiscounts ?? []
  const promotionDescriptions = discountCalculation?.promotionDescriptions ?? []

  const isBusy = isProcessing || isSubmitting || isCalculatingDiscounts

  const checkoutSummary: OrderPricingSummary = useMemo(
    () => ({
      subtotal: Number(originalCartSubtotal.toFixed(2)),
      total: Number(cartTotal.toFixed(2)),
      discountTotal: Number(totalDiscount.toFixed(2)),
      promotionDescriptions,
    }),
    [originalCartSubtotal, cartTotal, totalDiscount, promotionDescriptions],
  )

  const handleCheckout = async () => {
    if (cart.length === 0 || isBusy) {
      return
    }

    try {
      setIsSubmitting(true)
      await onCheckout({ paymentMethod, summary: checkoutSummary })
      setOpen(false)
    } catch (error) {
      console.error("No se pudo completar el pedido", error)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <Sheet open={open} onOpenChange={setOpen}>
      <SheetTrigger asChild>
        <Button variant="outline" size="icon" className="relative bg-transparent text-foreground hover:text-foreground">
          <ShoppingCart className="h-5 w-5" />
          {cartItemCount > 0 && (
            <Badge className="absolute -right-2 -top-2 h-6 w-6 rounded-full p-0 flex items-center justify-center bg-primary text-primary-foreground">
              {cartItemCount}
            </Badge>
          )}
        </Button>
      </SheetTrigger>
      <SheetContent className="w-full sm:max-w-lg">
        <SheetHeader>
          <SheetTitle>Tu Carrito</SheetTitle>
          <SheetDescription>
            {cartItemCount === 0
              ? "Tu carrito está vacío"
              : `${cartItemCount} ${cartItemCount === 1 ? "artículo" : "artículos"} en tu carrito`}
          </SheetDescription>
        </SheetHeader>

        <div className="mt-8 flex h-[calc(100vh-12rem)] flex-col space-y-4">
          {cart.length === 0 ? (
            <p className="text-center text-muted-foreground py-8">Añade productos para comenzar tu pedido</p>
          ) : (
            <>
              <div className="flex-1 overflow-y-auto pr-1">
                <div className="space-y-4">
                  {cart.map((item) => {
                    const isFreeProduct = item.customizationId.startsWith("free-")
                    return (
                      <div key={item.customizationId} className="flex gap-4 pb-4 border-b border-border last:border-b-0">
                        <img
                          src={item.image || "/placeholder.svg"}
                          alt={item.name}
                          className="h-20 w-20 rounded-md object-cover"
                        />
                        <div className="flex-1">
                          <div className="flex items-center gap-2">
                            <h4 className="font-semibold text-foreground">{item.name}</h4>
                            {isFreeProduct && (
                              <Badge className="bg-green-500 text-white text-xs">Gratis</Badge>
                            )}
                          </div>
                          {isFreeProduct ? (
                            <p className="text-sm text-green-600 font-semibold">Gratis</p>
                          ) : (
                            <div className="flex items-center gap-2">
                              <p className="text-sm font-semibold text-foreground">${item.price.toFixed(2)} ARS</p>
                              {item.originalPrice && item.originalPrice > item.price && (
                                <p className="text-sm text-muted-foreground line-through">${item.originalPrice.toFixed(2)} ARS</p>
                              )}
                            </div>
                          )}
                          {!isFreeProduct && (
                            <div className="mt-2 flex items-center gap-2">
                              <Button
                                variant="outline"
                                size="icon"
                                className="h-8 w-8 bg-transparent text-foreground hover:text-foreground"
                                onClick={() => onUpdateQuantity(item.customizationId!, item.quantity - 1)}
                              >
                                -
                              </Button>
                              <span className="w-8 text-center text-foreground">{item.quantity}</span>
                              <Button
                                variant="outline"
                                size="icon"
                                className="h-8 w-8 bg-transparent text-foreground hover:text-foreground"
                                onClick={() => onUpdateQuantity(item.customizationId!, item.quantity + 1)}
                              >
                                +
                              </Button>
                            </div>
                          )}
                          {isFreeProduct && (
                            <p className="text-xs text-muted-foreground mt-1">Cantidad: {item.quantity}</p>
                          )}
                        </div>
                        {!isFreeProduct && (
                          <Button variant="ghost" size="icon" onClick={() => onRemoveItem(item.customizationId!)}>
                            <X className="h-4 w-4" />
                          </Button>
                        )}
                      </div>
                    )
                  })}
                </div>
              </div>

              <Separator />

              {isCalculatingDiscounts && (
                <div className="text-center text-sm text-muted-foreground py-2">
                  Calculando descuentos...
                </div>
              )}

              {!isCalculatingDiscounts && appliedDiscounts.length > 0 && (
                <div className="space-y-2 p-3 bg-green-500/10 rounded-lg border border-green-500/20">
                  <div className="flex items-center gap-2 text-green-600 font-semibold">
                    <Gift className="h-4 w-4" />
                    <span>Promociones aplicadas</span>
                  </div>
                  {appliedDiscounts.map((ad, index) => (
                    <div key={index} className="flex justify-between text-sm">
                      <span className="text-muted-foreground">{ad.description}</span>
                      <span className="text-green-600 font-medium">-{ad.discount.toFixed(2)}ARS</span>
                    </div>
                  ))}
                </div>
              )}

              <div className="space-y-2">
                <p className="text-sm font-medium text-foreground">Método de pago</p>
                <Select value={paymentMethod} onValueChange={(value: PaymentMethod) => setPaymentMethod(value)}>
                  <SelectTrigger className="w-full justify-between text-left">
                    <SelectValue placeholder="Selecciona un método" />
                  </SelectTrigger>
                  <SelectContent>
                    {PAYMENT_METHOD_OPTIONS.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>

              </div>

              <div className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Subtotal (sin promociones)</span>
                  <span className="text-foreground">{originalCartSubtotal.toFixed(2)}ARS</span>
                </div>
                {totalDiscount > 0 && (
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">Descuentos</span>
                    <span className="text-green-600 font-medium">-{totalDiscount.toFixed(2)}ARS</span>
                  </div>
                )}
                <Separator />
                <div className="flex justify-between text-lg font-semibold">
                  <span className="text-foreground">Total</span>
                  <span className="text-primary">{cartTotal.toFixed(2)}ARS</span>
                </div>
              </div>

              <Button
                className="w-full"
                size="lg"
                onClick={handleCheckout}
                disabled={cart.length === 0 || isBusy}
                aria-busy={isBusy}
              >
                Realizar Pedido
              </Button>
            </>
          )}
        </div>
      </SheetContent>
    </Sheet>
  )
}
