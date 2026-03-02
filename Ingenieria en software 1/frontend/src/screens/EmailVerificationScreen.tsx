import { useEffect, useState } from "react";
import { useLocation, useSearch } from "wouter";
import { UtensilsCrossed, CheckCircle, XCircle, Loader2 } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { BASE_API_URL } from "../config/app-query-client";
import { Button } from "@/components/ui/button";
import { Alert, AlertDescription } from "@/components/ui/alert";

export const EmailVerificationScreen = () => {
  const search = useSearch();
  const [, setLocation] = useLocation();
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
  const [message, setMessage] = useState('');

  useEffect(() => {
    const params = new URLSearchParams(search);
    const token = params.get('token');
    const error = params.get('error');

    if (!token) {
      setStatus('error');
      setMessage('Token de verificación no encontrado');
      return;
    }

    if (error === 'invalid') {
      setStatus('error');
      setMessage('Token inválido o expirado');
      return;
    }


    fetch(`${BASE_API_URL || 'http://localhost:8080'}/users/verify?token=${token}`, {
      method: 'GET',
    })
      .then(response => {
        if (response.ok) {
          setStatus('success');
          setMessage('Email verificado exitosamente');
          // Redirect to login after 3 seconds
          setTimeout(() => {
            setLocation('/login?message=email_verified');
          }, 3000);
        } else {
          setStatus('error');
          setMessage('Token inválido o expirado');
        }
      })
      .catch(() => {
        setStatus('error');
        setMessage('Error al verificar el email');
      });
  }, [search, setLocation]);

  const handleGoToLogin = () => {
    setLocation('/login?message=email_verified');
  };

  return (
    <div className="min-h-screen bg-background dark flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="flex flex-col items-center mb-8">
          <div className="flex items-center gap-3 mb-4">
            <UtensilsCrossed className="h-12 w-12 text-primary" />
            <h1 className="text-3xl font-bold text-foreground">Comedor FIUBA</h1>
          </div>
          <p className="text-muted-foreground text-center">Verificación de Email</p>
        </div>

        <Card className="border-border shadow-lg">
          <CardHeader>
            <CardTitle className="text-2xl text-foreground">
              {status === 'loading' && 'Verificando...'}
              {status === 'success' && 'Verificación Exitosa'}
              {status === 'error' && 'Error de Verificación'}
            </CardTitle>
            <CardDescription>
              {status === 'loading' && 'Estamos verificando tu email...'}
              {status === 'success' && 'Tu email ha sido verificado correctamente'}
              {status === 'error' && 'No pudimos verificar tu email'}
            </CardDescription>
          </CardHeader>

          <CardContent className="space-y-4">
            {status === 'loading' && (
              <div className="flex items-center justify-center py-8">
                <Loader2 className="h-8 w-8 animate-spin text-primary" />
              </div>
            )}

            {status === 'success' && (
              <Alert variant="default" className="mb-4">
                <CheckCircle className="h-4 w-4" />
                <AlertDescription>{message}</AlertDescription>
              </Alert>
            )}

            {status === 'error' && (
              <Alert variant="destructive" className="mb-4">
                <XCircle className="h-4 w-4" />
                <AlertDescription>{message}</AlertDescription>
              </Alert>
            )}

            {status === 'success' && (
              <div className="text-center space-y-4">
                <p className="text-sm text-muted-foreground">
                  Serás redirigido al login automáticamente en unos segundos...
                </p>
                <Button onClick={handleGoToLogin} className="w-full" size="lg">
                  Ir al Login Ahora
                </Button>
              </div>
            )}

            {status === 'error' && (
              <div className="text-center space-y-4">
                <Button onClick={handleGoToLogin} className="w-full" size="lg">
                  Volver al Login
                </Button>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
};