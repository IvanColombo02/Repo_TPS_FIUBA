import { useCallback, useEffect, useMemo, useState } from "react"
import { useAccessTokenGetter, useToken } from "@/services/TokenContext"
import { useToast } from "@/hooks/use-toast"
import {
  type PromotionDTO,
  type PromotionCreateDTO,
  type PromotionUpdateDTO,
  type PromotionQueryParams,
  fetchPromotionsPage,
  fetchAllPromotions,
  createPromotion,
  updatePromotion as updatePromotionAPI,
  deletePromotion as deletePromotionAPI,
  fetchActivePromotions,
  updatePromotionPriorities,
  type PromotionPriorityUpdateDTO,
} from "@/lib/api/promotions"

const DEFAULT_PAGE_SIZE = 10

type PromotionQueryState = Omit<PromotionQueryParams, "page" | "size">

type FetchMode = "all" | "paginated"

export interface PromotionsPaginationInfo {
  page: number
  size: number
  totalPages: number
  totalElements: number
  first: boolean
  last: boolean
}

export interface UsePromotionsOptions {
  mode?: FetchMode
  pageSize?: number
  initialPage?: number
  initialQuery?: PromotionQueryState
}

export interface UsePromotionsReturn {
  promotions: PromotionDTO[]
  loading: boolean
  isSubmitting: boolean
  loadPromotions: (pageOverride?: number, queryOverride?: PromotionQueryState) => Promise<void>
  addPromotion: (data: PromotionCreateDTO) => Promise<void>
  updatePromotion: (id: number, data: PromotionUpdateDTO) => Promise<void>
  removePromotion: (id: number) => Promise<void>
  pagination: PromotionsPaginationInfo | null
  setPage: (page: number) => Promise<void>
  query: PromotionQueryState
  updateQuery: (updater: Partial<PromotionQueryState> | ((prev: PromotionQueryState) => PromotionQueryState)) => Promise<void>
  mode: FetchMode
  getActivePromotions: () => Promise<PromotionDTO[]>
  loadPrioritizablePromotions: () => Promise<PromotionDTO[]>
  reorderPromotions: (orderedIds: number[]) => Promise<PromotionDTO[]>
}

function sanitizePromotionQuery(query: PromotionQueryState): PromotionQueryState {
  const next: Record<string, unknown> = {}
  Object.entries(query).forEach(([key, value]) => {
    if (value === undefined || value === null) {
      return
    }
    if (typeof value === "string" && value.trim() === "") {
      return
    }
    next[key] = value
  })
  return next as PromotionQueryState
}

