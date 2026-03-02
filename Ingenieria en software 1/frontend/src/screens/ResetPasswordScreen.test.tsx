import { describe, it, vi, expect, beforeEach } from "vitest";
import { render, screen, fireEvent, act } from "@testing-library/react";
import { ResetPasswordScreen } from "./ResetPasswordScreen";
import * as UserServices from "@/services/UserServices";
import * as Wouter from "wouter";

// Mocks
const mockSetLocation = vi.fn();

vi.mock("wouter", async () => {
    // wouter gives us hooks to read the URL and change it. Here, we simulate the URL and capture redirections.
    // vi.mock() intercepts imports of wouter so the test doesn’t depend on the real browser URL.
  const actual: typeof Wouter = await vi.importActual("wouter"); 
  return {
    ...actual,
    useSearch: () => "token=12345", // returns a simulated token
    useLocation: () => ["/reset", mockSetLocation], // is mocked and returns mockSetLocation so we can verify redirects
  };
});


beforeEach(() => {
  vi.clearAllMocks();
  vi.useFakeTimers();
});

describe("ResetPasswordScreen redirección", () => {
  it("redirige al login con el mensaje correcto al cambiar la contraseña", async () => {
    
    const resetPasswordSpy = vi
    // spyOn lets you intercept and inspect service calls inside your component.
    // It lets you "spy" on a function so you can replace its implementation for the test,
    // track if it was called, with which arguments, and how many times. Here it spies on UserServices.resetPassword function.
    // and mocks its return value to resolve to undefined (like a successful password reset).

      .spyOn(UserServices, "resetPassword")
      .mockResolvedValueOnce(undefined);    

    render(<ResetPasswordScreen />);

    // Fills the fields
    fireEvent.change(screen.getByLabelText(/nueva contraseña/i), {
      target: { value: "password123" },
    });
    fireEvent.change(screen.getByLabelText(/confirmar contraseña/i), {
      target: { value: "password123" },
    });

    // The form is submitted
    await act(async () => {
      fireEvent.submit(
        screen.getByRole("button", { name: /cambiar contraseña/i })
      );
    });

    // Checks success message
    expect(screen.getByText(/contraseña actualizada/i)).toBeInTheDocument();

    // Simulates the 2 second setTimeout
    await act(async () => {
      vi.advanceTimersByTime(2000);
    });

    // Verifies redirection successful
    expect(mockSetLocation).toHaveBeenCalledWith(
      "/login?message=password_reset"
    );

    // erifies that resetPassword was called with the correct parameters
    expect(resetPasswordSpy).toHaveBeenCalledWith("12345", "password123");
  });
});