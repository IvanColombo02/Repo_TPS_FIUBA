import { render, screen } from "@testing-library/react";
import { describe, expect, test } from "vitest";
import { ErrorMessage } from "./error-message";

describe("ErrorMessage", () => {
  test("Renders error message when error string is provided", () => {
    // Tests that renders error message when a string is provided
    render(<ErrorMessage error="This is an error message" />);
    
    expect(screen.getByText("This is an error message")).toBeInTheDocument();
  });

  test("Renders error message when Error object is provided", () => {
    // Tests that renders error message when an Error object is provided
    const error = new Error("This is an error from Error object");
    render(<ErrorMessage error={error} />);
    
    expect(screen.getByText("This is an error from Error object")).toBeInTheDocument();
  });

  test("Does not render anything when error is null", () => {
    // Tests that does not render anything when error is null
    const { container } = render(<ErrorMessage error={null} />);
    
    expect(container.firstChild).toBeNull();
  });

  test("Shows default message when Error object has no message", () => {
    // Tests that shows default message when Error object has no message
    const error = new Error();
    error.message = "";
    render(<ErrorMessage error={error} />);
    
    expect(screen.getByText("Error de validación")).toBeInTheDocument();
  });

  test("Renders with error icon", () => {
    // Tests that renders with error icon
    render(<ErrorMessage error="Test error" />);
    
    // Check that the error icon is present (AlertTriangle from lucide-react)
    const icon = document.querySelector("svg");
    expect(icon).toBeInTheDocument();
    expect(icon).toHaveClass("lucide", "lucide-triangle-alert");
  });

  test("Applies correct styling classes", () => {
    // Tests that applies correct styling classes
    render(<ErrorMessage error="Test error" />);
    
    const errorContainer = screen.getByText("Test error").parentElement;
    expect(errorContainer).toHaveClass("flex", "items-start", "gap-1.5", "text-destructive");
  });
});
