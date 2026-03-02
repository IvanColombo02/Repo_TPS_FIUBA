import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { Login } from "./Login";
import { InvalidCredentialsError, EmailNotValidatedError } from "@/services/UserServices";
import * as Wouter from "wouter";

// Mock wouter to simulate URL search params
vi.mock("wouter", async () => {
  const actual: typeof Wouter = await vi.importActual("wouter");
  return {
    ...actual,
    useSearch: () => "",
    Link: ({ children, href }: { children: React.ReactNode; href: string }) => (
      <a href={href}>{children}</a>
    ),
  };
});

/**
 * Tests for Login component to verify that clear error messages are displayed
 * when login fails due to unvalidated email or incorrect credentials.
 * Verifies that error messages are visible, clear, and properly differentiated.
 */
describe("Login - Error Messages Display", () => {
  const mockOnSubmit = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  /**
   * Test that InvalidCredentialsError displays clear error message.
   * Verifies that error message is visible and clearly indicates invalid credentials.
   */
  it("should display clear error message when login fails due to invalid credentials", () => {
    const invalidCredentialsError = new InvalidCredentialsError();
    render(<Login onSubmit={mockOnSubmit} submitError={invalidCredentialsError} />);

    const errorMessage = screen.getByText("Email o contraseña incorrectos");
    expect(errorMessage).toBeInTheDocument();
    expect(errorMessage).toBeVisible();

    const alert = errorMessage.closest('[role="alert"]');
    expect(alert).toBeInTheDocument();
    expect(alert).toHaveClass(/destructive/);
  });

  /**
   * Test that EmailNotValidatedError displays clear error message.
   * Verifies that error message is visible and clearly indicates email not validated.
   */
  it("should display clear error message when login fails due to unvalidated email", () => {
    const emailNotValidatedError = new EmailNotValidatedError();
    render(<Login onSubmit={mockOnSubmit} submitError={emailNotValidatedError} />);

    const errorMessage = screen.getByText(
      "Debes validar tu email antes de iniciar sesión. Revisa tu correo electrónico."
    );
    expect(errorMessage).toBeInTheDocument();
    expect(errorMessage).toBeVisible();

    const alert = errorMessage.closest('[role="alert"]');
    expect(alert).toBeInTheDocument();
    expect(alert).toHaveClass(/destructive/);
  });

  /**
   * Test that different error types are differentiated correctly.
   * Verifies that InvalidCredentialsError and EmailNotValidatedError show different messages.
   */
  it("should differentiate between InvalidCredentialsError and EmailNotValidatedError", () => {
    const invalidCredentialsError = new InvalidCredentialsError();
    const { rerender } = render(<Login onSubmit={mockOnSubmit} submitError={invalidCredentialsError} />);
    expect(screen.getByText("Email o contraseña incorrectos")).toBeInTheDocument();
    expect(screen.queryByText("Debes validar tu email antes de iniciar sesión. Revisa tu correo electrónico.")).not.toBeInTheDocument();

    const emailNotValidatedError = new EmailNotValidatedError();
    rerender(<Login onSubmit={mockOnSubmit} submitError={emailNotValidatedError} />);
    expect(screen.getByText("Debes validar tu email antes de iniciar sesión. Revisa tu correo electrónico.")).toBeInTheDocument();
    expect(screen.queryByText("Email o contraseña incorrectos")).not.toBeInTheDocument();
  });

  /**
   * Test that error messages are visible and clear.
   * Verifies that error messages are displayed in a visible manner with clear text.
   */
  it("should display error messages in a visible and clear manner", () => {
    const invalidCredentialsError = new InvalidCredentialsError();
    render(<Login onSubmit={mockOnSubmit} submitError={invalidCredentialsError} />);

    const errorMessage = screen.getByText("Email o contraseña incorrectos");
    expect(errorMessage).toBeVisible();
    expect(errorMessage.textContent).toBe("Email o contraseña incorrectos");
    expect(errorMessage.textContent?.length).toBeGreaterThan(0);

    const alertContainer = errorMessage.closest('[role="alert"]');
    expect(alertContainer).toBeInTheDocument();
    expect(alertContainer).toBeVisible();
  });

  /**
   * Test that no error message is displayed when there is no error.
   * Verifies that form fields are still visible when there is no error.
   */
  it("should not display error message when there is no error", () => {
    render(<Login onSubmit={mockOnSubmit} submitError={null} />);
    expect(screen.queryByText("Email o contraseña incorrectos")).not.toBeInTheDocument();
    expect(
      screen.queryByText("Debes validar tu email antes de iniciar sesión. Revisa tu correo electrónico.")
    ).not.toBeInTheDocument();
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/contraseña/i)).toBeInTheDocument();
  });

  /**
   * Test that InvalidCredentialsError message is different from EmailNotValidatedError message.
   * Verifies that backend errors are properly differentiated by message content.
   */
  it("should differentiate backend errors by message content", () => {
    const invalidCredentialsError = new InvalidCredentialsError();
    render(<Login onSubmit={mockOnSubmit} submitError={invalidCredentialsError} />);

    const invalidCredentialsMessage = screen.getByText("Email o contraseña incorrectos");
    expect(invalidCredentialsMessage).toBeInTheDocument();
    expect(invalidCredentialsMessage.textContent).toContain("incorrectos");

    const emailNotValidatedError = new EmailNotValidatedError();
    render(<Login onSubmit={mockOnSubmit} submitError={emailNotValidatedError} />);

    const emailNotValidatedMessage = screen.getByText(
      "Debes validar tu email antes de iniciar sesión. Revisa tu correo electrónico."
    );
    expect(emailNotValidatedMessage).toBeInTheDocument();
    expect(emailNotValidatedMessage.textContent).toContain("validar");
    expect(emailNotValidatedMessage.textContent).not.toContain("incorrectos");
  });
});
