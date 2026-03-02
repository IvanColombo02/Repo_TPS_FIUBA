import { describe, expect, test } from "vitest";
import { ProductCreateSchema, ProductUpdateSchema } from "./Product";

describe("Product Models", () => {
  describe("ProductCreateSchema", () => {
    test("Validates correct product creation data", () => {
      // Tests validation of correct product creation data
      const validProduct = {
        name: "Test Product",
        description: "This is a test product description",
        price: 15.99,
        categories: ["Main Course"],
        type: "Food",
        estimatedTime: 30,
        brandId: 1,
        ingredientsIds: { "1": 2, "2": 1 },
        base64Image: ""
      };

      const result = ProductCreateSchema.safeParse(validProduct);
      expect(result.success).toBe(true);
    });

    test("Fails validation when name is too short", () => {
      // Tests validation failure when name is too short
      const invalidProduct = {
        name: "AB",
        description: "This is a test product description",
        price: 15.99,
        categories: ["Main Course"],
        type: "Food",
        estimatedTime: 30,
        brandId: 1,
        ingredientsIds: { "1": 2 }
      };

      const result = ProductCreateSchema.safeParse(invalidProduct);
      expect(result.success).toBe(false);
    });

    test("Fails validation when description is too short", () => {
      // Tests validation failure when description is too short
      const invalidProduct = {
        name: "Test Product",
        description: "Short",
        price: 15.99,
        categories: ["Main Course"],
        type: "Food",
        estimatedTime: 30,
        brandId: 1,
        ingredientsIds: { "1": 2 }
      };

      const result = ProductCreateSchema.safeParse(invalidProduct);
      expect(result.success).toBe(false);
    });

    test("Fails validation when price is negative", () => {
      // Tests validation failure when price is negative
      const invalidProduct = {
        name: "Test Product",
        description: "This is a test product description",
        price: -5,
        categories: ["Main Course"],
        type: "Food",
        estimatedTime: 30,
        brandId: 1,
        ingredientsIds: { "1": 2 }
      };

      const result = ProductCreateSchema.safeParse(invalidProduct);
      expect(result.success).toBe(false);
    });

    test("Fails validation when no categories are provided", () => {
      // Tests validation failure when no categories are provided
      const invalidProduct = {
        name: "Test Product",
        description: "This is a test product description",
        price: 15.99,
        categories: [],
        type: "Food",
        estimatedTime: 30,
        brandId: 1,
        ingredientsIds: { "1": 2 }
      };

      const result = ProductCreateSchema.safeParse(invalidProduct);
      expect(result.success).toBe(false);
    });

    test("Fails validation when no ingredients are provided", () => {
      // Tests validation failure when no ingredients are provided
      const invalidProduct = {
        name: "Test Product",
        description: "This is a test product description",
        price: 15.99,
        categories: ["Main Course"],
        type: "Food",
        estimatedTime: 30,
        brandId: 1,
        ingredientsIds: {}
      };

      const result = ProductCreateSchema.safeParse(invalidProduct);
      expect(result.success).toBe(false);
    });
  });

  describe("ProductUpdateSchema", () => {
    test("Validates correct product update data", () => {
      // Tests validation of correct product update data
      const validProduct = {
        name: "Updated Product",
        description: "This is an updated product description",
        price: 20.99,
        categories: ["Main Course", "Vegetarian"],
        type: "Food",
        estimatedTime: 45,
        brandId: 2,
        ingredientsIds: { "1": 3, "2": 2, "3": 1 }
      };

      const result = ProductUpdateSchema.safeParse(validProduct);
      expect(result.success).toBe(true);
    });

    test("Fails validation when estimated time is too high", () => {
      // Tests validation failure when estimated time is too high
      const invalidProduct = {
        name: "Test Product",
        description: "This is a test product description",
        price: 15.99,
        categories: ["Main Course"],
        type: "Food",
        estimatedTime: 1000, // Too high
        brandId: 1,
        ingredientsIds: { "1": 2 }
      };

      const result = ProductUpdateSchema.safeParse(invalidProduct);
      expect(result.success).toBe(false);
    });
  });
});
