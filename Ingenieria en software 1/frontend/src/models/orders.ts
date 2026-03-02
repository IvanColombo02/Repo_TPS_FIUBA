import type { OrderStatus, PaymentMethod } from "@/lib/types"

export const ORDER_STATUS_FLOW: OrderStatus[] = ["PENDING", "IN_PREPARATION", "READY", "DELIVERED"]

export const ORDER_STATUS_LABELS: Record<OrderStatus, string> = {
    PENDING: "Pendiente",
    IN_PREPARATION: "En preparación",
    READY: "Listo para entregar",
    DELIVERED: "Entregado",
    CANCELLED: "Cancelado",
}

export const ORDER_STATUS_DESCRIPTIONS: Record<OrderStatus, string> = {
    PENDING: "Pedido recibido, en espera de preparación.",
    IN_PREPARATION: "El equipo está preparando el pedido.",
    READY: "El pedido está listo para ser entregado.",
    DELIVERED: "El pedido fue entregado al cliente.",
    CANCELLED: "El pedido fue cancelado.",
}

export const ORDER_STATUS_BADGE_VARIANTS: Record<OrderStatus, "default" | "secondary" | "destructive" | "outline"> = {
    PENDING: "secondary",
    IN_PREPARATION: "outline",
    READY: "default",
    DELIVERED: "default",
    CANCELLED: "destructive",
}

export const PAYMENT_METHOD_LABELS: Record<PaymentMethod, string> = {
    CASH: "Efectivo",
    CARD: "Tarjeta",
    MP: "Mercado Pago",
}

export const PAYMENT_METHOD_OPTIONS: Array<{ value: PaymentMethod; label: string }> = [
    { value: "CASH", label: PAYMENT_METHOD_LABELS.CASH },
    { value: "CARD", label: PAYMENT_METHOD_LABELS.CARD },
    { value: "MP", label: PAYMENT_METHOD_LABELS.MP },
]

export function getNextStatus(status: OrderStatus): OrderStatus | null {
    if (status === "CANCELLED") {
        return null
    }
    const currentIndex = ORDER_STATUS_FLOW.indexOf(status)
    if (currentIndex === -1 || currentIndex === ORDER_STATUS_FLOW.length - 1) {
        return null
    }
    return ORDER_STATUS_FLOW[currentIndex + 1]
}

export function canEmployeeCancelOrder(status: OrderStatus): boolean {
    return status !== "DELIVERED" && status !== "CANCELLED"
}
