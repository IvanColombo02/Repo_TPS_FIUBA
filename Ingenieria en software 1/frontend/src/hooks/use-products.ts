import { useCallback, useEffect, useMemo, useState } from "react";
import { useAccessTokenGetter } from "@/services/TokenContext";
import { useToast } from "@/hooks/use-toast";
import {
  type ProductDTO,
  type ProductCreateDTO,
  type ProductUpdateDTO,
  type ProductQueryParams,
  fetchProductsPage,
  fetchAllProducts,
  createProduct,
  updateProduct as updateProductAPI,
  deleteProduct as deleteProductAPI,
} from "@/lib/api/products";

const DEFAULT_PAGE_SIZE = 10;

type ProductQueryState = Omit<ProductQueryParams, "page" | "size">;

type FetchMode = "all" | "paginated";

export interface ProductsPaginationInfo {
  page: number;
  size: number;
  totalPages: number;
  totalElements: number;
  first: boolean;
  last: boolean;
}

export interface UseProductsOptions {
  mode?: FetchMode;
  pageSize?: number;
  initialPage?: number;
  initialQuery?: ProductQueryState;
  enabled?: boolean;
  resetOnDisable?: boolean;
}

export interface UseProductsReturn {
  products: ProductDTO[];
  loading: boolean;
  isSubmitting: boolean;
  loadProducts: (pageOverride?: number, queryOverride?: ProductQueryState) => Promise<void>;
  addProduct: (data: ProductCreateDTO) => Promise<void>;
  updateProduct: (id: number, data: ProductUpdateDTO) => Promise<void>;
  removeProduct: (id: number) => Promise<void>;
  recalculateProductStocks: (ingredientId?: number) => Promise<number[]>;
  pagination: ProductsPaginationInfo | null;
  setPage: (page: number) => Promise<void>;
  query: ProductQueryState;
  updateQuery: (updater: Partial<ProductQueryState> | ((prev: ProductQueryState) => ProductQueryState)) => Promise<void>;
  reset: () => void;
  mode: FetchMode;
}

function sanitizeProductQuery(query: ProductQueryState): ProductQueryState {
  const next: Record<string, unknown> = {};
  (Object.entries(query) as [keyof ProductQueryState, ProductQueryState[keyof ProductQueryState]][]).forEach(([key, value]) => {
    if (value === undefined || value === null) {
      return;
    }
    if (typeof value === "string" && value.trim() === "") {
      return;
    }
    if (Array.isArray(value) && value.length === 0) {
      return;
    }
    next[key as string] = value;
  });
  return next as ProductQueryState;
}

