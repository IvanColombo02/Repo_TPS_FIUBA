import { useEffect, useState } from "react";
import { useSearch, useLocation } from "wouter";
import { resetPassword } from "@/services/UserServices";
import { Button } from "@/components/ui/button";
import { PasswordInput } from "@/components/ui/password-input";
import { AuthContainer } from "@/components/Auth/AuthContainer";

export const ResetPasswordScreen = () => {
  const search = useSearch();
  const [, setLocation] = useLocation();
  const [token, setToken] = useState<string | null>(null);
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [status, setStatus] = useState<"idle" | "loading" | "success" | "error">("idle");
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  useEffect(() => {
    const params = new URLSearchParams(search);
    setToken(params.get("token"));
  }, [search]);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token) {
      setErrorMsg("Token no encontrado en el link");
      setStatus("error");
      return;
    }
    if (password.length < 6) {
      setErrorMsg("La contraseña debe tener al menos 6 caracteres");
      setStatus("error");
      return;
    }
    if (password !== confirm) {
      setErrorMsg("Las contraseñas no coinciden");
      setStatus("error");
      return;
    }
    setStatus("loading");
    setErrorMsg(null);
    try {
      await resetPassword(token, password);
      setStatus("success");

      setTimeout(() => setLocation("/login?message=password_reset"), 2000);
    } catch (err: unknown) {
      setStatus("error");
      const code = String((err as Error)?.message || "");
      if (code === "PASSWORD_INVALID") {
        setErrorMsg("La contraseña no es válida (mínimo 6 caracteres)");
      } else if (code === "PASSWORD_SAME_AS_OLD") {
        setErrorMsg("La nueva contraseña no puede ser igual a la anterior");
      } else if (code === "TOKEN_INVALID") {
        setErrorMsg("El enlace es inválido o expiró, solicitá uno nuevo");
      } else {
        setErrorMsg("Error al cambiar la contraseña");
      }
    }
  };

  return (
    <AuthContainer title="Cambiar contraseña">
      {!token ? (
        <p className="text-sm text-red-600">Token no encontrado</p>
      ) : status === "success" ? (
        <p>Contraseña actualizada. Redirigiéndote al login...</p>
      ) : (
        <form className="space-y-4" onSubmit={onSubmit}>
          <div className="space-y-2">
            <label htmlFor="new-password" className="text-sm">
              Nueva contraseña
            </label>
            <PasswordInput
              id="new-password"
              name="new-password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="new-password"
            />
          </div>

          <div className="space-y-2">
            <label htmlFor="confirm-password" className="text-sm">
              Confirmar contraseña
            </label>
            <PasswordInput
              id="confirm-password"
              name="confirm-password"
              placeholder="••••••••"
              value={confirm}
              onChange={(e) => setConfirm(e.target.value)}
              autoComplete="new-password"
            />
          </div>

          {status === "error" && (
            <p className="text-sm text-red-600">{errorMsg}</p>
          )}

          <Button type="submit" disabled={status === "loading"}>
            {status === "loading" ? "Guardando..." : "Cambiar contraseña"}
          </Button>
        </form>
      )}
    </AuthContainer>
  );
};