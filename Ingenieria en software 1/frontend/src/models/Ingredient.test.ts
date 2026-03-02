import { describe, expect, test } from "vitest";
import { IngredientCreateSchema, IngredientUpdateSchema } from "./Ingredient";

describe("Ingredient Models", () => {
  describe("IngredientCreateSchema", () => {
    test("Validates correct ingredient creation data", () => {
      // Tests validation of correct ingredient creation data
      const validIngredient = {
        name: "Tomato",
        stock: 50,
        base64Image: ""
      };

      const result = IngredientCreateSchema.safeParse(validIngredient);
      expect(result.success).toBe(true);
    });

    test("Fails validation when name is too short", () => {
      // Tests that fails when name is too short
      const invalidIngredient = {
        name: "AB",
        stock: 50,
        base64Image: ""
      };

      const result = IngredientCreateSchema.safeParse(invalidIngredient);
      expect(result.success).toBe(false);
    });

    test("Fails validation when stock is negative", () => {
      // Tests that fails when stock is negative
      const invalidIngredient = {
        name: "Tomato",
        stock: -5,
        base64Image: ""
      };

      const result = IngredientCreateSchema.safeParse(invalidIngredient);
      expect(result.success).toBe(false);
    });

    test("Validates with default base64Image", () => {
      // Tests that validates with default base64 image
      const validIngredient = {
        name: "Tomato",
        stock: 50
      };

      const result = IngredientCreateSchema.safeParse(validIngredient);
      expect(result.success).toBe(true);
    });
  });

  describe("IngredientUpdateSchema", () => {
    test("Validates correct ingredient update data", () => {
      // Tests validation of correct ingredient update data
      const validIngredient = {
        name: "Updated Tomato",
        stock: 75,
        base64Image: "data:image/jpeg;base64,..."
      };

      const result = IngredientUpdateSchema.safeParse(validIngredient);
      expect(result.success).toBe(true);
    });

    test("Fails validation when name is too long", () => {
      // Tests that fails when name is too long
      const invalidIngredient = {
        name: "A".repeat(101), // Too long
        stock: 50,
        base64Image: ""
      };

      const result = IngredientUpdateSchema.safeParse(invalidIngredient);
      expect(result.success).toBe(false);
    });

    test("Validates without base64Image", () => {
      // Tests that validates without base64 image
      const validIngredient = {
        name: "Tomato",
        stock: 50
      };

      const result = IngredientUpdateSchema.safeParse(validIngredient);
      expect(result.success).toBe(true);
    });
  });
});
