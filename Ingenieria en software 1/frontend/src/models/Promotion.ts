import { z } from "zod"

export const ExpressionSchema = z.object({
  type: z.string(),
  condition: z.any().optional(),
  action: z.any().optional(),
  left: z.any().optional(),
  right: z.any().optional(),
  operator: z.string().optional(),
  value: z.union([z.number(), z.string()]).optional(),
  day: z.string().optional(),
  productId: z.number().optional(),
  category: z.string().optional(),
  productType: z.string().optional(),
  productName: z.string().optional(),
  minQuantity: z.number().optional(),
  targetType: z.enum(["ORDER", "ORDER_ITEM"]).optional(),
  targetFilterType: z.enum(["product", "category", "type"]).optional(),
  targetItemId: z.number().optional(),
  targetCategory: z.string().optional(),
  targetProductType: z.string().optional(),
  amount: z.number().optional(),
  percentage: z.number().optional(),
  buyQuantity: z.number().optional(),
  payQuantity: z.number().optional(),
  quantity: z.number().optional(),
  filterType: z.enum(["category", "type"]).optional(),

  hour: z.string().optional(),
  mail: z.string().optional(),
})

export type Expression = z.infer<typeof ExpressionSchema>

// Tipos para acciones de promoción
export type Action =
  | {
    type: "fixedDiscount"
    targetType: "ORDER" | "ORDER_ITEM"
    amount: number
    targetItemId?: number
    targetFilterType?: "product" | "category" | "type"
    targetCategory?: string
    targetProductType?: string
  }
  | {
    type: "percentageDiscount"
    targetType: "ORDER" | "ORDER_ITEM"
    percentage: number
    targetItemId?: number
    targetFilterType?: "product" | "category" | "type"
    targetCategory?: string
    targetProductType?: string
  }
  | {
    type: "freeProduct"
    targetType: "ORDER"
    productId: number
    quantity: number
  }
  | {
    type: "quantityDiscount"
    targetType: "ORDER_ITEM"
    buyQuantity: number
    payQuantity: number
  }

export const PromotionCreateSchema = z
  .object({
    name: z
      .string()
      .min(3, "El nombre debe tener al menos 3 caracteres")
      .max(100, "El nombre no puede exceder 100 caracteres")
      .trim(),
    description: z
      .string()
      .min(10, "La descripción debe tener al menos 10 caracteres")
      .max(500, "La descripción no puede exceder 500 caracteres")
      .trim(),
    fromDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, "Formato de fecha inválido (YYYY-MM-DD)"),
    toDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, "Formato de fecha inválido (YYYY-MM-DD)"),
    base64Image: z.string().default(""),
    expression: z.string().refine(
      (val) => {
        try {
          const parsed = JSON.parse(val)

          return parsed.condition && parsed.action
        } catch {
          return false
        }
      },
      { message: "La expresión debe ser un JSON válido con condition y action" }
    ),
  })
  .refine(
    (data) => new Date(data.fromDate) <= new Date(data.toDate),
    {
      message: "La fecha de inicio debe ser anterior o igual a la fecha de fin",
      path: ["toDate"],
    }
  )

export const PromotionUpdateSchema = z.object({
  name: z
    .string()
    .min(3, "El nombre debe tener al menos 3 caracteres")
    .max(100, "El nombre no puede exceder 100 caracteres")
    .trim()
    .optional(),
  description: z
    .string()
    .min(10, "La descripción debe tener al menos 10 caracteres")
    .max(500, "La descripción no puede exceder 500 caracteres")
    .trim()
    .optional(),
  fromDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, "Formato de fecha inválido (YYYY-MM-DD)").optional(),
  toDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, "Formato de fecha inválido (YYYY-MM-DD)").optional(),
  base64Image: z.string().optional(),
  expression: z.string().refine(
    (val) => {
      try {
        const parsed = JSON.parse(val)
        ExpressionSchema.parse(parsed)
        return true
      } catch {
        return false
      }
    },
    { message: "La expresión debe ser un JSON válido" }
  ).optional(),
})

export type PromotionCreateForm = z.infer<typeof PromotionCreateSchema>
export type PromotionUpdateForm = z.infer<typeof PromotionUpdateSchema>

