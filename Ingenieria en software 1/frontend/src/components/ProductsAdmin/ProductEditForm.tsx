import { useState, useEffect, useMemo } from "react";
import { useAppForm } from "@/config/use-app-form";
import { ProductUpdateSchema, ProductUpdateForm } from "@/models/Product";
import { FormValidateOrFn } from "@tanstack/react-form";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ErrorMessage } from "@/components/ui/error-message";
import { IngredientSelectionDialog } from "./IngredientSelectionDialog";
import { Loader2, Save, ChefHat } from "lucide-react";
import { Upload, X, Image as ImageIcon } from "lucide-react";
import { validateAndConvertImage } from "@/lib/utils/image-utils";


interface ProductEditFormProps {
    product: {
        id: number;
        name: string;
        description: string;
        price: number;
        stock: number;
        categories: string[];
        type: string;
        estimatedTime: number;
        ingredients: Record<string, number>;
        base64Image: string;
    };
    onSave: (data: ProductUpdateForm) => Promise<void>;
    isSubmitting?: boolean;
}

export function ProductEditFormComponent({ product, onSave, isSubmitting = false }: ProductEditFormProps) {
    const [isIngredientDialogOpen, setIsIngredientDialogOpen] = useState(false);
    const [imagePreview, setImagePreview] = useState<string | null>(product.base64Image || null);
    const [imageError, setImageError] = useState<string | null>(null);


    const initialIngredients = useMemo(() => {

        let parsedIngredients: Record<string, number> = {};

        if (product.ingredients) {
            if (typeof product.ingredients === 'object' && !Array.isArray(product.ingredients)) {
                for (const [ingredientKey, quantity] of Object.entries(product.ingredients)) {

                    if (/^\d+$/.test(ingredientKey)) {
                        parsedIngredients[ingredientKey] = quantity;
                        continue;
                    }

                    const idMatch = ingredientKey.match(/id=(\d+)/);
                    if (idMatch) {
                        const id = idMatch[1];

                        parsedIngredients[id] = quantity;
                    }
                }

            } else if (Array.isArray(product.ingredients)) {

                parsedIngredients = product.ingredients.reduce((acc: Record<string, number>, item: unknown) => {
                    if (item && typeof item === 'object' && 'id' in item && 'quantity' in item) {
                        const typedItem = item as { id: number; quantity: number };
                        acc[typedItem.id.toString()] = typedItem.quantity;
                    }
                    return acc;
                }, {});
            }
        }
        return parsedIngredients;
    }, [product.ingredients]);

    const [currentIngredients, setCurrentIngredients] = useState<Record<string, number>>(initialIngredients);

    const form = useAppForm({
        defaultValues: {
            name: product.name,
            description: product.description,
            price: product.price,

            categories: product.categories,
            type: product.type,
            estimatedTime: product.estimatedTime,
            ingredientsIds: initialIngredients,
            base64Image: product.base64Image,
        } as ProductUpdateForm,
        onSubmit: async ({ value }) => {

            const currentIds = new Set(Object.keys(initialIngredients).map(id => parseInt(id)));
            const newIds = new Set(Object.keys(value.ingredientsIds).map(id => parseInt(id)));


            const addIngredients: Record<string, number> = {};

            for (const [idStr, quantity] of Object.entries(value.ingredientsIds)) {
                const id = parseInt(idStr);

                if (!currentIds.has(id) || initialIngredients[idStr] !== quantity) {
                    addIngredients[idStr] = quantity;

                }
            }


            const deleteIngredients: number[] = [];

            for (const id of currentIds) {
                if (!newIds.has(id)) {
                    deleteIngredients.push(id);

                }
            }


            const currentImage = product.base64Image || "";
            const newImage = value.base64Image || "";
            const imageChanged = currentImage !== newImage;

            const updateData = {
                name: value.name,
                description: value.description,
                price: value.price,
                categories: value.categories,
                type: value.type,
                estimatedTime: value.estimatedTime,
                ...(imageChanged ? { base64Image: newImage } : {}),
                ...(Object.keys(addIngredients).length > 0 && { addIngredients }),
                ...(deleteIngredients.length > 0 && { deleteIngredients }),
            };

            // @ts-expect-error - updateData type compatibility
            await onSave(updateData);
        },
        validators: {
            onSubmit: ProductUpdateSchema as unknown as FormValidateOrFn<ProductUpdateForm>,
        },
    });

    const [categoriesInput, setCategoriesInput] = useState(() => (product.categories || []).join("; "));

    const parseCategoriesInput = (inputValue: string) => {
        return inputValue
            .split(";")
            .map(cat => cat.trim())
            .filter(cat => cat.length > 0);
    };


    useEffect(() => {
        setCurrentIngredients(form.state.values.ingredientsIds);
    }, [form.state.values.ingredientsIds]);

    useEffect(() => {
        setCategoriesInput((product.categories || []).join('; '));
    }, [product.id, product.categories]);

    const handleIngredientSelection = (ingredients: Record<string, number>) => {
        setCurrentIngredients(ingredients);
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
                                <ErrorMessage error={field.state.meta.errors[0] || null} />
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
                                <ErrorMessage error={field.state.meta.errors[0] || null} />
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
                            <ErrorMessage error={field.state.meta.errors[0] || null} />
                        )}
                    </div>
                )}
            />

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
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
                                <ErrorMessage error={field.state.meta.errors[0] || null} />
                            )}
                        </div>
                    )}
                />

                <form.Field
                    name="estimatedTime"
                    children={(field) => (
                        <div className="space-y-2">
                            <Label htmlFor="estimatedTime">Tiempo (min) *</Label>
                            <Input
                                id="estimatedTime"
                                type="number"
                                min="1"
                                placeholder="30"
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
            </div>

            <form.Field
                name="categories"
                children={(field) => (
                    <div className="space-y-2">
                        <Label htmlFor="categories">Categorías (separadas por punto y coma) *</Label>
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
                            <ErrorMessage error={field.state.meta.errors[0] || null} />
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
                    <ChefHat className="mr-2 h-4 w-4" />
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
                        }).path === 'ingredientsIds') || null} />
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
                            >
                                <Upload className="w-4 h-4" />
                            </Label>

                            <Input
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


            <Button
                type="submit"
                disabled={isSubmitting}
                className="w-full"
            >
                {isSubmitting ? (
                    <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        Guardando...
                    </>
                ) : (
                    <>
                        <Save className="mr-2 h-4 w-4" />
                        Guardar Cambios
                    </>
                )}
            </Button>

            <IngredientSelectionDialog
                isOpen={isIngredientDialogOpen}
                onClose={() => setIsIngredientDialogOpen(false)}
                onSave={handleIngredientSelection}
                initialIngredients={currentIngredients}
            />
        </form>
    );
}
