import { useMemo, type JSX } from "react"
import {
  ArrowLeft,
  ShoppingBag,
  Clock,
  CheckCircle,
  XCircle,
  ChefHat,
  PackageCheck,
} from "lucide-react"
import { Link } from "wouter"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { useMyOrders, useCancelMyOrder } from "@/hooks/use-orders"
import { useToken } from "@/services/TokenContext"
import type { Order, OrderStatus } from "@/lib/types"
import {
  ORDER_STATUS_BADGE_VARIANTS,
  ORDER_STATUS_DESCRIPTIONS,
  ORDER_STATUS_LABELS,
  PAYMENT_METHOD_LABELS,
} from "@/models/orders"
import { useToast } from "@/hooks/use-toast"

const statusIconMap: Record<OrderStatus, JSX.Element> = {
  PENDING: <Clock className="h-4 w-4 text-amber-500" />,
  IN_PREPARATION: <ChefHat className="h-4 w-4 text-sky-500" />,
  READY: <CheckCircle className="h-4 w-4 text-emerald-500" />,
  DELIVERED: <PackageCheck className="h-4 w-4 text-green-500" />,
  CANCELLED: <XCircle className="h-4 w-4 text-red-500" />,
}

const formatDateTime = (iso: string) =>
  new Date(iso).toLocaleString("es-AR", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  })

const ACTIVE_ORDER_STATUSES: OrderStatus[] = ["PENDING", "IN_PREPARATION", "READY"]
const HISTORY_ORDER_STATUSES: OrderStatus[] = ["DELIVERED", "CANCELLED"]

