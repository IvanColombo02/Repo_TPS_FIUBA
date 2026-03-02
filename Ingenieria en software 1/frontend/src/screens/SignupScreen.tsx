import { useAppForm } from "@/config/use-app-form";
import { SignupRequestSchema, SignupRequest } from "@/models/Login";
import { FormValidateOrFn } from "@tanstack/react-form";
import { useSignup, InvalidCredentialsError, EmailNotValidatedError, UserAlreadyExistsError } from "@/services/UserServices";
import { UtensilsCrossed, AlertTriangle, Upload, User } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Link, useLocation } from "wouter"
import { useState, useEffect } from "react"
import { ErrorMessage } from "@/components/ui/error-message"
import { validateAndConvertImage } from "@/lib/utils/image-utils"
import { PasswordInput } from "@/components/ui/password-input"
import { ValidatedInput } from "@/components/form/ValidatedInput"
import { ValidatedSelect } from "@/components/form/ValidatedSelect"

function getErrorMessage(error: Error | null): string | null {
  if (!error) return null;

  if (error instanceof UserAlreadyExistsError) {
    return "El email o nombre de usuario ya está registrado";
  }

  if (error instanceof InvalidCredentialsError) {
    return "El email ya está registrado";
  }

  if (error instanceof EmailNotValidatedError) {
    return error.message;
  }

  return error.message || "Ocurrió un error al crear la cuenta";
}

