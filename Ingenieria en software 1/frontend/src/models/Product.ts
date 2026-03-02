import { z } from 'zod';

// Schema for creating products (according to new backend structure)
export const ProductCreateSchema = z.object({
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
  categories: z.array(z.string())
    .min(1, 'Debe seleccionar al menos una categoría'),
  type: z.string()
    .min(1, 'El tipo es requerido')
    .trim(),
  estimatedTime: z.number()
    .min(1, 'El tiempo estimado debe ser mayor a 0')
    .max(480, 'El tiempo estimado no puede exceder 8 horas'),
  ingredientsIds: z.record(z.string(), z.number())
    .refine((ingredients) => Object.keys(ingredients).length > 0, {
      message: 'Debe seleccionar al menos un ingrediente'
    }),
  base64Image: z.string().default(''),
});

export const ProductUpdateSchema = z.object({
  name: z.string()
    .min(3, 'El nombre debe tener al menos 3 caracteres')
    .max(100, 'El nombre no puede exceder 100 caracteres')
    .trim(),
  description: z.string()
    .min(2, 'La descripción debe tener al menos 10 caracteres')
    .max(500, 'La descripción no puede exceder 500 caracteres')
    .trim(),
  price: z.number()
    .min(1, 'El precio debe ser mayor o igual a 1'),

  categories: z.array(z.string())
    .min(1, 'Debe seleccionar al menos una categoría'),
  type: z.string()
    .min(1, 'El tipo es requerido')
    .trim(),
  estimatedTime: z.number()
    .min(1, 'El tiempo estimado debe ser mayor a 0')
    .max(480, 'El tiempo estimado no puede exceder 8 horas'),
  ingredientsIds: z.record(z.string(), z.number())
    .refine((ingredients) => Object.keys(ingredients).length > 0, {
      message: 'Debe seleccionar al menos un ingrediente'
    }),
  base64Image: z.string().optional(),
});

export const ProductStockSchema = z.object({
  stock: z.number()
    .min(0, 'El stock debe ser mayor o igual a 0'),
});

export type ProductCreateForm = z.infer<typeof ProductCreateSchema>;
export type ProductUpdateForm = z.infer<typeof ProductUpdateSchema>;
export type ProductStockForm = z.infer<typeof ProductStockSchema>;