export function usePromotions(options: UsePromotionsOptions = {}): UsePromotionsReturn {
  const mode: FetchMode = options.mode ?? "all"
  const pageSize = options.pageSize ?? DEFAULT_PAGE_SIZE
  const initialPage = options.initialPage ?? 0
  const initialQuery = useMemo(() => sanitizePromotionQuery(options.initialQuery ?? {}), [options.initialQuery])

  const [tokenState] = useToken()
  const getAccessToken = useAccessTokenGetter()
  const { toast } = useToast()
  const [promotions, setPromotions] = useState<PromotionDTO[]>([])
  const [loading, setLoading] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [currentPage, setCurrentPage] = useState(initialPage)
  const [pagination, setPagination] = useState<PromotionsPaginationInfo | null>(null)
  const [query, setQuery] = useState<PromotionQueryState>(initialQuery)
  
  const isAuthenticated = tokenState.state === "LOGGED_IN"

  const loadPromotions = useCallback(async (pageOverride?: number, queryOverride?: PromotionQueryState) => {
    if (!isAuthenticated) {
      setLoading(false)
      return
    }
    
    setLoading(true)
    try {
      const accessToken = await getAccessToken()
      const effectiveQuery = queryOverride ?? query

      if (mode === "paginated") {
        const targetPage = typeof pageOverride === "number" ? pageOverride : currentPage
        const response = await fetchPromotionsPage(accessToken, {
          ...effectiveQuery,
          page: targetPage,
          size: pageSize,
        })

        setPromotions(response.content)
        setPagination({
          page: response.number,
          size: response.size,
          totalPages: response.totalPages,
          totalElements: response.totalElements,
          first: response.first,
          last: response.last,
        })
        setCurrentPage(targetPage)
      } else {
        const allPromotions = await fetchAllPromotions(accessToken, effectiveQuery)
        setPromotions(allPromotions)
        setPagination(null)
      }
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Error al cargar promociones",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }, [getAccessToken, mode, pageSize, currentPage, query, toast, isAuthenticated])

  useEffect(() => {
    if (!isAuthenticated) {
      return
    }
    
    let cancelled = false
    loadPromotions().catch((error) => {
      if (!cancelled) {
        console.error("Error loading promotions:", error)
  
        setLoading(false)
      }
    })
    return () => {
      cancelled = true
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAuthenticated])

  const setPage = useCallback(async (page: number) => {
    if (mode === "paginated") {
      await loadPromotions(page)
    }
  }, [mode, loadPromotions])

  const updateQuery = useCallback(async (updater: Partial<PromotionQueryState> | ((prev: PromotionQueryState) => PromotionQueryState)) => {
    const nextState = typeof updater === "function" ? updater(query) : { ...query, ...updater }
    const sanitized = sanitizePromotionQuery(nextState)

    setQuery(sanitized)

    if (mode === "paginated") {
      setCurrentPage(0)
      await loadPromotions(0, sanitized)
    } else {
      await loadPromotions(undefined, sanitized)
    }
  }, [loadPromotions, mode, query])

  const addPromotion = async (data: PromotionCreateDTO) => {
    setIsSubmitting(true)
    try {
      const accessToken = await getAccessToken()
      const created = await createPromotion(accessToken, data)

      if (mode === "all") {
        setPromotions((prev) => [...prev, created])
      } else {
        await loadPromotions(0)
      }

      toast({
        title: "Promoción creada",
        description: "La promoción se ha creado exitosamente.",
      })
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Error al crear promoción",
        variant: "destructive",
      })
      throw error
    } finally {
      setIsSubmitting(false)
    }
  }

  const updatePromotion = async (id: number, data: PromotionUpdateDTO) => {
    setIsSubmitting(true)
    try {
      const accessToken = await getAccessToken()
      await updatePromotionAPI(accessToken, id, data)

      await loadPromotions(mode === "paginated" ? currentPage : undefined)

      toast({
        title: "Promoción actualizada",
        description: "La promoción se ha actualizado exitosamente.",
      })
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Error al actualizar promoción",
        variant: "destructive",
      })
      throw error
    } finally {
      setIsSubmitting(false)
    }
  }

  const removePromotion = async (id: number) => {
    setIsSubmitting(true)
    try {
      const accessToken = await getAccessToken()
      await deletePromotionAPI(accessToken, id)

      if (mode === "all") {
        setPromotions((prev) => prev.filter((p) => p.id !== id))
      } else {
        await loadPromotions(currentPage)
      }

      toast({
        title: "Promoción eliminada",
        description: "La promoción se ha eliminado exitosamente.",
      })
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Error al eliminar promoción",
        variant: "destructive",
      })
      throw error
    } finally {
      setIsSubmitting(false)
    }
  }

  const getActivePromotions = async (): Promise<PromotionDTO[]> => {
    try {
      const accessToken = await getAccessToken()
      return await fetchActivePromotions(accessToken)
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Error al obtener promociones activas",
        variant: "destructive",
      })
      return []
    }
  }

  const loadPrioritizablePromotions = useCallback(async (): Promise<PromotionDTO[]> => {
    if (!isAuthenticated) {
      return []
    }
    try {
      const accessToken = await getAccessToken()
      return await fetchActivePromotions(accessToken)
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Error al obtener promociones activas",
        variant: "destructive",
      })
      return []
    }
  }, [getAccessToken, toast, isAuthenticated])

  const reorderPromotions = useCallback(async (orderedIds: number[]): Promise<PromotionDTO[]> => {
    if (!orderedIds.length) {
      throw new Error("Debe seleccionar al menos una promoción")
    }
    setIsSubmitting(true)
    try {
      const accessToken = await getAccessToken()
      const payload: PromotionPriorityUpdateDTO = { orderedPromotionIds: orderedIds }
      const updated = await updatePromotionPriorities(accessToken, payload)
      await loadPromotions(mode === "paginated" ? currentPage : undefined)

      toast({
        title: "Prioridades actualizadas",
        description: "Las promociones se ejecutarán en el nuevo orden.",
      })

      return updated
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Error al actualizar prioridades",
        variant: "destructive",
      })
      throw error
    } finally {
      setIsSubmitting(false)
    }
  }, [getAccessToken, loadPromotions, toast, mode, currentPage])

  return {
    promotions,
    loading,
    isSubmitting,
    loadPromotions,
    addPromotion,
    updatePromotion,
    removePromotion,
    pagination,
    setPage,
    query,
    updateQuery,
    mode,
    getActivePromotions,
    loadPrioritizablePromotions,
    reorderPromotions,
  }
}


