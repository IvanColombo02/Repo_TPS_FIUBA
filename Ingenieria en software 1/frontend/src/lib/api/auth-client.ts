import { BASE_API_URL } from "@/config/app-query-client";
import { AuthResponseSchema } from "@/models/Login";

export class InvalidCredentialsError extends Error {
    constructor() {
        super("Email o contraseña incorrectos");
        this.name = "InvalidCredentialsError";
    }
}

export class EmailNotValidatedError extends Error {
    constructor() {
        super("Debes validar tu email antes de iniciar sesión. Revisa tu correo electrónico.");
        this.name = "EmailNotValidatedError";
    }
}

export class UserAlreadyExistsError extends Error {
    constructor() {
        super("El email o nombre de usuario ya está registrado");
        this.name = "UserAlreadyExistsError";
    }
}

export async function auth(method: "PUT" | "POST", endpoint: string, data: object) {
    const url = BASE_API_URL + endpoint;

    const response = await fetch(url, {
        method,
        headers: {
            Accept: "application/json",
            "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
    });
    if (response.ok) {
        return AuthResponseSchema.parse(await response.json());
    } else {
        if (response.status === 401) {
            throw new InvalidCredentialsError();
        }
        if (response.status === 409) {
            throw new UserAlreadyExistsError();
        }
        const errorText = await response.text();
        if (errorText === "EMAIL_NOT_VERIFIED" ||
            errorText.toLowerCase().includes("email") &&
            (errorText.toLowerCase().includes("validat") ||
                errorText.toLowerCase().includes("verif") ||
                errorText.toLowerCase().includes("confirm"))) {
            throw new EmailNotValidatedError();
        }
        throw new Error(`Error (${response.status}): ${errorText}`);
    }
}
