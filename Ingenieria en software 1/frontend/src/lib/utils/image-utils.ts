export const MAX_IMAGE_SIZE = 2 * 1024 * 1024;

export interface ImageValidationResult {
  valid: boolean;
  error?: string;
  base64?: string;
}

/**
 * Valida y convierte una imagen a base64
 * @param file - Archivo de imagen a validar
 * @returns Promise con el resultado de validación
 */
export function validateAndConvertImage(file: File): Promise<ImageValidationResult> {
  return new Promise((resolve) => {
    if (!file.type.startsWith('image/')) {
      resolve({
        valid: false,
        error: 'Por favor selecciona un archivo de imagen válido'
      });
      return;
    }

    if (file.size > MAX_IMAGE_SIZE) {
      resolve({
        valid: false,
        error: 'La imagen no debe superar los 2MB'
      });
      return;
    }

    const reader = new FileReader();
    reader.onloadend = () => {
      resolve({
        valid: true,
        base64: reader.result as string
      });
    };
    reader.onerror = () => {
      resolve({
        valid: false,
        error: 'Error al leer la imagen'
      });
    };
    reader.readAsDataURL(file);
  });
}

