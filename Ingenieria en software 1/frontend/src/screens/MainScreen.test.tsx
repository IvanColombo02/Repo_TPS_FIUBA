import { render, screen, waitFor, within } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import { MainScreen } from "@/screens/MainScreen";
import * as productsApi from "@/lib/api/products";
import * as combosApi from "@/lib/api/combos";
import * as userServices from "@/services/UserServices";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";

vi.mock("@/hooks/use-orders", () => ({ useCreateOrder: () => ({ mutateAsync: vi.fn() }) }));
vi.mock("@/hooks/use-toast", () => ({ useToast: () => ({ toast: vi.fn() }) }));

vi.spyOn(userServices, "fetchUserProfile").mockResolvedValue({
  username: "testUser",
  role: "ROLE_USER",
  email: "test@example.com",
  firstName: "Test",
  lastName: "User",
  age: 30,
  gender: "Masculino",
  address: "Calle Falsa 123",
  base64Image: "",
});

vi.mock("@/services/TokenContext", () => ({
  useToken: () => [
    { state: "LOGGED_IN", tokens: { accessToken: "fake-token" } },
    vi.fn(),
  ],
  useAccessTokenGetter: () => () => "fake-token",
}));


const renderWithClient = (ui: React.ReactElement) => {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>{ui}</QueryClientProvider>
  );
};

