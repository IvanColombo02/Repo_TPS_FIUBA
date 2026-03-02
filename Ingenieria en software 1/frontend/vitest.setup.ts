/// <reference lib="dom" />

import * as matchers from "@testing-library/jest-dom/matchers";
import "@testing-library/jest-dom/vitest";
import { cleanup } from "@testing-library/react";
import { afterEach, expect } from "vitest";

expect.extend(matchers);

afterEach(() => {
  cleanup();
});

// 🧩 Agregar esto para evitar "Cannot read properties of undefined (reading 'baseApiUrl')"
Object.defineProperty(window, "_env_", {
  value: { baseApiUrl: "http://localhost:8080" },
  writable: true,
});