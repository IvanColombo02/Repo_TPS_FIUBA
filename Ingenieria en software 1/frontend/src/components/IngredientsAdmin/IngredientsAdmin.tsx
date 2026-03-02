import { useCallback, useState } from "react";
import { useAppForm } from "@/config/use-app-form";
import { IngredientCreateSchema, IngredientCreateForm } from "@/models/Ingredient";
import { FormValidateOrFn } from "@tanstack/react-form";
import { useIngredients } from "@/hooks/use-ingredients";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ErrorMessage } from "@/components/ui/error-message";
import { ItemsDropdown, type Item } from "@/components/ui/ItemsDropdown";
import { EditItemDialog } from "@/components/ui/EditItemDialog";
import { Plus, Loader2 } from "lucide-react";
import { PaginationToolbar } from "@/components/ui/PaginationToolbar";
import { Upload, X, Image as ImageIcon } from "lucide-react";
import { validateAndConvertImage } from "@/lib/utils/image-utils";


interface IngredientsAdminProps {
  onIngredientStockChange?: () => void;
}

export function IngredientsAdmin({ onIngredientStockChange }: IngredientsAdminProps) {
  const {
    ingredients,
    loading,
    isSubmitting,
    addIngredient,
    updateIngredient,
    removeIngredient,
    pagination,
    setPage,
    updateQuery,
  } = useIngredients(onIngredientStockChange, { mode: "paginated", pageSize: 10 });
  const [editingIngredient, setEditingIngredient] = useState<{ id: number; name: string; stock: number; base64Image?: string } | null>(null);
  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const [imageError, setImageError] = useState<string | null>(null);

  const handleImageChange = async (
    e: React.ChangeEvent<HTMLInputElement>,
    field: { handleChange: (value: string) => void }
  ) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const result = await validateAndConvertImage(file);

    if (!result.valid) {
      setImageError(result.error || "Imagen inválida");
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

  const form = useAppForm({
    defaultValues: {
      name: "",
      stock: 0,
      base64Image: "",
    } as IngredientCreateForm,
    onSubmit: async ({ value }) => {
      await addIngredient({
        name: value.name,
        stock: value.stock,
        base64Image: value.base64Image,
      });
      form.reset();
      setImagePreview(null);
      setImageError(null);
    },
    validators: {
      onSubmit: IngredientCreateSchema as unknown as FormValidateOrFn<IngredientCreateForm>,
    },
  });

  const handleEdit = (item: Item) => {
    setEditingIngredient({
      id: item.id,
      name: item.name,
      stock: item.stock || 0,
      base64Image: item.base64Image
    });
  };

  const handleSaveEdit = async (data: { name: string; stock: number; base64Image?: string }) => {
    if (editingIngredient) {
      await updateIngredient(editingIngredient.id, data);
      setEditingIngredient(null);
    }
  };

  const handleCloseEdit = () => {
    setEditingIngredient(null);
  };

  const handleSearch = useCallback(async (value: string) => {
    await updateQuery({ name: value.trim() || undefined });
  }, [updateQuery]);

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader className="pb-4">
          <CardTitle className="text-lg">Nuevo Ingrediente</CardTitle>
        </CardHeader>
        <CardContent>
          <form
            onSubmit={(e) => {
              e.preventDefault();
              e.stopPropagation();
              form.handleSubmit?.(e);
            }}
            className="flex gap-4 items-end"
          >
            <div className="flex-1">
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
            </div>

            <div className="w-24">
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
            </div>

            <div className="flex flex-col items-center">
              <form.Field
                name="base64Image"
                children={(field) => (
                  <div className="flex flex-col items-center space-y-2">

                    <div className="relative">
                      <div
                        className="w-20 h-20 rounded-full border-2 border-border overflow-hidden bg-muted flex items-center justify-center">
                        {imagePreview ? (
                          <img
                            src={imagePreview}
                            alt="preview"
                            className="w-full h-full object-cover"
                          />
                        ) : (
                          <ImageIcon className="w-8 h-8 text-muted-foreground" />
                        )}
                      </div>

                      {imagePreview && (
                        <button
                          type="button"
                          onClick={() => handleRemoveImage(field)}
                          className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full p-1"
                        >
                          <X className="w-3 h-3" />
                        </button>
                      )}

                      <Label
                        htmlFor="ingredient-image-upload"
                        className="absolute bottom-0 right-0 bg-primary text-primary-foreground rounded-full p-1 cursor-pointer"
                      >
                        <Upload className="w-3 h-3" />
                      </Label>

                      <Input
                        id="ingredient-image-upload"
                        type="file"
                        accept="image/*"
                        className="hidden"
                        onChange={(e) => handleImageChange(e, field)}
                      />
                    </div>

                    {imageError && (
                      <p className="text-xs text-red-500">{imageError}</p>
                    )}
                  </div>
                )}
              />
            </div>


            <Button
              type="submit"
              disabled={isSubmitting || !form.state.isValid}
              className="min-w-[120px]"
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Creando...
                </>
              ) : (
                <>
                  <Plus className="mr-2 h-4 w-4" />
                  Crear
                </>
              )}
            </Button>
          </form>
        </CardContent>
      </Card>

      <ItemsDropdown
        title="Ingredientes"
        items={ingredients}
        loading={loading}
        searchable={true}
        searchPlaceholder="Buscar ingredientes por nombre..."
        onSearch={handleSearch}
        onEdit={handleEdit}
        onDelete={removeIngredient}
        isDeleting={isSubmitting}
        emptyMessage="No hay ingredientes creados aún."
        totalItems={pagination?.totalElements}
        renderItem={(item) => (
          <div className="flex items-center gap-4">
            {item.base64Image && (
              <img src={item.base64Image} alt={item.name} loading="lazy" decoding="async" className="w-10 h-10 object-cover rounded-md" />
            )}
            <div>
              <span className="font-medium text-white">{item.name}</span>
              <span className="text-sm text-white/70 ml-2">
                Stock: {item.stock}
              </span>
            </div>
          </div>
        )}
      />

      {pagination && pagination.totalElements > 0 && (
        <PaginationToolbar
          page={pagination.page}
          size={pagination.size}
          totalPages={pagination.totalPages}
          totalElements={pagination.totalElements}
          onPageChange={setPage}
          disabled={loading || isSubmitting}
          className="bg-background"
        />
      )}

      <EditItemDialog
        isOpen={editingIngredient !== null}
        onClose={handleCloseEdit}
        onSave={handleSaveEdit}
        initialData={editingIngredient || undefined}
        title="Editar Ingrediente"
        isSubmitting={isSubmitting}
      />
    </div>
  );
}