describe("MainScreen - stock and combos display", () => {
  it("enables products with stock and disables combos without stock", async () => {

    vi.spyOn(productsApi, "fetchProductsPage").mockResolvedValue({
      content: [
        {
          id: 1,
          name: "Pizza Margarita",
          description: "Pizza clásica con tomate y muzzarella",
          price: 1500,
          stock: 5,
          categories: ["Comida"],
          type: "Producto",
          estimatedTime: 10,
          ingredients: {},
          base64Image: "/pizza.jpg",
        },
      ],
      totalPages: 1,
      totalElements: 1,
      size: 6,
      number: 0,
      first: true,
      last: true,
      empty: false,
    });

    vi.spyOn(combosApi, "fetchCombosPage").mockResolvedValue({
      content: [
        {
          id: 2,
          name: "Empanadas Combo",
          description: "Combo de 6 empanadas mixtas",
          price: 2000,
          stock: 0,
          categories: ["Combo"],
          types: ["Combo"],
          base64Image: "/combo.jpg",
          products: [],
        },
      ],
      totalPages: 1,
      totalElements: 1,
      size: 6,
      number: 0,
      first: true,
      last: true,
      empty: false,
    });

    vi.spyOn(productsApi, "fetchProductsSimplePage").mockResolvedValue({
      content: [
        {
          id: 1,
          name: "Pizza Margarita",
          description: "Pizza clásica con tomate y muzzarella",
          price: 1500,
          stock: 5,
          categories: ["Comida"],
          type: "Producto",
          estimatedTime: 10,
          base64Image: "/pizza.jpg",
        },
      ],
      totalPages: 1,
      totalElements: 1,
      size: 6,
      number: 0,
      first: true,
      last: true,
      empty: false,
    });

    vi.spyOn(combosApi, "fetchCombosSimplePage").mockResolvedValue({
      content: [
        {
          id: 2,
          name: "Empanadas Combo",
          description: "Combo de 6 empanadas mixtas",
          price: 2000,
          stock: 0,
          categories: ["Combo"],
          types: ["Combo"],
          base64Image: "/combo.jpg",
        },
      ],
      totalPages: 1,
      totalElements: 1,
      size: 6,
      number: 0,
      first: true,
      last: true,
      empty: false,
    });

    renderWithClient(<MainScreen />);

    await waitFor(async () => {

      expect(await screen.findByText("Pizza Margarita")).toBeInTheDocument();
      expect(await screen.findByText("Empanadas Combo")).toBeInTheDocument();
    });

    const addButtons = await screen.findAllByText(/añadir/i, { selector: "button" });
    expect(addButtons.length).toBeGreaterThanOrEqual(2);

    const [pizzaAddBtn, comboAddBtn] = addButtons;

    expect(pizzaAddBtn).not.toBeDisabled();

    expect(comboAddBtn).toBeDisabled();

    expect(screen.getByText(/no disponible/i)).toBeInTheDocument();

    const comboImg = screen.getByRole("img", { name: /Empanadas Combo/i });
    expect(comboImg.className).toMatch(/grayscale|opacity-60/);
  });

  it("shows product ingredients in details when clicking Ver detalles", async () => {
    // Setup mocks for detail fetch
    vi.spyOn(productsApi, "fetchProductById").mockResolvedValue({
      id: 1,
      name: "Pizza Margarita",
      description: "Pizza clásica con tomate y muzzarella",
      price: 1500,
      stock: 5,
      categories: ["Comida"],
      type: "Producto",
      estimatedTime: 10,
      // Ingredients as array
      ingredients: [
        { id: "1", name: "Mozzarella", quantity: 1 },
        { id: "2", name: "Tomate", quantity: 2 },
      ] as unknown as Record<string, number>,
      base64Image: "/pizza.jpg",
    });

    renderWithClient(<MainScreen />);

    await waitFor(async () => {
      expect(await screen.findByText("Pizza Margarita")).toBeInTheDocument();
    });

    const detailButtons = await screen.findAllByText(/ver detalles/i, { selector: 'button' });
    expect(detailButtons.length).toBeGreaterThan(0);
    await detailButtons[0].click();

    const dialog = await screen.findByRole("dialog")
    const dialogWithin = within(dialog)
    const ingredientsHeader = dialogWithin.getByRole("heading", { name: /ingredientes/i })
    // find the list container after the header, which is a sibling separated by a separator
    await waitFor(() => {
      const ingredientsContainer = (ingredientsHeader.parentElement as HTMLElement)?.querySelector(".space-y-2") as HTMLElement
      expect(ingredientsContainer).toBeTruthy()
      const ingredientsWithin = within(ingredientsContainer)
      expect(ingredientsWithin.getByText(/Mozzarella/i)).toBeInTheDocument();
      expect(ingredientsWithin.getByText(/Tomate/i)).toBeInTheDocument();
    });
  });

  it("shows combo products in details when clicking Ver detalles for combos", async () => {
    // Ensure the simple page shows the combo as available so the details button is enabled
    vi.spyOn(productsApi, "fetchProductsSimplePage").mockResolvedValue({
      content: [
        {
          id: 1,
          name: "Pizza Margarita",
          description: "Pizza clásica con tomate y muzzarella",
          price: 1500,
          stock: 5,
          categories: ["Comida"],
          type: "Producto",
          estimatedTime: 10,
          base64Image: "/pizza.jpg",
        },
      ],
      totalPages: 1,
      totalElements: 1,
      size: 6,
      number: 0,
      first: true,
      last: true,
      empty: false,
    });

    vi.spyOn(combosApi, "fetchCombosSimplePage").mockResolvedValue({
      content: [
        {
          id: 2,
          name: "Empanadas Combo",
          description: "Combo de 6 empanadas mixtas",
          price: 2000,
          stock: 10,
          categories: ["Combo"],
          types: ["Combo"],
          base64Image: "/combo.jpg",
        },
      ],
      totalPages: 1,
      totalElements: 1,
      size: 6,
      number: 0,
      first: true,
      last: true,
      empty: false,
    });
    vi.spyOn(combosApi, "fetchComboById").mockResolvedValue({
      id: 2,
      name: "Empanadas Combo",
      description: "Combo de 6 empanadas mixtas",
      price: 2000,
      stock: 0,
      categories: ["Combo"],
      types: ["Combo"],
      base64Image: "/combo.jpg",
      products: [
        { id: 1, name: "Empanada de carne", quantity: 3 },
        { id: 3, name: "Empanada de jamón", quantity: 3 },
      ],
    })

    renderWithClient(<MainScreen />)

    await waitFor(async () => {
      expect(await screen.findByText("Empanadas Combo")).toBeInTheDocument();
    });

    const detailButtons = await screen.findAllByText(/ver detalles/i, { selector: 'button' });
    // Click the second 'Ver detalles' button to open the combo's details
    await detailButtons[1].click();

    const dialog = await screen.findByRole("dialog");
    const dialogWithin = within(dialog);

    const productsHeader = dialogWithin.getByText(/productos incluidos/i);

    await waitFor(() => {
      const productsContainer = (productsHeader.parentElement as HTMLElement)?.querySelector(".space-y-2") as HTMLElement
      expect(productsContainer).toBeTruthy()
      const productsWithin = within(productsContainer)
      expect(productsWithin.getByText(/Empanada de carne/i)).toBeInTheDocument();
      expect(productsWithin.getByText(/Empanada de jamón/i)).toBeInTheDocument();
    })
  })
});