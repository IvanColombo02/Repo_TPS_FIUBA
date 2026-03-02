import { useState, useMemo, useEffect, useCallback, useRef } from "react";
import { useAppForm } from "@/config/use-app-form";
import { ComboUpdateSchema, ComboUpdateForm } from "@/models/Combo";
import { FormValidateOrFn } from "@tanstack/react-form";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ErrorMessage } from "@/components/ui/error-message";
import { ProductSelectionDialog } from "./ProductSelectionDialog";
import { Package, Save } from "lucide-react";
import { Upload, X, Image as ImageIcon } from "lucide-react";
import { validateAndConvertImage } from "@/lib/utils/image-utils";

interface ComboProduct {
    id: number;
    quantity: number;
}

interface ComboEditFormProps {
    combo: { id: number; name: string; description: string; price: number; stock: number; base64Image?: string; categories?: string[]; types?: string[]; products?: ComboProduct[] };
    onSave: (data: ComboUpdateForm) => Promise<void>;
    isSubmitting?: boolean;
}

export function ComboEditFormComponent({ combo, onSave, isSubmitting = false }: ComboEditFormProps) {
    const [isProductDialogOpen, setIsProductDialogOpen] = useState(false);
    const [imagePreview, setImagePreview] = useState<string | null>(combo.base64Image || null);
    const [imageError, setImageError] = useState<string | null>(null);
    const fileInputRef = useRef<HTMLInputElement | null>(null);

    const setFileInputRef = (element: HTMLInputElement | null) => {
        fileInputRef.current = element;
    };

    const initialProducts = useMemo(() => {
        return combo.products?.reduce((acc: Record<string, number>, product) => {
            acc[product.id.toString()] = product.quantity || 1;
            return acc;
        }, {}) || {};
    }, [combo.products]);


    const [currentProducts, setCurrentProducts] = useState<Record<string, number>>(initialProducts);

    const form = useAppForm({
        defaultValues: {
            name: combo.name || "",
            description: combo.description || "",
            price: combo.price && combo.price > 0 ? combo.price : 1,
            categories: combo.categories || [],
            types: combo.types || [],
            productsIds: initialProducts,
            base64Image: combo.base64Image || "",
        } as ComboUpdateForm,
        onSubmit: async ({ value }) => {
            const addProducts: Record<string, number> = {};
            const deleteProducts: string[] = [];
            const productsIds = value.productsIds ?? {};

            Object.keys(initialProducts).forEach((productId) => {
                if (!(productId in productsIds)) {
                    deleteProducts.push(productId);
                }
            });

            Object.entries(productsIds).forEach(([id, quantity]) => {
                addProducts[id] = quantity;
            });

            const hasProductChanges = deleteProducts.length > 0 ||
                Object.entries(addProducts).some(([id, quantity]) => initialProducts[id] !== quantity);

            const currentImage = combo.base64Image || "";
            const newImage = form.getFieldValue("base64Image") || value.base64Image || "";
            const imageChanged = currentImage !== newImage;

            const baseData: ComboUpdateForm = {
                name: value.name,
                description: value.description,
                price: value.price,
                categories: value.categories,
                types: value.types,
                // Incluir base64Image solo si cambió (permite eliminar la imagen enviando cadena vacía)
                ...(imageChanged ? { base64Image: newImage } : {}),
            };

            const updateData: ComboUpdateForm = hasProductChanges
                ? {
                    ...baseData,
                    ...(Object.keys(addProducts).length > 0 ? { addProducts } : {}),
                    ...(deleteProducts.length > 0 ? { deleteProducts } : {}),
                }
                : baseData;

            await onSave(updateData);
        },
        validators: {
            onSubmit: ComboUpdateSchema as unknown as FormValidateOrFn<ComboUpdateForm>,
        },
    });

    const [categoriesInput, setCategoriesInput] = useState(() => (combo.categories || []).join("; "));
    const [typesInput, setTypesInput] = useState(() => (combo.types || []).join("; "));

    const parseDelimitedInput = (inputValue: string) => {
        return inputValue
            .split(";")
            .map(item => item.trim())
            .filter(item => item.length > 0);
    };

    useEffect(() => {
        form.setFieldValue("name", combo.name || "");
        form.setFieldValue("description", combo.description || "");
        form.setFieldValue("price", combo.price && combo.price > 0 ? combo.price : 1);
        form.setFieldValue("categories", combo.categories || []);
        form.setFieldValue("types", combo.types || []);
        form.setFieldValue("productsIds", initialProducts);
        form.setFieldValue("base64Image", combo.base64Image || "");
        setCategoriesInput((combo.categories || []).join('; '));
        setTypesInput((combo.types || []).join('; '));
        setCurrentProducts(initialProducts);
        setImagePreview(combo.base64Image || null);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [combo.id]);

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const handleProductSelection = (products: Record<string, number>, selectedProductsData?: any[]) => {
        setCurrentProducts(products);
        form.setFieldValue("productsIds", products);

        const currentCategories = form.getFieldValue("categories");
        const currentTypes = form.getFieldValue("types");

        if (selectedProductsData && selectedProductsData.length > 0) {
            if (!currentCategories || currentCategories.length === 0) {
                const allCategories = selectedProductsData
                    .flatMap(p => p.categories || [])
                    .filter((cat, index, self) => self.indexOf(cat) === index) // únicos
                    .sort();
                form.setFieldValue("categories", allCategories);
                setCategoriesInput(allCategories.join('; '));
            }

            if (!currentTypes || currentTypes.length === 0) {
                const allTypes = selectedProductsData
                    .map(p => p.type)
                    .filter((type, index, self) => type && self.indexOf(type) === index) // únicos y no nulos
                    .sort();
                form.setFieldValue("types", allTypes);
                setTypesInput(allTypes.join('; '));
            }
        }
    };

    const handleImageChange = useCallback(async (
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
        const base64Value = result.base64 || "";
        field.handleChange(base64Value);

        form.setFieldValue("base64Image", base64Value);
    }, [form]);

    const handleRemoveImage = (field: { handleChange: (value: string) => void }) => {
        setImagePreview(null);
        field.handleChange("");
        form.setFieldValue("base64Image", "");
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

                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <form.Field
                        name="name"
                        children={(field) => (
                            <div className="space-y-2">
                                <Label htmlFor="name">Nombre *</Label>
                                <Input
                                    id="name"
                                    placeholder="Ej: Combo Familiar"
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
                                    placeholder="25.99"
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
                                placeholder="Descripción del combo..."
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

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <form.Field
                        name="categories"
                        children={(field) => (
                            <div className="space-y-2">
                                <Label htmlFor="categories">Categorías (separadas por punto y coma)</Label>
                                <Input
                                    id="categories"
                                    placeholder="Ej: Entrada; Bebida; Dulce"
                                    value={categoriesInput}
                                    onChange={(e) => {
                                        const inputValue = e.target.value;
                                        setCategoriesInput(inputValue);
                                        field.handleChange(parseDelimitedInput(inputValue));
                                    }}
                                    onBlur={() => {
                                        field.handleBlur();
                                        const normalized = parseDelimitedInput(categoriesInput).join('; ');
                                        setCategoriesInput(normalized);
                                    }}
                                />
                                {field.state.meta.errors.length > 0 && (
                                    <ErrorMessage error={field.state.meta.errors[0] || null} />
                                )}
                            </div>
                        )}
                    />

                    <form.Field
                        name="types"
                        children={(field) => (
                            <div className="space-y-2">
                                <Label htmlFor="types">Tipos</Label>
                                <Input
                                    id="types"
                                    placeholder="Ej: Vegetariano;Vegano;Carnes"
                                    value={typesInput}
                                    onChange={(e) => {
                                        const inputValue = e.target.value;
                                        setTypesInput(inputValue);
                                        field.handleChange(parseDelimitedInput(inputValue));
                                    }}
                                    onBlur={() => {
                                        field.handleBlur();
                                        const normalized = parseDelimitedInput(typesInput).join('; ');
                                        setTypesInput(normalized);
                                    }}
                                />
                                {field.state.meta.errors.length > 0 && (
                                    <ErrorMessage error={field.state.meta.errors[0] || null} />
                                )}
                            </div>
                        )}
                    />
                </div>

                <div className="space-y-2">
                    <Label>Productos *</Label>
                    <Button
                        type="button"
                        variant="outline"
                        onClick={() => setIsProductDialogOpen(true)}
                        className="w-full justify-start"
                    >
                        <Package className="mr-2 h-4 w-4" />
                        {Object.keys(form.state.values.productsIds ?? {}).length > 0
                            ? `${Object.keys(form.state.values.productsIds ?? {}).length} producto(s) seleccionado(s)`
                            : "Seleccionar productos"
                        }
                    </Button>
                    {form.state.errors.find((error: unknown): error is { path: string } => typeof error === 'object' && error !== null && 'path' in error && (error as { path: string }).path === 'productsIds') && (
                        <ErrorMessage error={form.state.errors.find((error: unknown): error is { path: string } => typeof error === 'object' && error !== null && 'path' in error && (error as { path: string }).path === 'productsIds') || null} />
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
                                    htmlFor={`base64Image-${field.name}`}
                                    className="absolute bottom-1 right-1 bg-primary text-primary-foreground rounded-full p-2 cursor-pointer hover:bg-primary/90 transition-colors shadow-lg"
                                    onClick={(e) => {
                                        e.preventDefault();
                                        fileInputRef.current?.click();
                                    }}
                                >
                                    <Upload className="w-4 h-4" />
                                </Label>

                                <Input
                                    ref={setFileInputRef}
                                    id={`base64Image-${field.name}`}
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
                            <div
                                className="mr-2 h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
                            Actualizando...
                        </>
                    ) : (
                        <>
                            <Save className="mr-2 h-4 w-4" />
                            Actualizar Combo
                        </>
                    )}
                </Button>
            </form>

            <ProductSelectionDialog
                isOpen={isProductDialogOpen}
                onClose={() => setIsProductDialogOpen(false)}
                onSave={handleProductSelection}
                initialProducts={currentProducts}
            />
        </div>
    );
}