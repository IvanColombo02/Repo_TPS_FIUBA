import { useState } from "react";
import { useAppForm } from "@/config/use-app-form";
import { ProductCreateSchema, ProductCreateForm } from "@/models/Product";
import { FormValidateOrFn } from "@tanstack/react-form";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ErrorMessage } from "@/components/ui/error-message";
import { IngredientSelectionDialog } from "./IngredientSelectionDialog";
import { Plus, ChefHat } from "lucide-react";
import { Upload, X, Image as ImageIcon } from "lucide-react";
import { validateAndConvertImage } from "@/lib/utils/image-utils";


interface ProductCreateFormProps {
  onSave: (data: ProductCreateForm) => Promise<void>;
  isSubmitting?: boolean;
}

export function ProductCreateFormComponent({ onSave, isSubmitting = false }: ProductCreateFormProps) {
  const [isIngredientDialogOpen, setIsIngredientDialogOpen] = useState(false);
    const [imagePreview, setImagePreview] = useState<string | null>(null);
    const [imageError, setImageError] = useState<string | null>(null);


    const form = useAppForm({
    defaultValues: {
      name: "",
      description: "",
      price: 1,
      categories: [],
      type: "",
      estimatedTime: 0,
      ingredientsIds: {},
      base64Image: "",
    } as ProductCreateForm,
    onSubmit: async ({ value }) => {
      const currentBase64Image = form.getFieldValue("base64Image") || value.base64Image || "";
      const formData = {
        ...value,
        base64Image: currentBase64Image,
      };
      await onSave(formData);
      form.reset();
      setCategoriesInput("");
      setImagePreview(null);
      setImageError(null);
    },
    validators: {
      onSubmit: ProductCreateSchema as unknown as FormValidateOrFn<ProductCreateForm>,
    },
  });

  const [categoriesInput, setCategoriesInput] = useState(() => form.state.values.categories.join("; "));

  const parseCategoriesInput = (inputValue: string) => {
    return inputValue
      .split(";")
      .map(cat => cat.trim())
      .filter(cat => cat.length > 0);
  };

  const handleIngredientSelection = (ingredients: Record<string, number>) => {
    form.setFieldValue("ingredientsIds", ingredients);
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
        setImagePreview(result.base64 || null);
        field.handleChange(result.base64 || "");
    };

    const handleRemoveImage = (field: { handleChange: (value: string) => void }) => {
        setImagePreview(null);
        field.handleChange("");
    };


    return (
    <div className="space-y-4">
        <form
            onSubmit={(e) => {
                e.preventDefault();
                e.stopPropagation();
                form.handleSubmit?.(e);
            }}
            className="space-y-4"
        >
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <form.Field
                    name="name"
                    children={(field) => (
                        <div className="space-y-2">
                            <Label htmlFor="name">Nombre *</Label>
                            <Input
                                id="name"
                                placeholder="Ej: Pizza Margherita"
                                value={field.state.value}
                                onChange={(e) => field.handleChange(e.target.value)}
                                onBlur={field.handleBlur}
                            />
                            {field.state.meta.errors.length > 0 && (
                                <ErrorMessage error={field.state.meta.errors[0] || null}/>
                            )}
                        </div>
                    )}
                />

                <form.Field
                    name="price"
                    children={(field) => (
                        <div className="space-y-2">
                            <Label htmlFor="price">Precio *</Label>
                            <Input
                                id="price"
                                type="number"
                                min="1"
                                step="0.01"
                                placeholder="15.99"
                                value={field.state.value}
                                onChange={(e) => {
                                    const val = e.target.value === '' ? 0 : parseFloat(e.target.value);
                                    field.handleChange(isNaN(val) ? 0 : val);
                                }}
                                onBlur={field.handleBlur}
                            />
                            {field.state.meta.errors.length > 0 && (
                                <ErrorMessage error={field.state.meta.errors[0] || null}/>
                            )}
                        </div>
                    )}
                />

            </div>

            <form.Field
                name="description"
                children={(field) => (
                    <div className="space-y-2">
                        <Label htmlFor="description">Descripción *</Label>
                        <Input
                            id="description"
                            placeholder="Descripción del producto..."
                            value={field.state.value}
                            onChange={(e) => field.handleChange(e.target.value)}
                            onBlur={field.handleBlur}
                        />
                        {field.state.meta.errors.length > 0 && (
                            <ErrorMessage error={field.state.meta.errors[0] || null}/>
                        )}
                    </div>
                )}
            />

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <form.Field
                    name="type"
                    children={(field) => (
                        <div className="space-y-2">
                            <Label htmlFor="type">Tipo *</Label>
                            <Input
                                id="type"
                                placeholder="Ej: Vegetariano"
                                value={field.state.value}
                                onChange={(e) => field.handleChange(e.target.value)}
                                onBlur={field.handleBlur}
                            />
                            {field.state.meta.errors.length > 0 && (
                                <ErrorMessage error={field.state.meta.errors[0] || null}/>
                            )}
                        </div>
                    )}
                />

                <form.Field
                    name="estimatedTime"
                    children={(field) => (
                        <div className="space-y-2">
                            <Label htmlFor="estimatedTime">Tiempo estimado (min) *</Label>
                            <Input
                                id="estimatedTime"
                                type="number"
                                min="1"
                                placeholder="15"
                                value={field.state.value}
                                onChange={(e) => {
                                    const val = e.target.value === '' ? 0 : parseInt(e.target.value);
                                    field.handleChange(isNaN(val) ? 0 : val);
                                }}
                                onBlur={field.handleBlur}
                            />
                            {field.state.meta.errors.length > 0 && (
                                <ErrorMessage error={field.state.meta.errors[0] || null}/>
                            )}
                        </div>
                    )}
                />
            </div>

            <form.Field
                name="categories"
                children={(field) => (
                    <div className="space-y-2">
                        <Label htmlFor="categories">Categorías * (separadas por punto y coma)</Label>
                        <Input
                            id="categories"
                            placeholder="Ej: Entrada; Bebida; Dulce"
                            value={categoriesInput}
                            onChange={(e) => {
                                const inputValue = e.target.value;
                                setCategoriesInput(inputValue);
                                const categories = parseCategoriesInput(inputValue);
                                field.handleChange(categories);
                            }}
                            onBlur={() => {
                                field.handleBlur();
                                const normalized = parseCategoriesInput(categoriesInput).join('; ');
                                setCategoriesInput(normalized);
                            }}
                        />
                        {field.state.meta.errors.length > 0 && (
                            <ErrorMessage error={field.state.meta.errors[0] || null}/>
                        )}
                    </div>
                )}
            />

            <div className="space-y-2">
                <Label>Ingredientes *</Label>
                <Button
                    type="button"
                    variant="outline"
                    onClick={() => setIsIngredientDialogOpen(true)}
                    className="w-full justify-start"
                >
                    <ChefHat className="mr-2 h-4 w-4"/>
                    {Object.keys(form.state.values.ingredientsIds).length > 0
                        ? `${Object.keys(form.state.values.ingredientsIds).length} ingrediente(s) seleccionado(s)`
                        : "Seleccionar ingredientes"
                    }
                </Button>
                {form.state.errors.find((error: unknown): error is {
                    path: string
                } => typeof error === 'object' && error !== null && 'path' in error && (error as {
                    path: string
                }).path === 'ingredientsIds') && (
                    <ErrorMessage error={form.state.errors.find((error: unknown): error is {
                        path: string
                    } => typeof error === 'object' && error !== null && 'path' in error && (error as {
                        path: string
                    }).path === 'ingredientsIds') || null}/>
                )}
            </div>

            <form.Field
                name="base64Image"
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
                                    <ImageIcon className="w-14 h-14 text-muted-foreground"/>
                                )}
                            </div>

                            {imagePreview && (
                                <button
                                    type="button"
                                    onClick={() => handleRemoveImage(field)}
                                    className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full p-1.5 hover:bg-red-600 transition-colors"
                                >
                                    <X className="w-4 h-4"/>
                                </button>
                            )}

                            <Label
                                htmlFor="base64Image"
                                className="absolute bottom-1 right-1 bg-primary text-primary-foreground rounded-full p-2 cursor-pointer hover:bg-primary/90 transition-colors shadow-lg"
                            >
                                <Upload className="w-4 h-4"/>
                            </Label>

                            <Input
                                id="base64Image"
                                type="file"
                                accept="image/*"
                                className="hidden"
                                onChange={(e) => handleImageChange(e, field)}
                            />
                        </div>

                        {imageError && (
                            <p className="text-sm text-red-400 font-medium">{imageError}</p>
                        )}
                        <p className="text-xs text-muted-foreground">
                            Haz clic en el ícono para subir una foto (opcional, máx. 2MB)
                        </p>
                    </div>
                )}
            />


            <Button
                type="submit"
                disabled={isSubmitting || !form.state.isValid}
                className="w-full"
            >
                {isSubmitting ? (
                    <>
                        <div
                            className="mr-2 h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent"/>
                        Creando...
                    </>
                ) : (
                    <>
                        <Plus className="mr-2 h-4 w-4"/>
                        Crear Producto
                    </>
                )}
            </Button>
        </form>

        <IngredientSelectionDialog
            isOpen={isIngredientDialogOpen}
            onClose={() => setIsIngredientDialogOpen(false)}
            onSave={handleIngredientSelection}
            initialIngredients={form.state.values.ingredientsIds}
        />
    </div>
    );
}
