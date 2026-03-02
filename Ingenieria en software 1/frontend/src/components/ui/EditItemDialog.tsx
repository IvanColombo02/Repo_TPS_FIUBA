import { useEffect, useState, useRef } from "react";
import { useAppForm } from "@/config/use-app-form";
import { IngredientCreateSchema, IngredientCreateForm } from "@/models/Ingredient";
import { FormValidateOrFn } from "@tanstack/react-form";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ErrorMessage } from "@/components/ui/error-message";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { ImageIcon, Loader2, Upload, X } from "lucide-react";
import { validateAndConvertImage } from "@/lib/utils/image-utils";
interface EditItemDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (data: { name: string; stock: number; base64Image?: string }) => Promise<void>;
  initialData?: { id: number; name: string; stock: number; base64Image?: string };
  title?: string;
  isSubmitting?: boolean;
}

export function EditItemDialog({
  isOpen,
  onClose,
  onSave,
  initialData,
  title = "Editar Elemento",
  isSubmitting = false
}: EditItemDialogProps) {
  const form = useAppForm({
    defaultValues: {
      name: "",
      stock: 0,
      base64Image: "",
    } as IngredientCreateForm,
    onSubmit: async ({ value }) => {
      await onSave({
        name: value.name,
        stock: value.stock,
        base64Image: value.base64Image,
      });
      onClose();
    },
    validators: {
      onSubmit: IngredientCreateSchema as unknown as FormValidateOrFn<IngredientCreateForm>,
    },
  });

  const [imagePreview, setImagePreview] = useState<string | null>(
    initialData?.base64Image && initialData.base64Image.trim() !== ""
      ? initialData.base64Image
      : null
  );
  const [imageError, setImageError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (initialData && isOpen) {
      const base64Image = initialData.base64Image;
      form.setFieldValue("name", initialData.name);
      form.setFieldValue("stock", initialData.stock);
      form.setFieldValue("base64Image", base64Image || "");

      const previewValue = base64Image && base64Image.trim() !== "" ? base64Image : null;
      setImagePreview(previewValue);
      setImageError(null);
    } else if (!isOpen) {
      form.reset();
      setImagePreview(null);
      setImageError(null);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [initialData?.id, isOpen]);

  const handleClose = () => {
    form.reset();
    setImagePreview(null);
    setImageError(null);
    onClose();
  };
  const handleImageChange = async (
    e: React.ChangeEvent<HTMLInputElement>,
    field: { handleChange: (value: string) => void }
  ) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const result = await validateAndConvertImage(file);
    if (!result.valid) {
      setImageError(result.error || "Error al procesar la imagen");
      return;
    }

    setImageError(null);
    const base64Value = result.base64 || "";
    setImagePreview(base64Value || null);
    field.handleChange(base64Value);
    form.setFieldValue("base64Image", base64Value);
  };

  const handleRemoveImage = (field: { handleChange: (value: string) => void }) => {
    setImagePreview(null);
    field.handleChange("");
    form.setFieldValue("base64Image", "");
  };


  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          <DialogDescription>
            Modifica los datos del elemento y guarda los cambios.
          </DialogDescription>
        </DialogHeader>

        <form
          onSubmit={(e) => {
            e.preventDefault();
            e.stopPropagation();
            form.handleSubmit();
          }}
          className="space-y-4"
        >
          <div className="grid grid-cols-1 gap-4">
            <form.Field
              name="name"
              children={(field) => (
                <div className="space-y-2">
                  <Label htmlFor="name">Nombre</Label>
                  <Input
                    id="name"
                    placeholder="Ej: Tomate, Lechuga..."
                    value={field.state.value}
                    onChange={(e) => field.handleChange(e.target.value)}
                    onBlur={field.handleBlur}
                  />
                  {field.state.meta.errors.length > 0 && (
                    <ErrorMessage error={field.state.meta.errors[0] || null} />
                  )}
                </div>
              )}
            />

            <form.Field
              name="stock"
              children={(field) => (
                <div className="space-y-2">
                  <Label htmlFor="stock">Stock</Label>
                  <Input
                    id="stock"
                    type="number"
                    min="0"
                    max="10000"
                    placeholder="100"
                    value={field.state.value}
                    onChange={(e) => {
                      const val = e.target.value === '' ? 0 : parseInt(e.target.value);
                      field.handleChange(isNaN(val) ? 0 : val);
                    }}
                    onBlur={field.handleBlur}
                  />
                  {field.state.meta.errors.length > 0 && (
                    <ErrorMessage error={field.state.meta.errors[0] || null} />
                  )}
                </div>
              )}
            />

            <form.Field
              name="base64Image"
              children={(field) => (
                <div className="flex flex-col items-center space-y-2">
                  <div className="relative">
                    <div
                      className="w-28 h-28 rounded-full border-4 border-border overflow-hidden bg-muted flex items-center justify-center">
                      {imagePreview ? (
                        <img src={imagePreview} alt="Preview" loading="lazy" decoding="async" className="w-full h-full object-cover" />
                      ) : (
                        <ImageIcon className="w-14 h-14 text-muted-foreground" />
                      )}
                    </div>

                    {imagePreview && (
                      <button
                        type="button"
                        onClick={() => handleRemoveImage(field)}
                        className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full p-1.5 hover:bg-red-600 transition-colors"
                      >
                        <X className="w-4 h-4" />
                      </button>
                    )}

                    <Label
                      htmlFor="base64Image"
                      className="absolute bottom-1 right-1 bg-primary text-primary-foreground rounded-full p-2 cursor-pointer hover:bg-primary/90 transition-colors shadow-lg"
                      onClick={(e) => {
                        e.preventDefault();
                        if (fileInputRef.current) {
                          fileInputRef.current.click();
                        }
                      }}
                    >
                      <Upload className="w-4 h-4" />
                    </Label>

                    <Input
                      ref={fileInputRef}
                      id="base64Image"
                      type="file"
                      accept="image/*"
                      className="hidden"
                      onChange={(e) => handleImageChange(e, field)}
                    />
                  </div>

                  {imageError && <p className="text-sm text-red-400 font-medium">{imageError}</p>}
                  <p className="text-xs text-muted-foreground">
                    Haz clic en el ícono para cambiar la foto (opcional, máx. 2MB)
                  </p>
                </div>
              )}
            />
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={handleClose}
              disabled={isSubmitting}
            >
              Cancelar
            </Button>
            <Button
              type="submit"
              disabled={isSubmitting || !form.state.isValid}
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Guardando...
                </>
              ) : (
                "Guardar"
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
