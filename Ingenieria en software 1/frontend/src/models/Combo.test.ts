import { describe, expect, test } from "vitest";
import { ComboCreateSchema, ComboUpdateSchema } from "./Combo";

describe("Combo Models", () => {
  describe("ComboCreateSchema", () => {
    test("Validates correct combo creation data", () => {
      // Tests validation of correct combo creation data
      const validCombo = {
        name: "Test Combo",
        description: "This is a test combo description",
        price: 15.99,
        categories: ["Main Course"],
        types: ["Food", "Diet"],
        productsIds: { "1": 2, "2": 1 },
        base64Image: ""
      };

      const result = ComboCreateSchema.safeParse(validCombo);
      expect(result.success).toBe(true);
    });

    test("Validates correct combo creation data without optionals types and categories", () => {
      // Tests validation of correct combo creation data without optionals types and categories
      const validCombo = {
        name: "Test Combo",
        description: "This is a test combo description",
        price: 15.99,
        productsIds: { "1": 2, "2": 1 },
        base64Image: ""
      };

      const result = ComboCreateSchema.safeParse(validCombo);
      expect(result.success).toBe(true);
    });

    test("Fails validation when name is too short", () => {
      // Tests validation failure when name is too short
      const invalidCombo = {
        name: "AB",
        description: "This is a test combo description",
        price: 15.99,
        categories: ["Main Course"],
        types: ["Food"],
        productsIds: { "1": 2 }
      };

      const result = ComboCreateSchema.safeParse(invalidCombo);
      expect(result.success).toBe(false);
    });

    test("Fails validation when name is too long", () => {
      // Tests validation failure when name is too long
      const invalidCombo = {
        name: "a".repeat(101),
        description: "This is a test combo description",
        price: 15.99,
        categories: ["Main Course"],
        types: ["Food"],
        productsIds: { "1": 2 }
      };

      const result = ComboCreateSchema.safeParse(invalidCombo);
      expect(result.success).toBe(false);
    });

    test("Fails validation when description is too short", () => {
      // Tests validation failure when description is too short
      const invalidCombo = {
        name: "Test Combo",
        description: "Short",
        price: 15.99,
        categories: ["Main Course"],
        types: ["Food"],
        productsIds: { "1": 2 }
      };

      const result = ComboCreateSchema.safeParse(invalidCombo);
      expect(result.success).toBe(false);
    });

    test("Fails update when description is too long", () => {
      // Tests validation failure when description is too long
      const invalidCombo = {
        description: "a".repeat(501),
      };

      const result = ComboCreateSchema.safeParse(invalidCombo);
      expect(result.success).toBe(false);
    });

    test("Fails validation when price is negative", () => {
      // Tests validation failure when price is negative
      const invalidCombo = {
        name: "Test Combo",
        description: "This is a test combo description",
        price: -5,
        categories: ["Main Course"],
        types: ["Food"],
        productsIds: { "1": 2 }
      };

      const result = ComboCreateSchema.safeParse(invalidCombo);
      expect(result.success).toBe(false);
    });

    test("Fails validation when no products are provided", () => {
      // Tests validation failure when no products are provided
      const invalidCombo = {
        name: "Test Combo",
        description: "This is a test combo description",
        price: 15.99,
        categories: ["Main Course"],
        types: ["Food"],
        productsIds: {}
      };

      const result = ComboCreateSchema.safeParse(invalidCombo);
      expect(result.success).toBe(false);
    });
  });

  describe("ComboUpdateSchema", () => {
    test("Validates correct combo update data", () => {
      // Tests validation of correct combo update data
      const validCombo = {
        name: "Updated Combo",
        description: "This is an updated combo description",
        price: 20.99,
        categories: ["Main Course", "Vegetarian"],
        types: ["Food"],
        productsIds: { "1": 3, "2": 2, "3": 1 }
      };

      const result = ComboUpdateSchema.safeParse(validCombo);
      expect(result.success).toBe(true);
    });

    test("Validates correct partial combo update data", () => {
      // Tests validation of correct partial combo update data
      const validCombo = {
        productsIds: { "1": 3, "2": 2, "3": 1, "5": 1 }
      };

      const result = ComboUpdateSchema.safeParse(validCombo);
      expect(result.success).toBe(true);
    });

    test("Fails update when description is too short", () => {
      // Tests validation failure when description is too short
      const invalidCombo = {
        description: "Short",
      };

      const result = ComboUpdateSchema.safeParse(invalidCombo);
      expect(result.success).toBe(false);
    });

    test("Fails update when description is too long", () => {
      // Tests validation failure when description is too long
      const invalidCombo = {
        description: "a".repeat(501),
      };

      const result = ComboUpdateSchema.safeParse(invalidCombo);
      expect(result.success).toBe(false);
    });

    test("Fails update when no products are provided", () => {
      // Tests validation failure when no products are provided
      const invalidCombo = {
        productsIds: {}
      };

      const result = ComboUpdateSchema.safeParse(invalidCombo);
      expect(result.success).toBe(false);
    });

    test("Nothing happens when update payload is completely empty", () => {
     // Tests validation when update is payload is completely empty
        const result = ComboUpdateSchema.safeParse({});
        expect(result.success).toBe(true);
    });

  });
});
