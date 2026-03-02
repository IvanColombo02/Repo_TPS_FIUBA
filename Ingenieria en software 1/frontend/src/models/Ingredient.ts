import { z } from 'zod';

// Schema for creating ingredients (according to new backend structure)
export const IngredientCreateSchema = z.object({
  name: z.string()
    .min(3, 'El nombre debe tener al menos 3 caracteres')
    .max(100, 'El nombre no puede exceder 100 caracteres')
    .trim(),
  stock: z.number()
    .min(0, 'El stock debe ser mayor o igual a 0'),

    base64Image: z.string().default(''),
});

export const IngredientUpdateSchema = z.object({
  name: z.string()
    .min(3, 'El nombre debe tener al menos 3 caracteres')
    .max(100, 'El nombre no puede exceder 100 caracteres')
    .trim(),
  stock: z.number()
    .min(0, 'El stock debe ser mayor o igual a 0'),
  base64Image: z.string().optional(),
});

export type IngredientCreateForm = z.infer<typeof IngredientCreateSchema>;
export type IngredientUpdateForm = z.infer<typeof IngredientUpdateSchema>;
