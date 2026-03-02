import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"

import type { CartItem, Order, OrderStatus, PaymentMethod } from "@/lib/types"
import { getNextStatus } from "@/models/orders"
import { ordersApi } from "@/lib/api/orders"
import { useAccessTokenGetter, useToken } from "@/services/TokenContext"

const QUERY_KEY_MY_ORDERS = ["orders", "me"] as const
const QUERY_KEY_ACTIVE_ORDERS = ["orders", "active"] as const

const invalidateOrdersQueries = async (queryClient: ReturnType<typeof useQueryClient>) => {
    await Promise.all([
        queryClient.invalidateQueries({ queryKey: QUERY_KEY_MY_ORDERS, exact: false }).catch(() => { }),
        queryClient.invalidateQueries({ queryKey: QUERY_KEY_ACTIVE_ORDERS, exact: false }).catch(() => { }),
    ])
}

export const useMyOrders = () => {
    const [tokenState] = useToken()
    const getAccessToken = useAccessTokenGetter()
    const isEnabled = tokenState.state === "LOGGED_IN"

    return useQuery<Order[]>({
        queryKey: QUERY_KEY_MY_ORDERS,
        enabled: isEnabled,
        queryFn: async () => {
            const accessToken = await getAccessToken()
            return ordersApi.getMyOrders(accessToken)
        },
        staleTime: 30_000,
        refetchOnWindowFocus: isEnabled,
    })
}

export const useActiveOrders = (options?: { refetchInterval?: number }) => {
    const [tokenState] = useToken()
    const getAccessToken = useAccessTokenGetter()
    const isEnabled = tokenState.state === "LOGGED_IN"
    const refetchInterval = options?.refetchInterval ?? 30_000

    return useQuery<Order[]>({
        queryKey: QUERY_KEY_ACTIVE_ORDERS,
        enabled: isEnabled,
        queryFn: async () => {
            const accessToken = await getAccessToken()
            return ordersApi.getActiveOrders(accessToken)
        },
        refetchInterval: isEnabled ? refetchInterval : false,
        refetchOnWindowFocus: isEnabled,
    })
}

interface CreateOrderInput {
    cartItems: CartItem[]
    paymentMethod: PaymentMethod
}

export const useCreateOrder = () => {
    const getAccessToken = useAccessTokenGetter()
    const queryClient = useQueryClient()

    return useMutation({
        mutationFn: async ({ cartItems, paymentMethod }: CreateOrderInput) => {
            if (cartItems.length === 0) {
                throw new Error("El carrito está vacío")
            }
            const accessToken = await getAccessToken()
            const payload = ordersApi.buildCreateRequest(cartItems, paymentMethod)
            return ordersApi.createOrder(accessToken, payload)
        },
        onSuccess: async () => {
            await invalidateOrdersQueries(queryClient)
        },
    })
}

export const useCancelMyOrder = () => {
    const getAccessToken = useAccessTokenGetter()
    const queryClient = useQueryClient()

    return useMutation({
        mutationFn: async (orderId: string | number) => {
            const accessToken = await getAccessToken()
            return ordersApi.cancelMyOrder(accessToken, Number(orderId))
        },
        onSuccess: async () => {
            await invalidateOrdersQueries(queryClient)
        },
    })
}

interface UpdateOrderStatusInput {
    orderId: string | number
    status: OrderStatus
}

export const useUpdateOrderStatus = () => {
    const getAccessToken = useAccessTokenGetter()
    const queryClient = useQueryClient()

    return useMutation({
        mutationFn: async ({ orderId, status }: UpdateOrderStatusInput) => {
            const accessToken = await getAccessToken()
            return ordersApi.updateOrderStatus(accessToken, Number(orderId), status)
        },
        onSuccess: async () => {
            await invalidateOrdersQueries(queryClient)
        },
    })
}

interface AdvanceOrderStatusInput {
    orderId: string | number
    currentStatus: OrderStatus
}

export const useAdvanceOrderStatus = () => {
    const getAccessToken = useAccessTokenGetter()
    const queryClient = useQueryClient()

    return useMutation({
        mutationFn: async ({ orderId, currentStatus }: AdvanceOrderStatusInput) => {
            const nextStatus = getNextStatus(currentStatus)
            if (!nextStatus) {
                throw new Error("El pedido ya está en su estado final")
            }
            const accessToken = await getAccessToken()
            return ordersApi.updateOrderStatus(accessToken, Number(orderId), nextStatus)
        },
        onSuccess: async () => {
            await invalidateOrdersQueries(queryClient)
        },
    })
}
