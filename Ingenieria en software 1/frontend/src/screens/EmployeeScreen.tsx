import { useMemo, useState, type JSX } from "react"
import {
  ChefHat,
  ArrowLeft,
  Clock,
  CheckCircle,
  XCircle,
  PackageCheck,
  ListFilter,
  Users,
  ArrowRight,
} from "lucide-react"
import { Button } from "@/components/ui/button"
import { Link } from "wouter"
import { Badge } from "@/components/ui/badge"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"

import { useActiveOrders, useAdvanceOrderStatus, useUpdateOrderStatus } from "@/hooks/use-orders"
import { useToast } from "@/hooks/use-toast"
import type { OrderStatus } from "@/lib/types"
import {
  ORDER_STATUS_BADGE_VARIANTS,
  ORDER_STATUS_DESCRIPTIONS,
  ORDER_STATUS_FLOW,
  ORDER_STATUS_LABELS,
  PAYMENT_METHOD_LABELS,
  getNextStatus,
  canEmployeeCancelOrder,
} from "@/models/orders"

type StatusFilter = "ALL" | OrderStatus

const statusIconMap: Record<OrderStatus, JSX.Element> = {
  PENDING: <Clock className="h-4 w-4 text-amber-500" />,
  IN_PREPARATION: <ChefHat className="h-4 w-4 text-sky-500" />,
  READY: <CheckCircle className="h-4 w-4 text-emerald-500" />,
  DELIVERED: <PackageCheck className="h-4 w-4 text-green-500" />,
  CANCELLED: <XCircle className="h-4 w-4 text-red-500" />,
}

const statusFilters: Array<{ value: StatusFilter; label: string }> = [
  { value: "ALL", label: "Todas" },
  { value: "PENDING", label: "Pendientes" },
  { value: "IN_PREPARATION", label: "En preparación" },
  { value: "READY", label: "Listas" },
  { value: "DELIVERED", label: "Entregadas" },
  { value: "CANCELLED", label: "Canceladas" },
]

const formatDateTime = (iso: string) =>
  new Date(iso).toLocaleString("es-AR", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  })

const buildStatusProgress = (current: OrderStatus) => {
  const currentIndex = ORDER_STATUS_FLOW.findIndex((status) => status === current)
  return ORDER_STATUS_FLOW.map((status, index) => ({
    status,
    completed: index <= currentIndex,
  }))
}

