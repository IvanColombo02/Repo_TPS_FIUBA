import type { CartItem, MenuItem } from "@/lib/types"
import type { PromotionDTO } from "@/lib/api/promotions"
import type { Expression } from "@/models/Promotion"


const DAY_NAMES_ES: Record<string, string> = {
  MONDAY: "Lunes",
  TUESDAY: "Martes",
  WEDNESDAY: "Miércoles",
  THURSDAY: "Jueves",
  FRIDAY: "Viernes",
  SATURDAY: "Sábado",
  SUNDAY: "Domingo",
}

const DAYS_OF_WEEK = ["SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"]

const OPERATOR_TEXT: Record<string, string> = {
  ">": "más de",
  ">=": "al menos",
  "<": "menos de",
  "<=": "hasta",
  "==": "exactamente",
}

const TIME_OPERATOR_TEXT: Record<string, string> = {
  ">": "después de las",
  "<": "antes de las",
  "=": "a las",
}

const OPERATOR_EVALUATORS: Record<string, (a: number, b: number) => boolean> = {
  ">": (a, b) => a > b,
  ">=": (a, b) => a >= b,
  "<": (a, b) => a < b,
  "<=": (a, b) => a <= b,
  "==": (a, b) => a === b,
}

const normalize = (str: string | undefined | null): string =>
  String(str || "").trim().toLowerCase()

const toNumber = (value: unknown): number =>
  typeof value === "number" ? value : parseFloat(String(value)) || 0

const getItemId = (item: CartItem | MenuItem): number =>
  typeof item.id === "string" ? parseInt(item.id.replace("combo-", "")) : item.id

const getItemCategories = (item: CartItem | MenuItem): string[] =>
  item.categories?.length ? item.categories : [item.category]

const isFreeItem = (item: CartItem): boolean =>
  item.customizationId.startsWith("free-")

const getNonFreeCartItems = (cart: CartItem[]): CartItem[] =>
  cart.filter(item => !isFreeItem(item))

const itemMatchesProductId = (item: CartItem | MenuItem, productId: number | undefined): boolean => {
  if (!productId) return false
  const itemId = getItemId(item)
  return itemId === productId || item.id === productId
}

const itemMatchesCategory = (item: CartItem | MenuItem, category: string | undefined): boolean => {
  if (!category) return false
  const normalizedCategory = normalize(category)
  return getItemCategories(item).some(cat => normalize(cat) === normalizedCategory)
}

const itemMatchesType = (item: CartItem | MenuItem, type: string | undefined): boolean =>
  !!type && normalize(item.type) === normalize(type)

const parsePromoExpression = (promo: PromotionDTO): { condition: Expression; action: Record<string, unknown> } | null => {
  try {
    const parsed = JSON.parse(promo.expression)
    return { condition: parsed.condition, action: parsed.action }
  } catch {
    return null
  }
}

type ConditionDescriptionHandler = (condition: Expression) => string

const createLogicalHandler = (connector: string): ConditionDescriptionHandler =>
  (condition) => {
    const left = getConditionDescription(condition.left)
    const right = getConditionDescription(condition.right)
    return left && right ? `${left} ${connector} ${right}` : left || right || ""
  }

const conditionDescriptionHandlers: Record<string, ConditionDescriptionHandler> = {
  totalAmount: (c) => `Compras por ${OPERATOR_TEXT[c.operator || ">"] || "exactamente"} $${toNumber(c.value)}`,
  dayOfWeek: (c) => `Válido los ${DAY_NAMES_ES[c.day || ""] || c.day}`,
  time: (c) => `${TIME_OPERATOR_TEXT[c.operator || ">"] || "a las"} ${(c.hour || "12:00:00").substring(0, 5)} hs`,
  mailContains: (c) => `Email contiene "${c.mail || ""}"`,
  productInCart: () => "Producto específico en el carrito",
  productType: (c) => {
    if (c.filterType === "type" && c.productType) return `Productos de tipo "${c.productType}"`
    if (c.category) return `Productos de categoría "${c.category}"`
    return "Tipo/categoría de producto"
  },
  quantity: (c) => `Mínimo ${c.minQuantity || 0} unidades`,
  and: createLogicalHandler("y"),
  or: createLogicalHandler("o"),
}