const renderOrderCard = (
  order: Order,
  options: {
    showCancelButton: boolean
    onCancel?: (orderId: string) => void
    disableCancel?: boolean
  },
) => {
  const { showCancelButton, onCancel, disableCancel } = options

  return (
    <Card key={order.id} className="border-border shadow-lg">
      <CardHeader className="pb-4">
        <div className="flex items-start justify-between gap-4">
          <div>
            <CardTitle className="text-lg text-foreground">Pedido {order.id}</CardTitle>
            <CardDescription>
              Creado el {formatDateTime(order.createdAt)}
              {order.updatedAt && order.updatedAt !== order.createdAt && (
                <span className="block">Actualizado {formatDateTime(order.updatedAt)}</span>
              )}
            </CardDescription>
            <div className="mt-2 text-sm text-muted-foreground">
              Pago con {PAYMENT_METHOD_LABELS[order.paymentMethod]}
            </div>
          </div>
          <div className="flex flex-col items-end gap-2">
            <Badge variant={ORDER_STATUS_BADGE_VARIANTS[order.status]} className="flex items-center gap-1">
              {statusIconMap[order.status]}
              {ORDER_STATUS_LABELS[order.status]}
            </Badge>
            {showCancelButton && onCancel && (
              <Button
                variant="outline"
                size="sm"
                className="text-destructive hover:text-destructive"
                disabled={disableCancel}
                onClick={() => onCancel(order.id)}
              >
                Cancelar pedido
              </Button>
            )}
          </div>
        </div>
        <p className="mt-3 text-sm text-muted-foreground">{ORDER_STATUS_DESCRIPTIONS[order.status]}</p>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="space-y-2">
          {order.items.map((item) => {
            const itemTotalWithoutDiscount = item.itemPrice * item.quantity
            const itemSubtotal = item.subtotal || itemTotalWithoutDiscount
            const hasDiscount = item.discount && item.discount > 0
            const isFree = hasDiscount && item.discount! >= itemTotalWithoutDiscount
            
            return (
              <div key={item.id} className="flex items-center justify-between text-sm">
                <div className="flex items-center gap-2">
                  <span className="text-foreground">
                    {item.quantity}x {item.itemName}
                  </span>
                  {isFree && (
                    <Badge className="bg-green-500 text-white text-xs">Gratis</Badge>
                  )}
                </div>
                <div className="flex items-center gap-2">
                  {hasDiscount && !isFree && (
                    <span className="text-muted-foreground line-through text-xs">
                      ${itemTotalWithoutDiscount.toFixed(2)}
                    </span>
                  )}
                  {isFree && (
                    <span className="text-muted-foreground line-through text-xs">
                      ${itemTotalWithoutDiscount.toFixed(2)}
                    </span>
                  )}
                  <span className={
                    isFree 
                      ? "text-green-600 font-semibold" 
                      : hasDiscount 
                        ? "text-foreground font-medium" 
                        : "text-muted-foreground"
                  }>
                    ${isFree ? "0.00" : itemSubtotal.toFixed(2)}
                  </span>
                </div>
              </div>
            )
          })}
        </div>

        {order.promotionDescriptions && order.promotionDescriptions.length > 0 && (
          <div className="rounded-md border border-primary/30 bg-primary/5 p-3 text-sm text-foreground">
            <p className="font-medium text-primary mb-1">Promociones aplicadas</p>
            <ul className="list-disc pl-5 space-y-1 text-muted-foreground">
              {order.promotionDescriptions.map((promo, index) => (
                <li key={`${order.id}-promo-${index}`}>{promo}</li>
              ))}
            </ul>
          </div>
        )}

        <div className="border-t border-border pt-3 space-y-2 text-sm">
          {typeof order.subtotal === "number" && order.subtotal !== order.totalPrice && (
            <div className="flex items-center justify-between">
              <span className="text-muted-foreground">Subtotal</span>
              <span className="text-foreground">${order.subtotal.toFixed(2)}</span>
            </div>
          )}
          {order.discountTotal && order.discountTotal > 0 && (
            <div className="flex items-center justify-between text-green-600">
              <span>Descuentos aplicados</span>
              <span className="font-medium">-${order.discountTotal.toFixed(2)}</span>
            </div>
          )}
          <div className="flex items-center justify-between font-semibold text-base pt-1">
            <span className="text-foreground">Total</span>
            <span className="text-foreground">${order.totalPrice.toFixed(2)}</span>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}

export const MyOrdersScreen = () => {
  const { data: orders = [], isLoading, isFetching, isError, error } = useMyOrders()
  const cancelOrderMutation = useCancelMyOrder()
  const [tokenState] = useToken()
  const { toast } = useToast()

  const userOrders = useMemo(
    () =>
      [...orders].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()),
    [orders],
  )

  const activeOrders = useMemo(
    () => userOrders.filter((order) => ACTIVE_ORDER_STATUSES.includes(order.status)),
    [userOrders],
  )

  const historyOrders = useMemo(
    () => userOrders.filter((order) => HISTORY_ORDER_STATUSES.includes(order.status)),
    [userOrders],
  )

  const handleCancel = async (orderId: string) => {
    const order = userOrders.find((item) => item.id === orderId)
    if (!order || order.status !== "PENDING") {
      return
    }

    try {
      await cancelOrderMutation.mutateAsync(orderId)
      toast({
        title: "Pedido cancelado",
        description: `El pedido ${orderId} se canceló correctamente.`,
      })
    } catch (error) {
      const message = error instanceof Error ? error.message : "No se pudo cancelar el pedido"
      toast({
        title: "Error al cancelar",
        description: message,
        variant: "destructive",
      })
    }
  }

  const errorMessage = isError ? (error instanceof Error ? error.message : "No se pudieron cargar tus pedidos") : null

  return (
    <div className="min-h-screen bg-background dark">
      <header className="sticky top-0 z-50 border-b border-border bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="container mx-auto flex h-16 items-center justify-between px-4">
          <div className="flex items-center gap-3">
            <ShoppingBag className="h-8 w-8 text-primary" />
            <h1 className="text-2xl font-bold text-foreground">Mis Pedidos</h1>
          </div>
          <Link href="/">
            <Button variant="outline" className="flex items-center gap-2 text-foreground">
              <ArrowLeft className="h-5 w-5" />
              <span className="hidden sm:inline">Volver al Menú</span>
            </Button>
          </Link>
        </div>
      </header>

      <main className="container mx-auto px-4 py-8 max-w-4xl">
        {errorMessage && (
          <div className="mb-6 rounded-md border border-destructive/40 bg-destructive/10 p-4 text-sm text-destructive">
            {errorMessage}
          </div>
        )}

        {!isLoading && userOrders.length === 0 ? (
          <div className="text-center py-20">
            <div className="flex justify-center mb-6">
              <div className="w-20 h-20 rounded-full bg-primary/10 flex items-center justify-center">
                <ShoppingBag className="h-10 w-10 text-primary" />
              </div>
            </div>
            <h2 className="text-2xl font-bold text-foreground mb-2">
              {tokenState.state === "LOGGED_IN" ? "No tienes pedidos aún" : "Inicia sesión para ver tus pedidos"}
            </h2>
            <p className="text-lg text-muted-foreground mt-2">
              {tokenState.state === "LOGGED_IN"
                ? "Haz tu primer pedido y aparecerá aquí."
                : "Inicia sesión nuevamente para ver tus pedidos."}
            </p>
            <p className="text-sm text-muted-foreground mt-4 max-w-md mx-auto">
              Explora nuestro menú y realiza tu pedido de forma rápida y sencilla.
            </p>
            <Link href="/">
              <Button className="mt-6">Ver Menú</Button>
            </Link>
          </div>
        ) : userOrders.length > 0 ? (
          <div className="space-y-6">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-xl font-semibold text-foreground">Mis pedidos</h2>
                <p className="text-sm text-muted-foreground">Revisa el estado actual y tu historial.</p>
              </div>
              <Badge variant="outline" className="text-sm">
                {userOrders.length} pedido{userOrders.length !== 1 ? "s" : ""}
              </Badge>
            </div>

            <Tabs defaultValue={activeOrders.length > 0 ? "active" : "history"} className="space-y-4">
              <TabsList>
                <TabsTrigger value="active">
                  En curso
                  <Badge variant="secondary" className="ml-2">
                    {activeOrders.length}
                  </Badge>
                </TabsTrigger>
                <TabsTrigger value="history">
                  Historial
                  <Badge variant="outline" className="ml-2">
                    {historyOrders.length}
                  </Badge>
                </TabsTrigger>
              </TabsList>

              <TabsContent value="active" className="space-y-4">
                {activeOrders.length === 0 ? (
                  <div className="rounded-lg border border-dashed border-border p-10 text-center text-muted-foreground">
                    No tienes pedidos activos en este momento.
                  </div>
                ) : (
                  activeOrders.map((order) =>
                    renderOrderCard(order, {
                      showCancelButton: order.status === "PENDING",
                      onCancel: handleCancel,
                      disableCancel: cancelOrderMutation.isPending,
                    }),
                  )
                )}
              </TabsContent>

              <TabsContent value="history" className="space-y-4">
                {historyOrders.length === 0 ? (
                  <div className="rounded-lg border border-dashed border-border p-10 text-center text-muted-foreground">
                    Aún no hay pedidos entregados o cancelados.
                  </div>
                ) : (
                  historyOrders.map((order) =>
                    renderOrderCard(order, {
                      showCancelButton: false,
                    }),
                  )
                )}
              </TabsContent>
            </Tabs>
          </div>
        ) : (
          <div className="flex justify-center py-10">
            <span className="text-sm text-muted-foreground">
              {isFetching ? "Actualizando pedidos..." : "Cargando pedidos..."}
            </span>
          </div>
        )}
      </main>
    </div>
  )
}

