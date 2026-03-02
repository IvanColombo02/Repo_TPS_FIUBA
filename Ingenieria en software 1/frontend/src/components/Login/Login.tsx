import { useAppForm } from "@/config/use-app-form";
import { LoginRequest, LoginRequestSchema } from "@/models/Login";
import { InvalidCredentialsError, EmailNotValidatedError } from "@/services/UserServices";
import { UtensilsCrossed, AlertCircle, MailWarning, CheckCircle } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Link, useSearch } from "wouter"
import { PasswordInput } from "@/components/ui/password-input"
import {ValidatedInput} from "@/components/form/ValidatedInput.tsx";


type Props = {
  onSubmit: (value: LoginRequest) => void;
  submitError: Error | null;
};

function getUrlMessage(search: string) {
  const params = new URLSearchParams(search);
  const message = params.get('message');
  
  if (message === 'signup_success') {
    return {
      icon: CheckCircle,
      message: "Cuenta creada exitosamente. Revisa tu email para activar tu cuenta.",
      variant: "default" as const,
    };
  }
  
  if (message === 'reset_email_sent') {
    return {
      icon: CheckCircle,
      message: "Te enviamos un correo para recuperar tu contraseña.",
      variant: "default" as const,
    };
  }

  if (message === 'password_reset') {
    return {
      icon: CheckCircle,
      message: "Tu contraseña fue actualizada correctamente.",
      variant: "default" as const,
    };
  }

  if (message === 'email_verified') {
    return {
      icon: CheckCircle,
      message: "Email verificado exitosamente. Ya puedes iniciar sesión.",
      variant: "default" as const,
    };
  }
  
  return null;
}

function getErrorInfo(error: Error | null) {
  if (!error) return null;
  
  if (error instanceof InvalidCredentialsError) {
    return {
      icon: AlertCircle,
      message: error.message,
      variant: "destructive" as const,
    };
  }
  
  if (error instanceof EmailNotValidatedError) {
    return {
      icon: MailWarning,
      message: error.message,
      variant: "destructive" as const,
    };
  }

  return {
    icon: AlertCircle,
    message: error.message || "Ocurrió un error al intentar iniciar sesión",
    variant: "destructive" as const,
  };
}

export function Login({ onSubmit, submitError }: Props) {
  const search = useSearch();
  
  const formData = useAppForm({
    defaultValues: {
      email: "",
      password: "",
    },
    validators: {
      onSubmit: LoginRequestSchema,
    },
    onSubmit: async ({ value }) => onSubmit(value),
  });

  const urlMessage = getUrlMessage(search);
  const errorInfo = getErrorInfo(submitError);

  return (
  <div className="min-h-screen bg-background dark flex items-center justify-center p-4">
    <div className="w-full max-w-md">
      <div className="flex flex-col items-center mb-8">
        <div className="flex items-center gap-3 mb-4">
          <UtensilsCrossed className="h-12 w-12 text-primary" />
          <h1 className="text-3xl font-bold text-foreground">Comedor FIUBA</h1>
        </div>
        <p className="text-muted-foreground text-center">Inicia sesión para realizar tu pedido</p>
      </div>

      <Card className="border-border shadow-lg">
        <CardHeader>
          <CardTitle className="text-2xl text-foreground">Iniciar Sesión</CardTitle>
          <CardDescription>Ingresa tus credenciales para acceder</CardDescription>
        </CardHeader>
        
        <formData.AppForm>
          <CardContent className="space-y-4 [&_button[type=submit]]:hidden">
            {urlMessage && (
                <Alert variant={urlMessage.variant} className="mb-4">
                  <urlMessage.icon className="h-4 w-4"/>
                  <AlertDescription>{urlMessage.message}</AlertDescription>
                </Alert>
            )}

            {errorInfo && (
                <Alert variant={errorInfo.variant} className="mb-4">
                  <errorInfo.icon className="h-4 w-4"/>
                  <AlertDescription>{errorInfo.message}</AlertDescription>
                </Alert>
            )}
            <formData.AppField
                name="email"
                children={(field) => (
                    <ValidatedInput
                        id="email"
                        name="email"
                        label="Email"
                        type="email"
                        placeholder="tu-email@ejemplo.com"
                        value={field.state.value}
                        onChange={(e) => field.handleChange(e.target.value)}
                        onBlur={field.handleBlur}
                        error={field.state.meta.errors[0]?.message}
                        autoComplete="email"
                    />
                )}
            />
            <formData.AppField
                name="password"
                children={(field) => (
                    <div className="space-y-2">
                      <Label htmlFor="password" className="text-foreground">Contraseña</Label>
                      <PasswordInput
                          id="password"
                          name="password"
                          placeholder="••••••••"
                          value={field.state.value}
                          onBlur={field.handleBlur}
                          onChange={(e) => field.handleChange(e.target.value)}
                          className= {`text-foreground ${field.state.meta.errors.length > 0 ? 'border-red-500 focus-visible:ring-red-500' : ''}`}

                          autoComplete="current-password"
                      />
                      {field.state.meta.errors.length > 0 && (
                          <p className="text-sm text-destructive">
                            {field.state.meta.errors[0]?.message}
                          </p>
                      )}
                    </div>
                )}
            />
          </CardContent>
        </formData.AppForm>

        <CardFooter className="flex flex-col gap-4">
          <Button
              type="button"
              onClick={formData.handleSubmit}
              className="w-full"
              size="lg"
          >
            Iniciar Sesión
          </Button>

          <Link href="/forgot" className="text-sm text-primary hover:underline font-medium">
            ¿Olvidaste tu contraseña?
          </Link>
          
          <p className="text-sm text-muted-foreground text-center">
            ¿No tienes cuenta?{" "}
          <Link href="/signup" className="text-primary hover:underline font-medium">
              Regístrate aquí
            </Link>
          </p>
        </CardFooter>
      </Card>

      {}
    </div>
  </div>
);
}