export function getConditionDescription(condition: Expression | null | undefined): string {
  if (!condition) return ""
  return conditionDescriptionHandlers[condition.type]?.(condition) ?? ""
}

export function getPromotionConditionDescription(promo: PromotionDTO): string | null {
  const parsed = parsePromoExpression(promo)
  return parsed ? getConditionDescription(parsed.condition) || null : null
}
type EvaluationContext = {
  cart: CartItem[]
  cartSubtotal: number
  userEmail?: string
}

type ConditionEvaluator = (condition: Expression, ctx: EvaluationContext) => boolean

const createLogicalEvaluator = (combiner: (a: boolean, b: boolean) => boolean): ConditionEvaluator =>
  (condition, ctx) =>
    combiner(
      evaluateConditionInternal(condition.left, ctx),
      evaluateConditionInternal(condition.right, ctx)
    )

const timeToMinutes = (timeStr: string): number => {
  const [hours, minutes] = timeStr.split(":").map(Number)
  return hours * 60 + (minutes || 0)
}

const conditionEvaluators: Record<string, ConditionEvaluator> = {
  totalAmount: (c, ctx) =>
    OPERATOR_EVALUATORS[c.operator || ">"]?.(ctx.cartSubtotal, toNumber(c.value)) ?? false,

  dayOfWeek: (c) =>
    DAYS_OF_WEEK[new Date().getDay()] === c.day,

  time: (c) => {
    const now = new Date()
    const currentMinutes = now.getHours() * 60 + now.getMinutes()
    const conditionMinutes = timeToMinutes(c.hour || "12:00:00")
    const operator = c.operator || ">"

    if (operator === ">") return currentMinutes > conditionMinutes
    if (operator === "<") return currentMinutes < conditionMinutes
    if (operator === "=") return currentMinutes === conditionMinutes
    return false
  },

  mailContains: (c, ctx) => {
    // Si no tenemos email del usuario, no podemos evaluar -> false
    if (!ctx.userEmail) return false
    const pattern = c.mail || ""
    return ctx.userEmail.includes(pattern)
  },

  productInCart: (c, ctx) =>
    getNonFreeCartItems(ctx.cart).some(item => itemMatchesProductId(item, c.productId)),

  productType: (c, ctx) => {
    const items = getNonFreeCartItems(ctx.cart)
    if (c.filterType === "type") return items.some(item => itemMatchesType(item, c.productType))
    return items.some(item => itemMatchesCategory(item, c.category))
  },

  quantity: (c, ctx) => {
    const cartItem = getNonFreeCartItems(ctx.cart).find(item => itemMatchesProductId(item, c.productId))
    return cartItem ? cartItem.quantity >= (c.minQuantity || 0) : false
  },

  and: createLogicalEvaluator((a, b) => a && b),
  or: createLogicalEvaluator((a, b) => a || b),
}

function evaluateConditionInternal(condition: Expression | null | undefined, ctx: EvaluationContext): boolean {
  if (!condition) return false
  return conditionEvaluators[condition.type]?.(condition, ctx) ?? false
}

export function evaluateCondition(
  condition: Expression | null | undefined,
  cart: CartItem[],
  cartSubtotal: number,
  userEmail?: string
): boolean {
  return evaluateConditionInternal(condition, { cart, cartSubtotal, userEmail })
}


type PromotionMatcher = (item: MenuItem, condition: Expression | null, action: Record<string, unknown> | null) => boolean

const promotionMatchers: PromotionMatcher[] = [

  (item, condition) =>
    itemMatchesProductId(item, condition?.productId as number),

  (item, condition) => {
    if (condition?.type !== "productType") return false
    if (condition.filterType === "type") return itemMatchesType(item, condition.productType as string)
    return itemMatchesCategory(item, condition.category as string)
  },


  (item, _, action) =>
    action?.targetType === "ORDER_ITEM" &&
    action?.targetFilterType === "product" &&
    itemMatchesProductId(item, action.targetItemId as number),

  (item, _, action) =>
    action?.targetType === "ORDER_ITEM" &&
    action?.targetFilterType === "category" &&
    itemMatchesCategory(item, action.targetCategory as string),

  (item, _, action) =>
    action?.targetType === "ORDER_ITEM" &&
    action?.targetFilterType === "type" &&
    itemMatchesType(item, action.targetProductType as string),
]

