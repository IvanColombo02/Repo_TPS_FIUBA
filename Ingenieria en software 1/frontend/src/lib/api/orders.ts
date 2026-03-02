import { BASE_API_URL } from "@/config/app-query-client"
import type { CartItem, Order, OrderItem, OrderStatus, PaymentMethod } from "@/lib/types"

const ORDERS_ENDPOINT = `${BASE_API_URL}/orders`

type BackendPaymentMethod = "CASH" | "CREDIT_CARD" | "DEBIT_CARD" | "DIGITAL_WALLET"

export interface OrderCreateRequest {
    items: Record<number, number>
    paymentMethod: BackendPaymentMethod
}

export interface OrderItemDTO {
    id: number
    componentId: number
    itemName: string
    itemPrice: number
    quantity: number
    subtotal: number
    discount: number | null
    promotionApplied: string | null
}

export interface OrderDTO {
    id: number
    userId: number
    userEmail: string
    items: OrderItemDTO[]
    status: OrderStatus
    paymentMethod: BackendPaymentMethod
    totalPrice: number
    createdAt: string
    updatedAt: string
}

interface ApiErrorPayload {
    status?: number
    message?: string
    errorCode?: string
}

const paymentMethodMapToBackend: Record<PaymentMethod, BackendPaymentMethod> = {
    CASH: "CASH",
    CARD: "CREDIT_CARD",
    MP: "DIGITAL_WALLET",
}

const paymentMethodMapFromBackend: Record<BackendPaymentMethod, PaymentMethod> = {
    CASH: "CASH",
    CREDIT_CARD: "CARD",
    DEBIT_CARD: "CARD",
    DIGITAL_WALLET: "MP",
}

const toOrderItem = (item: OrderItemDTO): OrderItem => ({
    id: String(item.id),
    componentId: String(item.componentId),
    itemName: item.itemName,
    itemPrice: item.itemPrice,
    quantity: item.quantity,
    subtotal: item.subtotal,
    discount: item.discount,
    promotionApplied: item.promotionApplied,
})

const toOrder = (dto: OrderDTO): Order => {
    const items = dto.items.map(toOrderItem)
    
    const subtotal = items.reduce((sum, item) => sum + (item.itemPrice * item.quantity), 0)
    
    const discountTotal = items.reduce((sum, item) => sum + (item.discount || 0), 0)
    
    const allPromotionDescriptions = items
        .map(item => item.promotionApplied)
        .filter((promo): promo is string => promo !== null && promo !== undefined)
        .flatMap(promo => promo.split("; ").map(p => p.trim()))
        .filter(p => p.length > 0)
    
    const promotionDescriptions = Array.from(new Set(allPromotionDescriptions))
    
    return {
        id: String(dto.id),
        userId: String(dto.userId),
        userEmail: dto.userEmail,
        status: dto.status,
        paymentMethod: paymentMethodMapFromBackend[dto.paymentMethod] ?? "CASH",
        totalPrice: dto.totalPrice,
        subtotal,
        discountTotal: discountTotal > 0 ? discountTotal : undefined,
        promotionDescriptions: promotionDescriptions.length > 0 ? promotionDescriptions : undefined,
        createdAt: dto.createdAt,
        updatedAt: dto.updatedAt,
        items,
    }
}

const parseError = async (response: Response): Promise<Error> => {
    try {
        const data = (await response.json()) as ApiErrorPayload
        const message = data.message || `Error ${response.status}`
        const error = new Error(message)
        Object.assign(error, { status: response.status, errorCode: data.errorCode })
        return error
    } catch {
        return new Error(`Error ${response.status}: ${response.statusText}`)
    }
}

export const buildOrderItemsPayload = (cartItems: CartItem[]): Record<number, number> => {
    const items: Record<number, number> = {}

    for (const item of cartItems) {
        
        if (item.customizationId?.startsWith("free-")) {
            continue
        }

        const componentId = extractComponentId(item)
        if (!componentId) {
            throw new Error("No se pudo determinar el componente del item del carrito")
        }

        items[componentId] = (items[componentId] ?? 0) + item.quantity
    }

    return items
}

const extractComponentId = (item: CartItem): number | null => {
    if (typeof item.comboId === "number") {
        return item.comboId
    }
    if (typeof item.id === "number") {
        return item.id
    }
    if (typeof item.id === "string") {
        const match = item.id.match(/\d+/)
        return match ? Number(match[0]) : null
    }
    return null
}

export interface CartDiscountCalculationDTO {
    originalSubtotal: number
    totalDiscount: number
    finalTotal: number
    appliedDiscounts: Array<{
        promotionId: number | null
        promotionName: string
        discount: number
        description: string
    }>
    promotionDescriptions: string[]
}

export async function calculateCartDiscounts(
    accessToken: string,
    items: Record<number, number>
): Promise<CartDiscountCalculationDTO> {
    const response = await fetch(`${ORDERS_ENDPOINT}/calculate-discounts`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${accessToken}`,
        },
        body: JSON.stringify(items),
    })

    if (!response.ok) {
        throw await parseError(response)
    }

    return response.json()
}

export const ordersApi = {
    buildCreateRequest(cart: CartItem[], method: PaymentMethod): OrderCreateRequest {
        return {
            items: buildOrderItemsPayload(cart),
            paymentMethod: paymentMethodMapToBackend[method] ?? "CASH",
        }
    },
    async createOrder(accessToken: string, payload: OrderCreateRequest): Promise<Order> {
        const response = await fetch(ORDERS_ENDPOINT, {
            method: "POST",
            headers: {
                Authorization: `Bearer ${accessToken}`,
                "Content-Type": "application/json",
            },
            body: JSON.stringify(payload),
        })

        if (!response.ok) {
            throw await parseError(response)
        }

        const data = (await response.json()) as OrderDTO
        return toOrder(data)
    },
    async getMyOrders(accessToken: string): Promise<Order[]> {
        const response = await fetch(ORDERS_ENDPOINT, {
            headers: {
                Authorization: `Bearer ${accessToken}`,
                Accept: "application/json",
            },
        })

        if (!response.ok) {
            throw await parseError(response)
        }

        const data = (await response.json()) as OrderDTO[]
        return data.map(toOrder)
    },
    async cancelMyOrder(accessToken: string, orderId: number): Promise<Order> {
        const response = await fetch(`${ORDERS_ENDPOINT}/${orderId}`, {
            method: "DELETE",
            headers: {
                Authorization: `Bearer ${accessToken}`,
                Accept: "application/json",
            },
        })

        if (!response.ok) {
            throw await parseError(response)
        }

        const data = (await response.json()) as OrderDTO
        return toOrder(data)
    },
    async updateOrderStatus(accessToken: string, orderId: number, status: OrderStatus): Promise<Order> {
        const response = await fetch(`${ORDERS_ENDPOINT}/${orderId}/status`, {
            method: "PATCH",
            headers: {
                Authorization: `Bearer ${accessToken}`,
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ status }),
        })

        if (!response.ok) {
            throw await parseError(response)
        }

        const data = (await response.json()) as OrderDTO
        return toOrder(data)
    },
    async getActiveOrders(accessToken: string): Promise<Order[]> {
        const response = await fetch(`${ORDERS_ENDPOINT}/active`, {
            headers: {
                Authorization: `Bearer ${accessToken}`,
                Accept: "application/json",
            },
        })

        if (!response.ok) {
            throw await parseError(response)
        }

        const data = (await response.json()) as OrderDTO[]
        return data.map(toOrder)
    },
}
