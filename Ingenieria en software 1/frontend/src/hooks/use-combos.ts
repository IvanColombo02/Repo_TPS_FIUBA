import { useCallback, useEffect, useMemo, useState } from "react";
import { useAccessTokenGetter } from "@/services/TokenContext";
import { useToast } from "@/hooks/use-toast";
import {
  type Combo,
  type ComboCreateForm,
  type ComboUpdateForm,
} from "@/models/Combo";
import {
  type ComboQueryParams,
  fetchCombosPage,
  fetchCombos,
  createCombo,
  updateCombo as updateComboAPI,
  deleteCombo,
  type ComboUpdateData,
} from "@/lib/api/combos";

const DEFAULT_PAGE_SIZE = 10;

type ComboQueryState = Omit<ComboQueryParams, "page" | "size">;

type FetchMode = "all" | "paginated";

export interface CombosPaginationInfo {
  page: number;
  size: number;
  totalPages: number;
  totalElements: number;
  first: boolean;
  last: boolean;
}

export interface UseCombosOptions {
  mode?: FetchMode;
  pageSize?: number;
  initialPage?: number;
  initialQuery?: ComboQueryState;
  enabled?: boolean;
  resetOnDisable?: boolean;
}

export interface UseCombosReturn {
  combos: Combo[];
  loading: boolean;
  isSubmitting: boolean;
  loadCombos: (pageOverride?: number, queryOverride?: ComboQueryState) => Promise<void>;
  addCombo: (data: ComboCreateForm) => Promise<void>;
  updateCombo: (id: number, data: ComboUpdateForm) => Promise<void>;
  removeCombo: (id: number) => Promise<void>;
  recalculateComboStocks: (productIds?: number[]) => Promise<void>;
  pagination: CombosPaginationInfo | null;
  setPage: (page: number) => Promise<void>;
  query: ComboQueryState;
  updateQuery: (updater: Partial<ComboQueryState> | ((prev: ComboQueryState) => ComboQueryState)) => Promise<void>;
  reset: () => void;
  mode: FetchMode;
}

function sanitizeComboQuery(query: ComboQueryState): ComboQueryState {
  const next: Record<string, unknown> = {};
  (Object.entries(query) as [keyof ComboQueryState, ComboQueryState[keyof ComboQueryState]][]).forEach(([key, value]) => {
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
  return next as ComboQueryState;
}

export function useCombos(options: UseCombosOptions = {}): UseCombosReturn {
  const mode: FetchMode = options.mode ?? "all";
  const pageSize = options.pageSize ?? DEFAULT_PAGE_SIZE;
  const initialPage = options.initialPage ?? 0;
  const initialQuery = useMemo(() => sanitizeComboQuery(options.initialQuery ?? {}), [options.initialQuery]);
  const enabled = options.enabled ?? true;
  const resetOnDisable = options.resetOnDisable ?? false;

  const getAccessToken = useAccessTokenGetter();
  const { toast } = useToast();
  const [combos, setCombos] = useState<Combo[]>([]);
  const [loading, setLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [currentPage, setCurrentPage] = useState(initialPage);
  const [pagination, setPagination] = useState<CombosPaginationInfo | null>(null);
  const [query, setQuery] = useState<ComboQueryState>(initialQuery);

  const loadCombos = useCallback(async (pageOverride?: number, queryOverride?: ComboQueryState) => {
    setLoading(true);
    try {
      const accessToken = await getAccessToken();
      const effectiveQuery = queryOverride ?? query;

      if (mode === "paginated") {
        const targetPage = typeof pageOverride === "number" ? pageOverride : currentPage;
        const response = await fetchCombosPage(accessToken, { ...effectiveQuery, page: targetPage, size: pageSize });

        setCombos(response.content);
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
        const data = await fetchCombos(accessToken, effectiveQuery);
        setCombos(data);
        setPagination(null);
      }
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Error al cargar combos",
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
    await loadCombos(page);
  }, [loadCombos, mode, pagination]);

  const updateQuery = useCallback(async (updater: Partial<ComboQueryState> | ((prev: ComboQueryState) => ComboQueryState)) => {
    const nextState = typeof updater === "function" ? updater(query) : { ...query, ...updater };
    const sanitized = sanitizeComboQuery(nextState);

    setQuery(sanitized);

    if (mode === "paginated") {
      setCurrentPage(0);
      await loadCombos(0, sanitized);
    } else {
      await loadCombos(undefined, sanitized);
    }
  }, [loadCombos, mode, query]);

  const addCombo = async (data: ComboCreateForm) => {
    setIsSubmitting(true);
    try {
      const accessToken = await getAccessToken();
      const created = await createCombo(accessToken, data);

      if (mode === "all") {
        setCombos(prev => [...prev, created]);
      } else {
        await loadCombos(0);
      }

      toast({
        title: "Combo creado",
        description: "El combo se ha creado exitosamente.",
      });
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Error al crear combo",
        variant: "destructive",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const updateCombo = async (id: number, data: ComboUpdateForm) => {
    setIsSubmitting(true);
    try {
      const accessToken = await getAccessToken();
      await updateComboAPI(accessToken, id, data as unknown as ComboUpdateData);

      await loadCombos(mode === "paginated" ? currentPage : undefined);

      toast({
        title: "Combo actualizado",
        description: "El combo se ha actualizado exitosamente.",
      });
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Error al actualizar combo",
        variant: "destructive",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const removeCombo = async (id: number) => {
    setIsSubmitting(true);
    try {
      const accessToken = await getAccessToken();
      await deleteCombo(accessToken, id);

      if (mode === "all") {
        setCombos(prev => prev.filter(combo => combo.id !== id));
      } else {
        const targetPage = pagination && pagination.page > 0 && combos.length <= 1
          ? pagination.page - 1
          : pagination?.page ?? 0;
        await loadCombos(targetPage);
      }

      toast({
        title: "Combo eliminado",
        description: "El combo se ha eliminado exitosamente.",
      });
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Error al eliminar combo",
        variant: "destructive",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const recalculateComboStocks = async (productIds?: number[]) => {
    try {
      const accessToken = await getAccessToken();

      const currentCombos = await fetchCombos(accessToken);
      const combosToRecalculate = productIds && productIds.length > 0
        ? currentCombos.filter(combo =>
          Array.isArray(combo.products) &&
          combo.products.some(product => productIds.includes(product.id))
        )
        : currentCombos;

      const updatePromises = combosToRecalculate.map(combo =>
        updateComboAPI(accessToken, combo.id, { description: combo.description })
          .catch(error => {
            console.error(`Error al recalcular stock del combo ${combo.id}:`, error);
            return null;
          })
      );

      await Promise.all(updatePromises);
      await loadCombos(mode === "paginated" ? currentPage : undefined);
    } catch (error) {
      console.error('Error al recalcular stocks de combos:', error);
      await loadCombos(mode === "paginated" ? currentPage : undefined);
    }
  };

  const reset = useCallback(() => {
    setCombos([]);
    setPagination(null);
    setCurrentPage(initialPage);
  }, [initialPage]);

  useEffect(() => {
    if (enabled) {
      loadCombos().catch(() => { /* ignore */ });
    } else if (resetOnDisable) {
      reset();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [enabled, resetOnDisable, reset]);

  return {
    combos,
    loading,
    isSubmitting,
    loadCombos,
    addCombo,
    updateCombo,
    removeCombo,
    recalculateComboStocks,
    pagination,
    setPage,
    query,
    updateQuery,
    reset,
    mode,
  };
}
