import { useEffect, useState } from "react";
import { requestPasswordReset } from "@/services/UserServices";
import { Button } from "@/components/ui/button";
import { AuthContainer } from "@/components/Auth/AuthContainer";
import { useLocation } from "wouter";
import { ValidatedInput } from "@/components/form/ValidatedInput";

export const ForgotPasswordScreen = () => {
  const [email, setEmail] = useState("");
  const [status, setStatus] = useState<"idle" | "loading" | "success" | "error">("idle");
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [, setLocation] = useLocation();
  const [touched, setTouched] = useState(false);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setStatus("loading");
    setErrorMsg(null);
    try {
      await requestPasswordReset(email.trim());
      setStatus("success");
    } catch (err: unknown) {
      setStatus("error");
      setErrorMsg((err as Error)?.message || "Error inesperado");
    }
  };

  useEffect(() => {
    if (status === "success") {
      const t = setTimeout(() => setLocation("/login?message=reset_email_sent"), 2000);
      return () => clearTimeout(t);
    }
  }, [status, setLocation]);

  return (
    <AuthContainer title="Recuperar contraseña">
      {status === "success" ? (
        <div className="space-y-4 text-center">
          <p className="text-foreground">Te enviamos un correo con instrucciones para cambiar tu contraseña.</p>
          <Button variant="ghost" size="sm" onClick={() => setLocation("/login")}>Ir al login ahora</Button>
        </div>
      ) : (
        <form className="space-y-6" onSubmit={onSubmit}>
          <div className="space-y-3">
            <ValidatedInput
              id="email"
              name="email"
              label="Email"
              type="email"
              placeholder="usuario@fi.uba.ar"
              value={email}
              onChange={(e) => setEmail((e.target as HTMLInputElement).value)}
              onBlur={() => setTouched(true)}
              error={touched && !email ? "El email es obligatorio" : undefined}
              autoComplete="email"
            />
          </div>
          {status === "error" && (
            <p className="text-sm text-red-600">
              {errorMsg === 'EMAIL_NOT_FOUND' ? 'Ese email no está registrado' : errorMsg}
            </p>
          )}
          <div className="flex flex-col items-center gap-3">
            <Button type="submit" disabled={status === "loading"} className="w-full">
              {status === "loading" ? "Enviando..." : "Enviar instrucciones"}
            </Button>
            <Button type="button" variant="ghost" size="sm" onClick={() => setLocation("/login")}>Volver al login</Button>
          </div>
        </form>
      )}
    </AuthContainer>
  );
};


