import { useCallback, useEffect, useMemo, useState } from "react";
import { useAccessTokenGetter } from "@/services/TokenContext";
import { useToast } from "@/hooks/use-toast";
import {
  type IngredientDTO,
  type IngredientCreateDTO,
  type IngredientUpdateDTO,
  type IngredientQueryParams,
  fetchIngredientsPage,
  fetchIngredients,
  createIngredient,
  updateIngredient as updateIngredientAPI,
  deleteIngredient as deleteIngredientAPI,
  updateIngredientStock as updateIngredientStockAPI
} from "@/lib/api/ingredients";
import { fetchAllProducts } from "@/lib/api/products";

export interface UseIngredientsReturn {
  ingredients: IngredientDTO[];
  loading: boolean;
  isSubmitting: boolean;
  loadIngredients: (pageOverride?: number, queryOverride?: IngredientQueryState) => Promise<void>;
  addIngredient: (data: { name: string; stock: number; base64Image?: string }) => Promise<void>;
  updateIngredient: (id: number, data: { name: string; stock: number; base64Image?: string }) => Promise<void>;
  removeIngredient: (id: number) => Promise<void>;
  updateIngredientStock: (id: number, stock: number) => Promise<void>;
  pagination: IngredientsPaginationInfo | null;
  setPage: (page: number) => Promise<void>;
  query: IngredientQueryState;
  updateQuery: (updater: Partial<IngredientQueryState> | ((prev: IngredientQueryState) => IngredientQueryState)) => Promise<void>;
  reset: () => void;
  mode: FetchMode;
}

type IngredientQueryState = Omit<IngredientQueryParams, "page" | "size">;

type FetchMode = "all" | "paginated";

export interface UseIngredientsOptions {
  mode?: FetchMode;
  pageSize?: number;
  initialPage?: number;
  initialQuery?: IngredientQueryState;
  enabled?: boolean;
  resetOnDisable?: boolean;
}

export interface IngredientsPaginationInfo {
  page: number;
  size: number;
  totalPages: number;
  totalElements: number;
  first: boolean;
  last: boolean;
}