export const SignupScreen = () => {
  const { mutate, error, isSuccess } = useSignup();
  const [, setLocation] = useLocation();
  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const [imageError, setImageError] = useState<string | null>(null);

  useEffect(() => {
    if (isSuccess) {
      setLocation("/login?message=signup_success");
    }
  }, [isSuccess, setLocation]);

  const formData = useAppForm({
    defaultValues: {
      username: "",
      email: "",
      password: "",
      role: "ROLE_USER",
      firstName: "",
      lastName: "",
      address: "",
      birthDate: "",
      gender: "Prefiero no decir" as "Masculino" | "Femenino" | "Prefiero no decir",
      profileImage: "",
    },
    validators: {
      onSubmit: SignupRequestSchema as unknown as FormValidateOrFn<SignupRequest>,
    },
    onSubmit: async ({ value }) => {
      mutate(value);
    },
  });

  const errorMessage = getErrorMessage(error);

  const handleImageChange = async (e: React.ChangeEvent<HTMLInputElement>, field: { handleChange: (value: string) => void }) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const result = await validateAndConvertImage(file);

    if (!result.valid) {
      setImageError(result.error || 'Error al procesar la imagen');
      return;
    }

    setImageError(null);
    setImagePreview(result.base64 || null);
    field.handleChange(result.base64 || "");
  };

  return (
    <div className="min-h-screen bg-background dark flex items-center justify-center p-4 py-4">
      <div className="w-full max-w-3xl">
        <div className="flex flex-col items-center mb-3">
          <div className="flex items-center gap-2 mb-1">
            <UtensilsCrossed className="h-8 w-8 text-primary" />
            <h1 className="text-xl font-bold text-foreground">Comedor FIUBA</h1>
          </div>
          <p className="text-xs text-muted-foreground text-center">Crea tu cuenta para comenzar a pedir</p>
        </div>

        <Card className="border-border shadow-lg">
          <CardHeader className="pb-3 pt-4">
            <CardTitle className="text-lg text-foreground">Crear Cuenta</CardTitle>
            <CardDescription className="text-xs">Completa tus datos para registrarte</CardDescription>
          </CardHeader>

          <formData.AppForm>
            <CardContent className="space-y-4 [&_button[type=submit]]:hidden px-8 py-3">

              {errorMessage && (
                <div className="flex items-start gap-2 p-3 rounded-md bg-red-500/10 border border-red-500">
                  <AlertTriangle className="w-5 h-5 mt-0.5 flex-shrink-0 text-red-500" />
                  <p className="text-sm font-medium text-red-500 leading-tight">{errorMessage}</p>
                </div>
              )}

              <formData.AppField
                name="profileImage"
                children={(field) => (
                  <div className="flex flex-col items-center space-y-2">
                    <div className="relative">
                      <div
                        className="w-28 h-28 rounded-full border-4 border-border overflow-hidden bg-muted flex items-center justify-center">
                        {imagePreview ? (
                          <img
                            src={imagePreview}
                            alt="Preview"
                            className="w-full h-full object-cover"
                          />
                        ) : (
                          <User className="w-14 h-14 text-muted-foreground" />
                        )}
                      </div>
                      <Label
                        htmlFor="profileImage"
                        className="absolute bottom-1 right-1 bg-primary text-primary-foreground rounded-full p-2 cursor-pointer hover:bg-primary/90 transition-colors shadow-lg"
                      >
                        <Upload className="w-4 h-4" />
                      </Label>
                      <Input
                        id="profileImage"
                        type="file"
                        accept="image/*"
                        className="hidden"
                        onChange={(e) => handleImageChange(e, field)}
                      />
                    </div>
                    <p className="text-xs text-muted-foreground">Haz clic en el ícono para subir una foto
                      (opcional)</p>
                    {imageError && (
                      <p className="text-sm text-red-400 font-medium">{imageError}</p>
                    )}
                  </div>
                )}
              />

              <div className="grid grid-cols-2 gap-3">
                <formData.AppField
                  name="firstName"
                  children={(field: {
                    state: { value: string; meta: { errors: (string | undefined)[] } };
                    handleBlur: () => void;
                    handleChange: (value: string) => void
                  }) => (
                    <ValidatedInput
                      id="firstName"
                      name="firstName"
                      label="Nombre"
                      placeholder="Juan"
                      autoComplete="given-name"
                      value={field.state.value}
                      onBlur={field.handleBlur}
                      onChange={(e) => field.handleChange(e.target.value)}
                      error={field.state.meta.errors[0]}
                    />
                  )}
                />

                <formData.AppField
                  name="lastName"
                  children={(field: {
                    state: { value: string; meta: { errors: (string | undefined)[] } };
                    handleBlur: () => void;
                    handleChange: (value: string) => void
                  }) => (
                    <ValidatedInput
                      id="lastName"
                      name="lastName"
                      label="Apellido"
                      placeholder="Pérez"
                      autoComplete="family-name"
                      value={field.state.value}
                      onBlur={field.handleBlur}
                      onChange={(e) => field.handleChange(e.target.value)}
                      error={field.state.meta.errors[0]}
                    />
                  )}
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <formData.AppField
                  name="username"
                  children={(field: {
                    state: { value: string; meta: { errors: (string | undefined)[] } };
                    handleBlur: () => void;
                    handleChange: (value: string) => void
                  }) => (
                    <ValidatedInput
                      id="username"
                      name="username"
                      label="Nombre de usuario"
                      placeholder="tu_usuario"
                      autoComplete="username"
                      value={field.state.value}
                      onBlur={field.handleBlur}
                      onChange={(e) => field.handleChange(e.target.value)}
                      error={field.state.meta.errors[0]}
                    />
                  )}
                />

                <formData.AppField
                  name="gender"
                  children={(field) => (
                    <ValidatedSelect
                      id="gender"
                      label="Género"
                      value={field.state.value}
                      onChange={(value) => field.handleChange(value as "Masculino" | "Femenino" | "Prefiero no decir")}
                      options={[
                        { value: "Masculino", label: "Masculino" },
                        { value: "Femenino", label: "Femenino" },
                        { value: "Prefiero no decir", label: "Prefiero no decir" }
                      ]}
                      error={field.state.meta.errors[0]}
                    />
                  )}
                />
              </div>

              <formData.AppField
                name="birthDate"
                children={(field) => {
                  const currentYear = new Date().getFullYear();
                  const years = Array.from({ length: 100 }, (_, i) => currentYear - i);

                  return (
                    <div className="space-y-2">
                      <Label htmlFor="birthYear" className="text-foreground">Año de Nacimiento</Label>
                      <select
                        id="birthYear"
                        name="birthYear"
                        value={field.state.value}
                        onChange={(e) => field.handleChange(e.target.value)}
                        className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                      >
                        <option value="">Selecciona un año</option>
                        {years.map((year) => (
                          <option key={year} value={`${year}-01-01T00:00:00.000Z`}>
                            {year}
                          </option>
                        ))}
                      </select>
                      <ErrorMessage error={field.state.meta.errors[0] || null} />
                    </div>
                  );
                }}
              />

              <formData.AppField
                name="address"
                children={(field) => (
                  <ValidatedInput
                    id="address"
                    name="address"
                    label="Dirección"
                    placeholder="Av. Paseo Colón 850"
                    autoComplete="street-address"
                    value={field.state.value}
                    onBlur={field.handleBlur}
                    onChange={(e) => field.handleChange(e.target.value)}
                    error={field.state.meta.errors[0]}
                  />
                )}
              />

              <div className="grid grid-cols-2 gap-3">
                <formData.AppField
                  name="email"
                  children={(field: {
                    state: { value: string; meta: { errors: (string | undefined)[] } };
                    handleBlur: () => void;
                    handleChange: (value: string) => void
                  }) => (
                    <ValidatedInput
                      id="email"
                      name="email"
                      label="Email"
                      type="email"
                      placeholder="tu-email@fi.uba.ar"
                      autoComplete="email"
                      value={field.state.value}
                      onBlur={field.handleBlur}
                      onChange={(e) => field.handleChange(e.target.value)}
                      error={field.state.meta.errors[0]}
                    />
                  )}
                />

                <formData.AppField
                  name="password"
                  children={(field: {
                    state: { value: string; meta: { errors: (string | undefined)[] } };
                    handleBlur: () => void;
                    handleChange: (value: string) => void
                  }) => (
                    <div className="space-y-2">
                      <Label htmlFor="password" className="text-foreground">Contraseña</Label>
                      <PasswordInput
                        id="password"
                        name="password"
                        placeholder="••••••••"
                        autoComplete="new-password"
                        value={field.state.value}
                        onBlur={field.handleBlur}
                        onChange={(e) => field.handleChange(e.target.value)}
                        className={`text-foreground ${field.state.meta.errors.length > 0 ? 'border-red-500 focus-visible:ring-red-500' : ''}`}
                      />
                      <ErrorMessage error={field.state.meta.errors[0] || null} />
                    </div>
                  )}
                />
              </div>
            </CardContent>
          </formData.AppForm>

          <CardFooter className="flex flex-col gap-2 pt-3 pb-4">
            <Button
              type="button"
              onClick={formData.handleSubmit}
              className="w-full h-9"
              size="sm"
            >
              Crear Cuenta
            </Button>
            <p className="text-xs text-muted-foreground text-center">
              ¿Ya tienes cuenta?{" "}
              <Link href="/login" className="text-primary hover:underline font-medium">
                Inicia sesión aquí
              </Link>
            </p>
          </CardFooter>
        </Card>
      </div>
    </div>
  );
};
