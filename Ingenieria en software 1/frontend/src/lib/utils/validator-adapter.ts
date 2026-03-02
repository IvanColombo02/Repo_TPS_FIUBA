import { ZodSchema } from 'zod';

/**
 * Adapter to use Zod schemas with @tanstack/react-form
 * Provides proper typing without needing `any` or `@ts-ignore`
 */
export function createZodValidator(schema: ZodSchema) {
  return (value: unknown) => {
    const result = schema.safeParse(value);
    if (result.success) {
      return undefined;
    }
    return result.error.errors;
  };
}