const conditionMatchesItem = (condition: Expression | null | undefined, item: MenuItem): boolean => {
  if (!condition) return false

  if (condition.type === "and" || condition.type === "or") {
    return conditionMatchesItem(condition.left, item) || conditionMatchesItem(condition.right, item)
  }

  if (condition.type === "productType") {
    if (condition.filterType === "type") return itemMatchesType(item, condition.productType as string)
    return itemMatchesCategory(item, condition.category as string)
  }

  if (condition.type === "productInCart" || condition.type === "quantity") {
    return itemMatchesProductId(item, condition.productId as number)
  }

  return false
}

const itemMatchesPromotion = (item: MenuItem, promo: PromotionDTO): boolean => {
  const parsed = parsePromoExpression(promo)
  if (!parsed) return false

  const itemId = getItemId(item)
  if (isNaN(itemId)) return false

  if (conditionMatchesItem(parsed.condition, item)) return true

  return promotionMatchers.some(matcher =>
    matcher(item, parsed.condition, parsed.action)
  )
}

export function getItemPromotions(item: MenuItem, activePromotions: PromotionDTO[]): PromotionDTO[] {
  if (!activePromotions.length) return []

  const itemId = getItemId(item)
  if (isNaN(itemId)) return []

  return activePromotions.filter(promo => itemMatchesPromotion(item, promo))
}

export function getRelevantPromotions(item: MenuItem, activePromotions: PromotionDTO[], userEmail?: string): PromotionDTO[] {
  if (!activePromotions.length) return []

  const itemPromotions = getItemPromotions(item, activePromotions)

  const simulatedCart: CartItem[] = [{
    ...item,
    quantity: 1,
    customizationId: `${item.id}-default`,
  }]

  const orderPromotions = activePromotions.filter(promo => {
    const parsed = parsePromoExpression(promo)
    if (!parsed || parsed.action?.targetType !== "ORDER") return false
    return evaluateCondition(parsed.condition, simulatedCart, item.price, userEmail)
  })


  const seen = new Set<number>()
  return [...itemPromotions, ...orderPromotions].filter(promo => {
    if (seen.has(promo.id)) return false
    seen.add(promo.id)
    return true
  })
}

export function itemHasRelevantPromotion(item: MenuItem, activePromotions: PromotionDTO[], userEmail?: string): boolean {
  return activePromotions.length > 0 && getRelevantPromotions(item, activePromotions, userEmail).length > 0
}

export function itemHasPromotion(item: MenuItem, activePromotions: PromotionDTO[]): boolean {
  return activePromotions.length > 0 && activePromotions.some(promo => itemMatchesPromotion(item, promo))
}


type DiscountCalculator = (originalPrice: number, action: Record<string, unknown>) => number

const discountCalculators: Record<string, DiscountCalculator> = {
  percentageDiscount: (price, action) =>
    action.percentage ? (price * (action.percentage as number)) / 100 : 0,

  fixedDiscount: (_, action) =>
    (action.amount as number) || 0,
}

export function calculateItemPrice(item: MenuItem, activePromotions: PromotionDTO[]): {
  originalPrice: number
  finalPrice: number
  discount: number
} {
  const originalPrice = item.price
  let totalDiscount = 0

  getItemPromotions(item, activePromotions).forEach(promo => {
    const parsed = parsePromoExpression(promo)
    if (!parsed || parsed.action?.targetType !== "ORDER_ITEM") return

    const actionType = parsed.action.type as string
    const calculator = discountCalculators[actionType]
    if (calculator) {
      totalDiscount += calculator(originalPrice, parsed.action)
    }
  })

  const finalPrice = Math.max(0, originalPrice - totalDiscount)
  const discountPercentage = originalPrice > 0 ? (totalDiscount / originalPrice) * 100 : 0

  return {
    originalPrice,
    finalPrice: Math.round(finalPrice * 100) / 100,
    discount: Math.round(discountPercentage * 100) / 100,
  }
}