export const EmployeeScreen = () => {
  const { data: orders = [], isLoading, isFetching, isError, error } = useActiveOrders({ refetchInterval: 20_000 })
  const advanceOrderMutation = useAdvanceOrderStatus()
  const updateOrderStatusMutation = useUpdateOrderStatus()
  const { toast } = useToast()
  const [activeFilter, setActiveFilter] = useState<StatusFilter>("ALL")
  const [processingOrderId, setProcessingOrderId] = useState<string | null>(null)

  const metrics = useMemo(() => {
    const total = orders.length
    const pending = orders.filter((order) => order.status === "PENDING").length
    const inPreparation = orders.filter((order) => order.status === "IN_PREPARATION").length
    const ready = orders.filter((order) => order.status === "READY").length

    return {
      total,
      pending,
      inPreparation,
      ready,
    }
  }, [orders])

  const filteredOrders = useMemo(() => {
    const ordered = [...orders].sort(
      (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
    )
    if (activeFilter === "ALL") {
      return ordered
    }
    return ordered.filter((order) => order.status === activeFilter)
  }, [activeFilter, orders])

  const handleAdvance = async (orderId: string, currentStatus: OrderStatus) => {
    setProcessingOrderId(orderId)
    try {
      const result = await advanceOrderMutation.mutateAsync({ orderId, currentStatus })
      toast({
        title: "Pedido actualizado",
        description: `El pedido ${result.id} ahora está ${ORDER_STATUS_LABELS[result.status].toLowerCase()}.`,
      })
    } catch (error) {
      const message = error instanceof Error ? error.message : "No se pudo avanzar el estado del pedido"
      toast({
        title: "Error al actualizar",
        description: message,
        variant: "destructive",
      })
    } finally {
      setProcessingOrderId(null)
    }
  }

  const handleCancel = async (orderId: string) => {
    setProcessingOrderId(orderId)
    try {
      const result = await updateOrderStatusMutation.mutateAsync({ orderId, status: "CANCELLED" })
      toast({
        title: "Pedido cancelado",
        description: `El pedido ${result.id} se marcó como cancelado.`,
      })
    } catch (error) {
      const message = error instanceof Error ? error.message : "No se pudo cancelar el pedido"
      toast({
        title: "Error al cancelar",
        description: message,
        variant: "destructive",
      })
    } finally {
      setProcessingOrderId(null)
    }
  }

  const isProcessing = (orderId: string) => processingOrderId === orderId

  return (
    <div className="min-h-screen bg-background dark">
      <header className="sticky top-0 z-50 border-b border-border bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="container mx-auto flex h-16 items-center justify-between px-4">
          <div className="flex items-center gap-3">
            <ChefHat className="h-8 w-8 text-primary" />
            <h1 className="text-2xl font-bold text-foreground">Gestión de Órdenes</h1>
          </div>
          <Link href="/">
            <Button variant="outline" className="flex items-center gap-2 text-foreground">
              <ArrowLeft className="h-5 w-5" />
              <span className="hidden sm:inline">Volver al Menú</span>
            </Button>
          </Link>
        </div>
      </header>

      <main className="container mx-auto px-4 py-8 max-w-6xl space-y-8">
        <section className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <Card>
            <CardHeader className="pb-2">
              <CardDescription>Total de pedidos</CardDescription>
              <CardTitle className="text-3xl text-foreground">{metrics.total}</CardTitle>
            </CardHeader>
            <CardContent className="flex items-center gap-2 text-muted-foreground">
              <ListFilter className="h-4 w-4" />
              Seguimiento general
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="pb-2">
              <CardDescription>En espera</CardDescription>
              <CardTitle className="text-3xl text-foreground">{metrics.pending}</CardTitle>
            </CardHeader>
            <CardContent className="flex items-center gap-2 text-muted-foreground">
              <Clock className="h-4 w-4 text-amber-500" />
              Pedidos listos para comenzar
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="pb-2">
              <CardDescription>Preparándose</CardDescription>
              <CardTitle className="text-3xl text-foreground">{metrics.inPreparation}</CardTitle>
            </CardHeader>
            <CardContent className="flex items-center gap-2 text-muted-foreground">
              <ChefHat className="h-4 w-4 text-sky-500" />
              En cocina
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="pb-2">
              <CardDescription>Listos para entregar</CardDescription>
              <CardTitle className="text-3xl text-foreground">{metrics.ready}</CardTitle>
            </CardHeader>
            <CardContent className="flex items-center gap-2 text-muted-foreground">
              <CheckCircle className="h-4 w-4 text-emerald-500" />
              Pendientes de retiro
            </CardContent>
          </Card>
        </section>

        <section>
          <Tabs value={activeFilter} onValueChange={(value) => setActiveFilter(value as StatusFilter)}>
            <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
              <div>
                <h2 className="text-xl font-semibold text-foreground">Pedidos en curso</h2>
                <p className="text-sm text-muted-foreground">
                  Actualiza el estado de cada pedido a medida que avanza en la cocina.
                </p>
              </div>
              <TabsList>
                {statusFilters.map((filter) => (
                  <TabsTrigger key={filter.value} value={filter.value}>
                    {filter.label}
                  </TabsTrigger>
                ))}
              </TabsList>
            </div>

            {isError && (
              <div className="mb-4 rounded-md border border-destructive/40 bg-destructive/10 p-4 text-sm text-destructive">
                {error instanceof Error ? error.message : "No se pudieron cargar las órdenes."}
              </div>
            )}

            <TabsContent value={activeFilter} className="mt-6">
              {isLoading ? (
                <div className="rounded-lg border border-dashed border-border p-10 text-center text-muted-foreground">
                  Cargando pedidos...
                </div>
              ) : filteredOrders.length === 0 ? (
                <div className="rounded-lg border border-dashed border-border p-10 text-center text-muted-foreground">
                  No hay pedidos en este estado por ahora.
                </div>
              ) : (
                <div className="space-y-4">
                  {filteredOrders.map((order) => {
                    const nextStatus = getNextStatus(order.status)
                    const canCancel = canEmployeeCancelOrder(order.status)
                    const showCancelButton = order.status === "PENDING"
                    const progress = buildStatusProgress(order.status)
                    const processing = isProcessing(order.id)

                    return (
                      <Card key={order.id} className="border-border">
                        <CardHeader className="gap-2">
                          <div className="flex flex-col gap-2 lg:flex-row lg:items-start lg:justify-between">
                            <div>
                              <CardTitle className="text-lg text-foreground">Pedido {order.id}</CardTitle>
                              <CardDescription>
                                Creado el {formatDateTime(order.createdAt)}
                                {order.userEmail && (
                                  <span className="block">
                                    <Users className="mr-1 inline h-3.5 w-3.5" />
                                    Cliente: {order.userEmail}
                                  </span>
                                )}
                              </CardDescription>
                              <div className="mt-2 text-sm text-muted-foreground">
                                Pago con {PAYMENT_METHOD_LABELS[order.paymentMethod]}
                              </div>
                            </div>
                            <Badge
                              className="self-start"
                              variant={ORDER_STATUS_BADGE_VARIANTS[order.status]}
                            >
                              {statusIconMap[order.status]}
                              {ORDER_STATUS_LABELS[order.status]}
                            </Badge>
                          </div>
                          <p className="text-sm text-muted-foreground">
                            {ORDER_STATUS_DESCRIPTIONS[order.status]}
                          </p>
                          <div className="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
                            {progress.map((step, index) => (
                              <div key={`${order.id}-${step.status}`} className="flex items-center gap-2">
                                <div
                                  className={`h-2.5 w-2.5 rounded-full ${step.completed ? "bg-primary" : "bg-border"}`}
                                ></div>
                                <span className={step.completed ? "text-foreground" : "text-muted-foreground"}>
                                  {ORDER_STATUS_LABELS[step.status]}
                                </span>
                                {index < progress.length - 1 && <div className="h-px w-6 bg-border" />}
                              </div>
                            ))}
                          </div>
                        </CardHeader>
                        <CardContent className="space-y-4">
                          <div className="space-y-2 text-sm">
                            {order.items.map((item) => (
                              <div key={item.id} className="flex items-center justify-between">
                                <span className="text-foreground">
                                  {item.quantity}x {item.itemName}
                                </span>
                                <span className="text-muted-foreground">
                                  ${(item.itemPrice * item.quantity).toFixed(2)}
                                </span>
                              </div>
                            ))}
                          </div>

                          <div className="flex flex-wrap items-center justify-between gap-3 text-sm">
                            <div className="space-y-1 text-muted-foreground">
                              {typeof order.subtotal === "number" && (
                                <div>
                                  Subtotal: <span className="text-foreground">${order.subtotal.toFixed(2)}</span>
                                </div>
                              )}
                              {order.discountTotal && order.discountTotal > 0 && (
                                <div>
                                  Descuentos: <span className="text-destructive">-${order.discountTotal.toFixed(2)}</span>
                                </div>
                              )}
                              <div className="font-semibold text-foreground">
                                Total: ${order.totalPrice.toFixed(2)}
                              </div>
                            </div>
                            <div className="flex flex-wrap gap-2">
                              <Button
                                onClick={() => handleAdvance(order.id, order.status)}
                                disabled={!nextStatus || processing || isFetching || advanceOrderMutation.isPending}
                                className="flex items-center gap-2"
                              >
                                <ArrowRight className="h-4 w-4" />
                                {nextStatus ? `Mover a ${ORDER_STATUS_LABELS[nextStatus]}` : "Sin acciones"}
                              </Button>
                              {showCancelButton && (
                                <Button
                                  variant="ghost"
                                  className="text-destructive hover:text-destructive"
                                  onClick={() => handleCancel(order.id)}
                                  disabled={!canCancel || processing || isFetching || updateOrderStatusMutation.isPending}
                                >
                                  <XCircle className="mr-1 h-4 w-4" />
                                  Cancelar
                                </Button>
                              )}
                            </div>
                          </div>

                        </CardContent>
                      </Card>
                    )
                  })}
                </div>
              )}
            </TabsContent>
          </Tabs>
        </section>
      </main>
    </div>
  )
}