export function useProducts(options: UseProductsOptions = {}): UseProductsReturn {
  const mode: FetchMode = options.mode ?? "all";
  const pageSize = options.pageSize ?? DEFAULT_PAGE_SIZE;
  const initialPage = options.initialPage ?? 0;
  const initialQuery = useMemo(() => sanitizeProductQuery(options.initialQuery ?? {}), [options.initialQuery]);
  const enabled = options.enabled ?? true;
  const resetOnDisable = options.resetOnDisable ?? false;

  const getAccessToken = useAccessTokenGetter();
  const { toast } = useToast();
  const [products, setProducts] = useState<ProductDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [currentPage, setCurrentPage] = useState(initialPage);
  const [pagination, setPagination] = useState<ProductsPaginationInfo | null>(null);
  const [query, setQuery] = useState<ProductQueryState>(initialQuery);

  const loadProducts = useCallback(async (pageOverride?: number, queryOverride?: ProductQueryState) => {
    setLoading(true);
    try {
      const accessToken = await getAccessToken();
      const effectiveQuery = queryOverride ?? query;

      if (mode === "paginated") {
        const targetPage = typeof pageOverride === "number" ? pageOverride : currentPage;
        const response = await fetchProductsPage(accessToken, { ...effectiveQuery, page: targetPage, size: pageSize });

        setProducts(response.content);
        setPagination({
          page: response.number,
          size: response.size,
          totalPages: response.totalPages,
          totalElements: response.totalElements,
          first: response.first,
          last: response.last,
        });
        setCurrentPage(response.number);
      } else {
        const data = await fetchAllProducts(accessToken, effectiveQuery);
        setProducts(data);
        setPagination(null);
      }
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Error al cargar productos",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  }, [currentPage, getAccessToken, mode, pageSize, query, toast]);

  const setPage = useCallback(async (page: number) => {
    if (mode !== "paginated") {
      return;
    }
    if (pagination && page === pagination.page) {
      return;
    }
    await loadProducts(page);
  }, [loadProducts, mode, pagination]);

  const updateQuery = useCallback(async (updater: Partial<ProductQueryState> | ((prev: ProductQueryState) => ProductQueryState)) => {
    const nextState = typeof updater === "function" ? updater(query) : { ...query, ...updater };
    const sanitized = sanitizeProductQuery(nextState);

    setQuery(sanitized);

    if (mode === "paginated") {
      setCurrentPage(0);
      await loadProducts(0, sanitized);
    } else {
      await loadProducts(undefined, sanitized);
    }
  }, [loadProducts, mode, query]);

  const handleProductsChangedEvent = useCallback(() => {
    loadProducts().catch(() => { /* ignore */ });
  }, [loadProducts]);

  const addProduct = async (data: ProductCreateDTO) => {
    setIsSubmitting(true);
    try {
      const accessToken = await getAccessToken();
      const created = await createProduct(accessToken, { ...data });

      if (mode === "all") {
        setProducts(prev => [...prev, created]);
      } else {
        await loadProducts(0);
      }

      try {
        window.dispatchEvent(new CustomEvent("products:changed", { detail: { id: created.id } }));
      } catch {
        /* ignore */
      }

      toast({
        title: "Éxito",
        description: `Producto "${data.name}" creado correctamente`,
      });
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Error al crear producto",
        variant: "destructive",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const removeProduct = async (id: number) => {
    setIsSubmitting(true);
    try {
      const accessToken = await getAccessToken();
      await deleteProductAPI(accessToken, id);

      if (mode === "all") {
        setProducts(prev => prev.filter(product => product.id !== id));
      } else {
        const targetPage = pagination && pagination.page > 0 && products.length <= 1
          ? pagination.page - 1
          : pagination?.page ?? 0;
        await loadProducts(targetPage);
      }

      try {
        window.dispatchEvent(new CustomEvent("products:changed", { detail: { id } }));
      } catch {
        /* ignore */
      }

      toast({
        title: "Éxito",
        description: "Producto eliminado correctamente",
      });
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Error al eliminar producto",
        variant: "destructive",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const updateProduct = async (id: number, data: ProductUpdateDTO) => {
    setIsSubmitting(true);
    try {
      const accessToken = await getAccessToken();
      await updateProductAPI(accessToken, id, data);

      await loadProducts(mode === "paginated" ? currentPage : undefined);

      try {
        window.dispatchEvent(new CustomEvent("products:changed", { detail: { id } }));
      } catch {
        /* ignore */
      }

      toast({
        title: "Éxito",
        description: `Producto "${data.name}" actualizado correctamente`,
      });
    } catch (error) {
      console.error("updateProduct - error:", error);
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Error al actualizar producto",
        variant: "destructive",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const recalculateProductStocks = async (ingredientId?: number): Promise<number[]> => {
    try {
      const accessToken = await getAccessToken();
      const allProducts = await fetchAllProducts(accessToken);

      const productsToRecalculate = ingredientId
        ? allProducts.filter(product => {
          if (!product.ingredients) return false;
          const entries = Object.entries(product.ingredients as unknown as Record<string, number>);
          return entries.some(([ingredientKey]) => {
            const key = String(ingredientKey);

            if (/^\d+$/.test(key)) {
              return parseInt(key, 10) === ingredientId;
            }

            const idMatch = key.match(/id=(\d+)/);
            if (idMatch) {
              return parseInt(idMatch[1], 10) === ingredientId;
            }

            try {
              const parsed = JSON.parse(key);
              if (parsed && typeof parsed === "object" && "id" in parsed) {
                const parsedId = typeof parsed.id === "number" ? parsed.id : parseInt(String(parsed.id), 10);
                return parsedId === ingredientId;
              }
            } catch {
              console.warn(`No se pudo parsear la clave del ingrediente: ${key}`);
            }

            return false;
          });
        })
        : allProducts;

      const updatePromises = productsToRecalculate.map(async (product) => {
        try {
          const ingredientsIds: Record<string, number> = {};

          if (product.ingredients && typeof product.ingredients === "object") {
            const entries = Object.entries(product.ingredients as unknown as Record<string, number>);
            for (const [ingredientKey, quantity] of entries) {
              const key = String(ingredientKey);
              let id: number | null = null;

              if (/^\d+$/.test(key)) {
                id = parseInt(key, 10);
              } else {
                const idMatch = key.match(/id=(\d+)/);
                if (idMatch) {
                  id = parseInt(idMatch[1], 10);
                } else {
                  try {
                    const parsed = JSON.parse(key);
                    if (parsed && typeof parsed === "object" && "id" in parsed) {
                      id = typeof parsed.id === "number" ? parsed.id : parseInt(String(parsed.id), 10);
                    }
                  } catch {
                    console.warn(`No se pudo parsear la clave del ingrediente: ${key}`);
                  }
                }
              }

              if (id !== null && Number.isFinite(id)) {
                ingredientsIds[id.toString()] = quantity;
              }
            }
          }

          const updateData: ProductUpdateDTO = {
            name: product.name,
            description: product.description,
            price: product.price,
            categories: product.categories,
            type: product.type,
            estimatedTime: product.estimatedTime,
            ingredientsIds,
            base64Image: product.base64Image,
          };
          await updateProductAPI(accessToken, product.id, updateData);
          return product.id;
        } catch (error) {
          console.error(`Error al recalcular stock del producto ${product.id}:`, error);
          return null;
        }
      });

      const updatedProductIds = (await Promise.all(updatePromises)).filter((id): id is number => id !== null);
      await loadProducts(mode === "paginated" ? currentPage : undefined);
      return updatedProductIds;
    } catch (error) {
      console.error("Error al recalcular stocks de productos:", error);
      await loadProducts(mode === "paginated" ? currentPage : undefined);
      return [];
    }
  };

  const reset = useCallback(() => {
    setProducts([]);
    setPagination(null);
    setCurrentPage(initialPage);
  }, [initialPage]);

  useEffect(() => {
    if (enabled) {
      loadProducts().catch(() => { /* ignore */ });
    } else if (resetOnDisable) {
      reset();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [enabled, resetOnDisable, reset]);

  useEffect(() => {
    window.addEventListener("products:changed", handleProductsChangedEvent as EventListener);
    return () => {
      window.removeEventListener("products:changed", handleProductsChangedEvent as EventListener);
    };
  }, [handleProductsChangedEvent]);

  return {
    products,
    loading,
    isSubmitting,
    loadProducts,
    addProduct,
    updateProduct,
    removeProduct,
    recalculateProductStocks,
    pagination,
    setPage,
    query,
    updateQuery,
    reset,
    mode,
  };
}
