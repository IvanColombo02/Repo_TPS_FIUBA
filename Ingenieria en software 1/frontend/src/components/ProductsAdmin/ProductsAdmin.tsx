import { ProductCreateForm, ProductUpdateForm } from "@/models/Product";
import { useProducts } from "@/hooks/use-products";
import { fetchProductsSimplePage, fetchProductById, type ProductSimpleDTO } from "@/lib/api/products";
import { useToken } from "@/services/TokenContext";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ItemsDropdown } from "@/components/ui/ItemsDropdown";
import { ProductCreateFormComponent } from "./ProductCreateForm";
import { ProductEditFormComponent } from "./ProductEditForm";
import { useCallback, useEffect, useState } from "react";
import { PaginationToolbar } from "@/components/ui/PaginationToolbar";

interface EditableProduct {
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
}

interface ProductsAdminProps {
  onRefreshRef?: React.MutableRefObject<((ingredientId?: number) => Promise<number[]>) | null>;
  onProductUpdate?: (productIds?: number[]) => void;
}

export function ProductsAdmin({ onRefreshRef, onProductUpdate }: ProductsAdminProps) {
  const {
    products,
    loading,
    isSubmitting,
    addProduct,
    removeProduct,
    updateProduct,
    recalculateProductStocks,
    pagination,
    setPage,
    updateQuery,
  } = useProducts({ mode: "paginated", pageSize: 10 });

  useEffect(() => {
    if (onRefreshRef) {
      onRefreshRef.current = recalculateProductStocks;
    }
    return () => {
      if (onRefreshRef) {
        onRefreshRef.current = null;
      }
    };
  }, [onRefreshRef, recalculateProductStocks]);

  const [tokenState] = useToken();
  const accessToken = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : null;
  const [simpleProducts, setSimpleProducts] = useState<ProductSimpleDTO[]>([]);
  const [simpleLoading, setSimpleLoading] = useState(false);
  const [simplePagination, setSimplePagination] = useState<{ page: number; size: number; totalElements: number; totalPages: number } | null>(null);

  const loadSimpleProducts = useCallback(async (page = 0) => {
    if (!accessToken) return;
    setSimpleLoading(true);
    try {
      const resp = await fetchProductsSimplePage(accessToken, { page, size: 10 });
      setSimpleProducts(resp.content);
      setSimplePagination({ page: resp.number, size: resp.size, totalElements: resp.totalElements, totalPages: resp.totalPages });
    } catch (e) {
      console.error('Error loading simple products:', e);
      setSimpleProducts([]);
      setSimplePagination(null);
    } finally {
      setSimpleLoading(false);
    }
  }, [accessToken]);

  useEffect(() => {
    loadSimpleProducts(0);
  }, [accessToken, loadSimpleProducts]);

  const [editingProduct, setEditingProduct] = useState<EditableProduct | null>(null);

  useEffect(() => {
    if (editingProduct) {
      const updatedProduct = products.find(p => p.id === editingProduct.id);
      if (updatedProduct && updatedProduct.stock !== editingProduct.stock) {
        setEditingProduct(updatedProduct as EditableProduct);
      }
    }
  }, [products, editingProduct]);

  const handleCreateProduct = async (data: ProductCreateForm) => {
    await addProduct({
      ...data,
      base64Image: data.base64Image || "",
    });
  };

  const handleEditProduct = async (data: ProductUpdateForm) => {
    if (editingProduct) {
      await updateProduct(editingProduct.id, data);
      const productId = editingProduct.id;
      setEditingProduct(null);
      if (onProductUpdate) {
        onProductUpdate([productId]);
      }
    }
  };

  const handleSearch = useCallback(async (value: string) => {
    const q = value.trim() || undefined;
    await updateQuery({ name: q });
    await loadSimpleProducts(0);
  }, [updateQuery, loadSimpleProducts]);

  return (
    <div className="space-y-6">
      {!editingProduct && (
        <Card>
          <CardHeader className="pb-4">
            <CardTitle className="text-lg">Nuevo Producto</CardTitle>
          </CardHeader>
          <CardContent>
            <ProductCreateFormComponent
              onSave={handleCreateProduct}
              isSubmitting={isSubmitting}
            />
          </CardContent>
        </Card>
      )}

      {editingProduct && (
        <Card data-edit-form>
          <CardHeader className="pb-4">
            <CardTitle className="text-lg">Editar Producto: {editingProduct.name}</CardTitle>
          </CardHeader>
          <CardContent>
            <ProductEditFormComponent
              product={editingProduct}
              onSave={handleEditProduct}
              isSubmitting={isSubmitting}
            />
            <div className="mt-4">
              <button
                onClick={() => {
                  setEditingProduct(null);

                  window.scrollTo({ top: 0, behavior: 'smooth' });
                }}
                className="text-sm text-gray-500 hover:text-gray-700"
              >
                ← Volver a crear producto
              </button>
            </div>
          </CardContent>
        </Card>
      )}

      <ItemsDropdown
        title="Productos"
        items={simpleProducts}
        loading={loading || simpleLoading}
        searchable={true}
        searchPlaceholder="Buscar productos por nombre..."
        onSearch={handleSearch}
        onEdit={(product) => {
          try {
            (async () => {
              if (!accessToken) return;
              const full = await fetchProductById(accessToken, product.id);
              setEditingProduct(full as unknown as EditableProduct);
            })();

            setTimeout(() => {
              const editForm = document.querySelector('[data-edit-form]');
              if (editForm) {
                editForm.scrollIntoView({ behavior: 'smooth', block: 'start' });
              }
            }, 100);
          } catch (error) {
            console.error('Error al editar producto:', error);
          }
        }}
        onDelete={async (id) => { await removeProduct(id); await loadSimpleProducts(simplePagination?.page ?? 0); }}
        isDeleting={isSubmitting}
        emptyMessage="No hay productos creados aún."
        renderItem={(item) => (
          <div className="flex items-center gap-4">
            {item.base64Image && (
              <img src={item.base64Image} alt={item.name} loading="lazy" decoding="async" className="w-10 h-10 object-cover rounded-md" />
            )}
            <div>
              <span className="font-medium text-white">{item.name}</span>
              <span className="text-sm text-white/70 ml-2">
                ${item.price} - Stock: {item.stock}
              </span>
            </div>
          </div>
        )}
        totalItems={simplePagination?.totalElements ?? pagination?.totalElements}
      />

      {((pagination && pagination.totalElements > 0) || (simplePagination && simplePagination.totalElements > 0)) && (
        <PaginationToolbar
          page={pagination?.page ?? simplePagination?.page ?? 0}
          size={pagination?.size ?? simplePagination?.size ?? 10}
          totalPages={pagination?.totalPages ?? simplePagination?.totalPages ?? 0}
          totalElements={pagination?.totalElements ?? simplePagination?.totalElements ?? 0}
          onPageChange={async (page) => { await loadSimpleProducts(page); await setPage(page) }}
          disabled={loading || isSubmitting || simpleLoading}
          className="bg-background"
        />
      )}

    </div>
  );
}