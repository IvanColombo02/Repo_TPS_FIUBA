import { z } from "zod";
import { validateEmailDomain } from "@/lib/utils/validation";

export const SignupRequestSchema = z.object({
  username: z.string().min(3, "El nombre de usuario debe tener al menos 3 caracteres"),
  email: z.string().email("Ingresa un email válido").refine(validateEmailDomain, "El email debe ser @fi.uba.ar"),
  password: z.string().min(6, "La contraseña debe tener al menos 6 caracteres"),
  role: z.string(),
  firstName: z.string().min(2, "El nombre debe tener al menos 2 caracteres"),
  lastName: z.string().min(2, "El apellido debe tener al menos 2 caracteres"),
  birthDate: z.string().min(1, "La fecha de nacimiento es requerida").refine((date) => {
    const birthDate = new Date(date);
    const today = new Date();
    const age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    const actualAge = monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate()) ? age - 1 : age;
    return actualAge >= 13 && actualAge <= 120;
  }, "Debes tener entre 13 y 120 años"),
  gender: z.enum(["Masculino", "Femenino", "Prefiero no decir"], {
    errorMap: () => ({ message: "Selecciona una opción válida" })
  }),
  address: z.string().min(3, "La dirección debe tener al menos 3 caracteres"),
  profileImage: z.string().default(""),
});

export type SignupRequest = z.infer<typeof SignupRequestSchema>;

export const LoginRequestSchema = z.object({
  email: z.string().email("Ingresa un email válido"),
  password: z.string().min(1, "La contraseña no puede estar vacía"),
});

export type LoginRequest = z.infer<typeof LoginRequestSchema>;

export const AuthResponseSchema = z.object({
  accessToken: z.string().min(1),
  refreshToken: z.string().min(1),
});

export type AuthResponse = z.infer<typeof AuthResponseSchema>;
