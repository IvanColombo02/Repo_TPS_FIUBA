import { describe, it, expect, vi, beforeEach } from "vitest";
import { createCombo } from "./combos";
import { ComboCreateSchema, ComboCreateForm } from "@/models/Combo";

global.fetch = vi.fn();

/**
 * Tests for combo creation validation to verify that combos cannot be created with empty product IDs.
 * Verifies that both schema validation and API calls reject combo creation when productsIds is empty.
 */
describe("Combo Empty Products Validation", () => {
  const mockAccessToken = "fake-access-token";

  beforeEach(() => {
    vi.clearAllMocks();
  });

  /**
   * Test that ComboCreateSchema rejects combo creation with empty product IDs.
   * Verifies that schema validation fails when productsIds map is empty.
   */
  it("should fail schema validation when productsIds is empty", () => {
    const invalidCombo: ComboCreateForm = {
      name: "Test Combo",
      description: "This is a test combo description with enough characters",
      price: 15.99,
      categories: ["Main Course"],
      types: ["Food"],
      productsIds: {},
      base64Image: "",
    };
    const result = ComboCreateSchema.safeParse(invalidCombo);
    expect(result.success).toBe(false);
    if (!result.success) {
      const error = result.error.errors.find((e) => e.path.includes("productsIds"));
      expect(error).toBeDefined();
      expect(error?.message).toContain("al menos un producto");
    }
  });

  /**
   * Test that createCombo API function rejects combo creation with empty product IDs.
   * Verifies that API call fails when productsIds map is empty.
   */
  it("should reject combo creation with empty product IDs via API", async () => {
    const invalidCombo: ComboCreateForm = {
      name: "Test Combo",
      description: "This is a test combo description with enough characters",
      price: 15.99,
      categories: ["Main Course"],
      types: ["Food"],
      productsIds: {},
      base64Image: "",
    };
    vi.mocked(fetch).mockResolvedValueOnce({
      ok: false,
      status: 400,
      statusText: "Bad Request",
      json: async () => ({ 
        message: "Validation failed: productsIds cannot be empty" 
      }),
    } as Response);
    await expect(createCombo(mockAccessToken, invalidCombo)).rejects.toThrow();
    expect(fetch).toHaveBeenCalledWith(
      expect.stringContaining("/combos"),
      expect.objectContaining({
        method: "POST",
        headers: expect.objectContaining({
          "Authorization": `Bearer ${mockAccessToken}`,
          "Content-Type": "application/json",
        }),
      })
    );
    const fetchCall = vi.mocked(fetch).mock.calls[0];
    const requestBody = JSON.parse(fetchCall[1]?.body as string);
    expect(requestBody.productsIds).toEqual({});
  });

  /**
   * Test that schema validation passes with valid product IDs.
   * Verifies that combo creation is allowed when productsIds contains product IDs.
   */
  it("should pass schema validation when productsIds contains product IDs", () => {
    const validCombo: ComboCreateForm = {
      name: "Test Combo",
      description: "This is a test combo description with enough characters",
      price: 15.99,
      categories: ["Main Course"],
      types: ["Food"],
      productsIds: { "1": 2, "2": 1 },
      base64Image: "",
    };
    const result = ComboCreateSchema.safeParse(validCombo);
    expect(result.success).toBe(true);
  });

  /**
   * Test that createCombo API function succeeds with valid product IDs.
   * Verifies that API call succeeds when productsIds contains product IDs.
   */
  it("should allow combo creation with valid product IDs via API", async () => {
    const validCombo: ComboCreateForm = {
      name: "Test Combo",
      description: "This is a test combo description with enough characters",
      price: 15.99,
      categories: ["Main Course"],
      types: ["Food"],
      productsIds: { "1": 2, "2": 1 },
      base64Image: "",
    };

    const mockComboResponse = {
      id: 1,
      name: "Test Combo",
      description: "This is a test combo description with enough characters",
      price: 15.99,
      stock: 0,
      categories: ["Main Course"],
      types: ["Food"],
      products: {
        "ProductDTO[id=1, name=Product 1, price=10.0]": 2,
        "ProductDTO[id=2, name=Product 2, price=5.0]": 1,
      },
      base64Image: "",
    };
    vi.mocked(fetch).mockResolvedValueOnce({
      ok: true,
      status: 201,
      json: async () => mockComboResponse,
    } as Response);

    const result = await createCombo(mockAccessToken, validCombo);
    expect(result).toBeDefined();
    expect(result.name).toBe("Test Combo");
    expect(result.id).toBe(1);
    const fetchCall = vi.mocked(fetch).mock.calls[0];
    const requestBody = JSON.parse(fetchCall[1]?.body as string);
    expect(requestBody.productsIds).toEqual({ 1: 2, 2: 1 });
    expect(Object.keys(requestBody.productsIds).length).toBeGreaterThan(0);
  });
});