function sanitizeIngredientQuery(query: IngredientQueryState): IngredientQueryState {
  const next: Record<string, unknown> = {};
  (Object.entries(query) as [keyof IngredientQueryState, IngredientQueryState[keyof IngredientQueryState]][]).forEach(([key, value]) => {
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
  return next as IngredientQueryState;
}

export function useIngredients(
  onStockChange?: (ingredientId?: number) => void,
  options: UseIngredientsOptions = {}
): UseIngredientsReturn {
  const mode: FetchMode = options.mode ?? "all";
  const pageSize = options.pageSize ?? 10;
  const initialPage = options.initialPage ?? 0;
  const initialQuery = useMemo(() => sanitizeIngredientQuery(options.initialQuery ?? {}), [options.initialQuery]);
  const enabled = options.enabled ?? true;
  const resetOnDisable = options.resetOnDisable ?? false;

  const getAccessToken = useAccessTokenGetter();
  const { toast } = useToast();
  const [ingredients, setIngredients] = useState<IngredientDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [currentPage, setCurrentPage] = useState(initialPage);
  const [pagination, setPagination] = useState<IngredientsPaginationInfo | null>(null);
  const [query, setQuery] = useState<IngredientQueryState>(initialQuery);

  const loadIngredients = useCallback(async (pageOverride?: number, queryOverride?: IngredientQueryState) => {
    setLoading(true);
    try {
      const accessToken = await getAccessToken();
      const effectiveQuery = queryOverride ?? query;

      if (mode === "paginated") {
        const targetPage = typeof pageOverride === "number" ? pageOverride : currentPage;
        const response = await fetchIngredientsPage(accessToken, { ...effectiveQuery, page: targetPage, size: pageSize });

        setIngredients(response.content);
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
        const data = await fetchIngredients(accessToken, effectiveQuery);
        setIngredients(data);
        setPagination(null);
      }
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Error al cargar ingredientes",
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
    await loadIngredients(page);
  }, [loadIngredients, mode, pagination]);

  const updateQuery = useCallback(async (updater: Partial<IngredientQueryState> | ((prev: IngredientQueryState) => IngredientQueryState)) => {
    const nextState = typeof updater === "function" ? updater(query) : { ...query, ...updater };
    const sanitized = sanitizeIngredientQuery(nextState);

    setQuery(sanitized);

    if (mode === "paginated") {
      setCurrentPage(0);
      await loadIngredients(0, sanitized);
    } else {
      await loadIngredients(undefined, sanitized);
    }
  }, [loadIngredients, mode, query]);

  const handleIngredientsChangedEvent = useCallback(() => {
    loadIngredients().catch(() => { /* ignore */ });
  }, [loadIngredients]);

  const addIngredient = async (data: { name: string; stock: number; base64Image?: string }) => {
    setIsSubmitting(true);
    try {
      const accessToken = await getAccessToken();
      const newIngredient: IngredientCreateDTO = {
        name: data.name,
        stock: data.stock,
        base64Image: data.base64Image || ''
      };
      const created = await createIngredient(accessToken, newIngredient);

      if (mode === "all") {
        setIngredients(prev => [...prev, created]);
      } else {
        await loadIngredients(0);
      }

      try {
        window.dispatchEvent(new CustomEvent('ingredients:changed', { detail: { id: created.id } }));
      } catch {
        // Ignore event dispatch errors
      }
      if (onStockChange && data.stock > 0) onStockChange(created.id);
      toast({
        title: "Éxito",
        description: `Ingrediente "${data.name}" creado correctamente`,
      });
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Error al crear ingrediente",
        variant: "destructive",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const updateIngredient = async (id: number, data: { name: string; stock: number; base64Image?: string }) => {
    setIsSubmitting(true);
    try {
      const accessToken = await getAccessToken();
      const current = ingredients.find(ing => ing.id === id);

      const updateData: IngredientUpdateDTO = {
        name: data.name,
        ...(data.base64Image !== undefined && { base64Image: data.base64Image })
      } as IngredientUpdateDTO;
      const updatedBase = await updateIngredientAPI(accessToken, id, updateData);

      if (typeof data.stock === 'number' && current && data.stock !== current.stock) {
        const delta = data.stock - current.stock;
        const updatedWithStock = await updateIngredientStockAPI(accessToken, id, { stock: delta });

        if (mode === "all") {
          setIngredients(prev => prev.map(ingredient =>
            ingredient.id === id ? updatedWithStock : ingredient
          ));
        } else {
          await loadIngredients(currentPage);
        }

        try {
          window.dispatchEvent(new CustomEvent('ingredients:changed', { detail: { id } }));
        } catch {
          // Ignore event dispatch errors
        }
        if (onStockChange) onStockChange(id);
      } else {
        if (mode === "all") {
          setIngredients(prev => prev.map(ingredient =>
            ingredient.id === id ? updatedBase : ingredient
          ));
        } else {
          await loadIngredients(currentPage);
        }

        try {
          window.dispatchEvent(new CustomEvent('ingredients:changed', { detail: { id } }));
        } catch {
          // Ignore event dispatch errors
        }
      }
      toast({
        title: "Éxito",
        description: `Ingrediente "${data.name}" actualizado correctamente`,
      });
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Error al actualizar ingrediente",
        variant: "destructive",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const removeIngredient = async (id: number) => {
    setIsSubmitting(true);
    try {
      const accessToken = await getAccessToken();
      await deleteIngredientAPI(accessToken, id);

      if (mode === "all") {
        setIngredients(prev => prev.filter(ing => ing.id !== id));
      } else {
        const targetPage = pagination && pagination.page > 0 && ingredients.length <= 1
          ? pagination.page - 1
          : pagination?.page ?? 0;
        await loadIngredients(targetPage);
      }
      try {
        window.dispatchEvent(new CustomEvent('ingredients:changed', { detail: { id } }));
      } catch {
        // Ignore event dispatch errors
      }
      toast({
        title: "Éxito",
        description: "Ingrediente eliminado correctamente",
      });
    } catch (error) {
      const message = error instanceof Error ? error.message : "Error al eliminar ingrediente";
      try {
        const accessToken = await getAccessToken();
        const products = await fetchAllProducts(accessToken);
        const affected: string[] = [];
        for (const p of products) {
          if (!p.ingredients) continue;
          const entries = Object.entries(p.ingredients as unknown as Record<string, number>);
          const ingredientIds = entries.map(([k]) => {
            if (/^\d+$/.test(k)) return parseInt(k, 10);
            const m = k.match(/id=(\d+)/);
            return m ? parseInt(m[1], 10) : NaN;
          }).filter(n => Number.isFinite(n)) as number[];
          const uniqueIds = Array.from(new Set(ingredientIds));
          if (uniqueIds.length === 1 && uniqueIds[0] === id) {
            affected.push(p.name);
          }
        }
        const extra = affected.length > 0 ? `\nUsado por: ${affected.join(', ')}` : '';
        toast({
          title: "No se puede eliminar el ingrediente",
          description: `${message}${extra}`,
          variant: "destructive",
        });
      } catch {
        toast({
          title: "No se puede eliminar el ingrediente",
          description: message,
          variant: "destructive",
        });
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const updateIngredientStock = async (id: number, stockDelta: number) => {
    setIsSubmitting(true);
    try {
      const accessToken = await getAccessToken();

      const updated = await updateIngredientStockAPI(accessToken, id, { stock: stockDelta });
      if (mode === "all") {
        setIngredients(prev => prev.map(ingredient =>
          ingredient.id === id ? updated : ingredient
        ));
      } else {
        await loadIngredients(currentPage);
      }
      try {
        window.dispatchEvent(new CustomEvent('ingredients:changed', { detail: { id } }));
      } catch {
        // Ignore event dispatch errors
      }
      if (onStockChange) onStockChange(id);
      toast({
        title: "Éxito",
        description: `Stock del ingrediente actualizado (${stockDelta > 0 ? "+" : ""}${stockDelta})`,
      });
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Error al actualizar stock del ingrediente",
        variant: "destructive",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  useEffect(() => {
    window.addEventListener('ingredients:changed', handleIngredientsChangedEvent as EventListener);
    return () => {
      window.removeEventListener('ingredients:changed', handleIngredientsChangedEvent as EventListener);
    };
  }, [handleIngredientsChangedEvent]);

  const reset = useCallback(() => {
    setIngredients([]);
    setPagination(null);
    setCurrentPage(initialPage);
  }, [initialPage]);

  useEffect(() => {
    if (enabled) {
      loadIngredients().catch(() => { /* ignore */ });
    } else if (resetOnDisable) {
      reset();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [enabled, resetOnDisable, reset]);

  return {
    ingredients,
    loading,
    isSubmitting,
    loadIngredients,
    addIngredient,
    updateIngredient,
    removeIngredient,
    updateIngredientStock,
    pagination,
    setPage,
    query,
    updateQuery,
    reset,
    mode,
  };
}
