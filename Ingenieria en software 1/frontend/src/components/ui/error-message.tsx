import { AlertTriangle } from "lucide-react"

import { ZodIssue } from 'zod';

interface ErrorMessageProps {
  error: string | Error | ZodIssue[] | ZodIssue | null;
}

/**
 * Reusable component to display error messages consistently.
 */
export const ErrorMessage = ({ error }: ErrorMessageProps) => {
  if (!error) return null;
  
  if (typeof error === 'string') {
    return renderMessage(error);
  }
  if (Array.isArray(error)) {
    return renderMessage(error[0]?.message || 'Error de validación');
  }
  if ('message' in error && typeof error.message === 'string' && error.message) {
    return renderMessage(error.message);
  }
  return renderMessage('Error de validación');
};

function renderMessage(message: string) {
  
  return (
    <div className="flex items-start gap-1.5 text-destructive">
      <AlertTriangle className="w-4 h-4 mt-0.5 flex-shrink-0" />
      <p className="text-sm font-medium leading-tight">{message}</p>
    </div>
  );
};

