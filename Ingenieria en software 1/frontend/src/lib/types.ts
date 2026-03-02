export type FoodCategory = string
export interface Ingredient {
  id: string
  name: string
  quantity?: number
}

export interface MenuItem {
  id: number | string
  name: string
  description: string
  price: number
  originalPrice?: number
  discount?: number
  category: FoodCategory
  categories?: FoodCategory[] 
  type?: string
  image: string
  available: boolean
  ingredients?: Ingredient[]
  isCombo?: boolean
  comboId?: number
  comboProducts?: Array<{
    id: number
    name: string
    quantity: number
  }>
  preparationTime: number | string
}

export interface CartItem extends MenuItem {
  quantity: number
  customizationId: string
}

export type CarouselAction =
  | { type: "category"; category: FoodCategory }
  | { type: "promotion" }
  | { type: "product"; productId: number }

export interface CarouselSlide {
  id: number
  title: string
  description: string
  image: string
  action?: CarouselAction
}

export type OrderStatus = "PENDING" | "IN_PREPARATION" | "READY" | "DELIVERED" | "CANCELLED"

export type PaymentMethod = "CASH" | "CARD" | "MP"

export interface OrderItem {
  id: string
  componentId: string
  itemName: string
  itemPrice: number
  quantity: number
  subtotal: number
  discount?: number | null
  promotionApplied?: string | null
}

export interface OrderPricingSummary {
  subtotal: number
  total: number
  discountTotal: number
  promotionDescriptions: string[]
}

export interface Order {
  id: string
  userId?: string
  userEmail?: string
  status: OrderStatus
  paymentMethod: PaymentMethod
  totalPrice: number
  subtotal?: number
  discountTotal?: number
  promotionDescriptions?: string[]
  createdAt: string
  updatedAt: string
  items: OrderItem[]
}
