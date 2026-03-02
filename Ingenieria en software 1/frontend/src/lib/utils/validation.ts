// Reusable validation utilities

export function validateEmailDomain(email: string, domain: string = "@fi.uba.ar"): boolean {
  return email.toLowerCase().endsWith(domain.toLowerCase());
}

export function validateMinLength(value: string, minLength: number): boolean {
  return !!(value && value.trim().length >= minLength);
}