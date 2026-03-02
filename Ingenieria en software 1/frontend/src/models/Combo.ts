import { z } from 'zod';

export const ComboCreateSchema = z.object({
  name: z.string()
    .min(3, 'El nombre debe tener al menos 3 caracteres')
    .max(100, 'El nombre no puede exceder 100 caracteres')
    .trim(),
  description: z.string()
    .min(10, 'La descripción debe tener al menos 10 caracteres')
    .max(500, 'La descripción no puede exceder 500 caracteres')
    .trim(),
  price: z.number()
    .min(1, 'El precio debe ser mayor o igual a 1'),
  categories: z.array(z.string()).default([]),
  types: z.array(z.string()).default([]),
  productsIds: z.record(z.string(), z.number())
    .refine((products) => Object.keys(products).length > 0, {
      message: 'Debe seleccionar al menos un producto'
    }),
  base64Image: z.string().default(''),
});


export const ComboUpdateSchema = z.object({
  name: z.string()
    .min(3, 'El nombre debe tener al menos 3 caracteres')
    .max(100, 'El nombre no puede exceder 100 caracteres')
    .trim()
    .optional(),
  description: z.string()
    .min(10, 'La descripción debe tener al menos 10 caracteres')
    .max(500, 'La descripción no puede exceder 500 caracteres')
    .trim()
    .optional(),
  price: z.number()
    .min(1, 'El precio debe ser mayor o igual a 1')
    .optional(),

  // Allow updating categories/types (optional)
  categories: z.array(z.string()).optional(),
  types: z.array(z.string()).optional(),
  productsIds: z.record(z.string(), z.number())
    .optional()
    .refine((products) => !products || Object.keys(products).length > 0, {
      message: 'Debe seleccionar al menos un producto'
    }),
  base64Image: z.string().optional(),
  addProducts: z.record(z.string(), z.number()).optional(),
  deleteProducts: z.array(z.string()).optional(),
});

export const ComboStockSchema = z.object({
  stock: z.number(),
});

export type ComboCreateForm = z.infer<typeof ComboCreateSchema>;
export type ComboUpdateForm = z.infer<typeof ComboUpdateSchema>;
export type ComboStockForm = z.infer<typeof ComboStockSchema>;


export interface Combo {
  id: number;
  name: string;
  description: string;
  price: number;
  stock: number;
  categories?: string[];
  types?: string[];
  base64Image?: string;
  products: Array<{
    id: number;
    name: string;
    quantity: number;
  }>;
}
