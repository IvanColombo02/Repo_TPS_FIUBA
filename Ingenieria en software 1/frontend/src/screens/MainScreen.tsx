"use client"

import { useState, useMemo, useEffect, useCallback, useRef } from "react"
import { UtensilsCrossed, X, Settings, ChefHat, ShoppingBag, ChevronLeft, ChevronRight } from "lucide-react"
import { Carousel } from "@/components/carousel"
import { ProductCard } from "@/components/product-card"
import { SearchFilters } from "@/components/search-filters"
import { CartSheet } from "@/components/cart-sheet"
import { UserProfileDropdown } from "@/components/user-profile-dropdown"
import { ProductDetailsDialog } from "@/components/product-details-dialog"
import { PromotionsSection } from "@/components/promotions-section"
import { Button } from "@/components/ui/button"
import { Pagination, PaginationContent, PaginationItem, PaginationLink } from "@/components/ui/pagination"
import { fetchActivePromotions, type PromotionDTO } from "@/lib/api/promotions"
import { generateCarouselSlides } from "@/lib/utils/carousel-slides"
import {
  fetchProductsPage,
  fetchProductsSimplePage,

  type ProductDTO,
  type ProductSimpleDTO,
  type ProductQueryParams,
} from "@/lib/api/products"
import { fetchCombosPage, fetchCombosSimplePage, type ComboQueryParams, type ComboSimpleDTO } from "@/lib/api/combos"
import type { PaginatedResponse } from "@/lib/api/helpers"
import type {
  MenuItem,
  CartItem,
  FoodCategory,
  CarouselAction,
  PaymentMethod,
  OrderPricingSummary,
  Order
} from "@/lib/types"
import type { Combo } from "@/models/Combo"
import { useToken } from "@/services/TokenContext"
import { fetchUserProfile } from "@/services/UserServices"
import { Link } from "wouter"
import { useCreateOrder } from "@/hooks/use-orders"
import { ORDER_STATUS_LABELS } from "@/models/orders"
import { useToast } from "@/hooks/use-toast"
import { computePagePlan, PRODUCTS_PER_PAGE, COMBOS_PER_PAGE, DEFAULT_PAGE_SIZE } from "@/lib/utils/pagination-helpers"
import { useDebouncedValue } from "@/hooks/use-debounce"
import { mapProductToMenuItem, mapComboToMenuItem, findItemById } from "@/lib/utils/item-helpers"
import { evaluateCondition, getItemPromotions, getRelevantPromotions, itemHasRelevantPromotion } from "@/lib/utils/promotions"
import { decodeJWT } from "@/services/UserServices"

import { PaymentDetailsDialog } from "@/components/payment-details-dialog"


export function MainScreen() {
  const [tokenState] = useToken()
  const accessToken = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : null
  const userEmail = useMemo(() => {
    if (!accessToken) return undefined
    return decodeJWT(accessToken)?.email
  }, [accessToken])

  const [productSlice, setProductSlice] = useState<MenuItem[]>([])
  const [comboSlice, setComboSlice] = useState<MenuItem[]>([])
  const [loadingProducts, setLoadingProducts] = useState(false)
  const [loadingCombos, setLoadingCombos] = useState(false)
  const [productTotals, setProductTotals] = useState<{ totalElements: number; totalPages: number } | null>(null)
  const [comboTotals, setComboTotals] = useState<{ totalElements: number; totalPages: number } | null>(null)

  const [itemsPageIndex, setItemsPageIndex] = useState(0)

  const [categories, setCategories] = useState<FoodCategory[]>([])
  const [types, setTypes] = useState<string[]>([])

  const [selectedCategory, setSelectedCategory] = useState<FoodCategory | "Todos">("Todos")
  const [searchQuery, setSearchQuery] = useState("")
  const debouncedSearchQuery = useDebouncedValue(searchQuery, 350)
  const [showPaymentDetailsDialog, setShowPaymentDetailsDialog] = useState(false)
  const [lastOrder, setLastOrder] = useState<Order | null>(null)

  const loadCartFromStorage = useCallback((): CartItem[] => {
    try {
      const savedCart = localStorage.getItem("cart")
      if (savedCart) {
        return JSON.parse(savedCart) as CartItem[]
      }
    } catch (error) {
      console.error("Error al cargar carrito desde localStorage:", error)
    }
    return []
  }, [])

  const saveCartToStorage = useCallback((cartItems: CartItem[]) => {
    try {
      localStorage.setItem("cart", JSON.stringify(cartItems))
    } catch (error) {
      console.error("Error al guardar carrito en localStorage:", error)
    }
  }, [])

  const [cart, setCart] = useState<CartItem[]>(loadCartFromStorage)
  const [showAvailableOnly, setShowAvailableOnly] = useState(false)
  const [selectedCategoryFilters, setSelectedCategoryFilters] = useState<FoodCategory[]>([])
  const [selectedTypeFilters, setSelectedTypeFilters] = useState<string[]>([])
  const [isFilterOpen, setIsFilterOpen] = useState(false)
  const [isTypeFilterOpen, setIsTypeFilterOpen] = useState(false)
  const [categorySearchQuery, setCategorySearchQuery] = useState("")
  const [typeSearchQuery, setTypeSearchQuery] = useState("")
  const [selectedProduct, setSelectedProduct] = useState<MenuItem | null>(null)
  const [userName, setUserName] = useState<string>("")
  const [userRole, setUserRole] = useState<string>("ROLE_USER")
  const [showPromotions, setShowPromotions] = useState(false)
  const [priceMin, setPriceMin] = useState<string>("")
  const [priceMax, setPriceMax] = useState<string>("")
  const [isPriceFilterOpen, setIsPriceFilterOpen] = useState(false)
  const [isPlacingOrder, setIsPlacingOrder] = useState(false)
  const [activePromotions, setActivePromotions] = useState<PromotionDTO[]>([])
  const [loadingPromotions, setLoadingPromotions] = useState(false)

  const [carouselItems, setCarouselItems] = useState<MenuItem[]>([])

  const createOrderMutation = useCreateOrder()
  const { toast } = useToast()


  const productPagesCacheRef = useRef<Map<string, PaginatedResponse<ProductSimpleDTO>>>(new Map())
  const comboPagesCacheRef = useRef<Map<string, PaginatedResponse<ComboSimpleDTO>>>(new Map())

  const productPagesInFlightRef = useRef<Map<string, Promise<PaginatedResponse<ProductSimpleDTO>>>>(new Map())
  const comboPagesInFlightRef = useRef<Map<string, Promise<PaginatedResponse<ComboSimpleDTO>>>>(new Map())
  const productQueryKeyRef = useRef<string | null>(null)
  const comboQueryKeyRef = useRef<string | null>(null)

  const updateFilterOptions = useCallback((items: MenuItem[]) => {
    if (items.length === 0) return

    setCategories((prev) => {
      const merged = new Set(prev)
      items.forEach((item) => {

        if (item.categories && item.categories.length > 0) {
          item.categories.forEach(cat => merged.add(cat))
        } else {
          merged.add(item.category)
        }
      })
      return Array.from(merged)
    })

    setTypes((prev) => {
      const merged = new Set(prev)
      items.forEach((item) => {
        if (item.type) {
          merged.add(item.type)
        }
      })
      return Array.from(merged)
    })
  }, [])

  useEffect(() => {
    if (!accessToken) {
      setProductSlice([])
      setComboSlice([])
      setProductTotals({ totalElements: 0, totalPages: 0 })
      setComboTotals({ totalElements: 0, totalPages: 0 })
      setActivePromotions([])
      setCarouselItems([])
      return
    }

    const abortController = new AbortController()

    fetchUserProfile(accessToken)
      .then((profile) => {
        if (!abortController.signal.aborted) {
          setUserName(profile.username)
          setUserRole(profile.role)
        }
      })
      .catch((error) => {
        if (!abortController.signal.aborted) {
          console.error("Error al cargar perfil:", error)
        }
      })

    setLoadingPromotions(true)
    fetchActivePromotions(accessToken)
      .then((promos) => {
        if (!abortController.signal.aborted) {
          setActivePromotions(promos)
        }
      })
      .catch((error) => {
        if (!abortController.signal.aborted) {
          console.error("Error al cargar promociones:", error)
          setActivePromotions([])
        }
      })
      .finally(() => {
        if (!abortController.signal.aborted) {
          setLoadingPromotions(false)
        }
      })


    return () => {
      abortController.abort()
    }
  }, [accessToken])

  const combinedCategoryFilters = useMemo(() => {
    const combined = new Set<FoodCategory>()
    if (selectedCategory !== "Todos") {
      combined.add(selectedCategory)
    }
    selectedCategoryFilters.forEach((category) => combined.add(category))
    return Array.from(combined)
  }, [selectedCategory, selectedCategoryFilters])

  const minPriceValue = priceMin !== "" ? Number(priceMin) : null
  const maxPriceValue = priceMax !== "" ? Number(priceMax) : null

  const productQueryBase = useMemo(() => {
    const params: Omit<ProductQueryParams, "page" | "size"> = {}

    const trimmedSearch = debouncedSearchQuery.trim()
    if (trimmedSearch) {
      params.name = trimmedSearch
    }

    if (combinedCategoryFilters.length > 0) {
      params.categories = combinedCategoryFilters
    }

    if (selectedTypeFilters.length > 0) {
      params.type = selectedTypeFilters
    }

    if (minPriceValue !== null && !Number.isNaN(minPriceValue)) {
      params.priceMin = Math.max(0, Math.floor(minPriceValue))
    }

    if (maxPriceValue !== null && !Number.isNaN(maxPriceValue)) {
      params.priceMax = Math.max(0, Math.ceil(maxPriceValue))
    }

    if (showAvailableOnly) {
      params.stockAsc = true
    }

    return params
  }, [combinedCategoryFilters, selectedTypeFilters, debouncedSearchQuery, minPriceValue, maxPriceValue, showAvailableOnly])

  const comboQueryBase = useMemo(() => {
    const params: Omit<ComboQueryParams, "page" | "size"> = {}

    const trimmedSearch = debouncedSearchQuery.trim()
    if (trimmedSearch) {
      params.name = trimmedSearch
    }

    if (combinedCategoryFilters.length > 0) {
      params.categories = combinedCategoryFilters
    }

    if (selectedTypeFilters.length > 0) {
      params.type = selectedTypeFilters
    }

    if (minPriceValue !== null && !Number.isNaN(minPriceValue)) {
      params.priceMin = Math.max(0, Math.floor(minPriceValue))
    }

    if (maxPriceValue !== null && !Number.isNaN(maxPriceValue)) {
      params.priceMax = Math.max(0, Math.ceil(maxPriceValue))
    }

    return params
  }, [combinedCategoryFilters, selectedTypeFilters, debouncedSearchQuery, minPriceValue, maxPriceValue])

  const productQueryKey = useMemo(() => JSON.stringify(productQueryBase), [productQueryBase])
  const comboQueryKey = useMemo(() => JSON.stringify(comboQueryBase), [comboQueryBase])

  useEffect(() => {
    if (productQueryKeyRef.current !== productQueryKey) {
      productQueryKeyRef.current = productQueryKey
      productPagesCacheRef.current = new Map()
      setProductTotals(null)
      setProductSlice([])
    }
  }, [productQueryKey])

  useEffect(() => {
    if (comboQueryKeyRef.current !== comboQueryKey) {
      comboQueryKeyRef.current = comboQueryKey
      comboPagesCacheRef.current = new Map()
      setComboTotals(null)
      setComboSlice([])
    }
  }, [comboQueryKey])

  useEffect(() => {
    if (!accessToken) {
      setProductSlice([])
      setComboSlice([])
      return
    }

    let cancelled = false

    const ensureProductPage = async (pageNumber: number): Promise<PaginatedResponse<ProductSimpleDTO> | null> => {
      if (pageNumber < 0) return null
      const cacheKey = `${productQueryKey}_${pageNumber}`
      const cached = productPagesCacheRef.current.get(cacheKey)
      if (cached) {
        return cached
      }

      const inFlight = productPagesInFlightRef.current.get(cacheKey)
      if (inFlight) {
        try {
          return await inFlight
        } catch {
          // fallthrough to retry
        }
      }

      try {
        const fetchPromise = (async () => {
          const response = await fetchProductsSimplePage(accessToken, { ...productQueryBase, page: pageNumber, size: PRODUCTS_PER_PAGE })
          if (!cancelled) {
            productPagesCacheRef.current.set(cacheKey, response)
            setProductTotals({ totalElements: response.totalElements, totalPages: response.totalPages })
          }
          return response
        })()

        productPagesInFlightRef.current.set(cacheKey, fetchPromise)
        try {
          const r = await fetchPromise
          return r
        } finally {
          productPagesInFlightRef.current.delete(cacheKey)
        }
      } catch (error) {
        console.error("Error al cargar productos paginados", error)
        productPagesInFlightRef.current.delete(cacheKey)
        return null
      }
    }

    const ensureComboPage = async (pageNumber: number): Promise<PaginatedResponse<ComboSimpleDTO> | null> => {
      if (pageNumber < 0) return null
      const cacheKey = `${comboQueryKey}_${pageNumber}`
      const cached = comboPagesCacheRef.current.get(cacheKey)
      if (cached) {
        return cached
      }

      const inFlight = comboPagesInFlightRef.current.get(cacheKey)
      if (inFlight) {
        try {
          return await inFlight
        } catch {
          // fallthrough to retry
        }
      }

      try {
        const fetchPromise = (async () => {
          const response = await fetchCombosSimplePage(accessToken, { ...comboQueryBase, page: pageNumber, size: COMBOS_PER_PAGE })
          if (!cancelled) {
            comboPagesCacheRef.current.set(cacheKey, response)
            setComboTotals({ totalElements: response.totalElements, totalPages: response.totalPages })
          }
          return response
        })()

        comboPagesInFlightRef.current.set(cacheKey, fetchPromise)
        try {
          const r = await fetchPromise
          return r
        } finally {
          comboPagesInFlightRef.current.delete(cacheKey)
        }
      } catch (error) {
        console.error("Error al cargar combos paginados", error)
        comboPagesInFlightRef.current.delete(cacheKey)
        return null
      }
    }

    const collectProductsRange = async (start: number, count: number) => {
      if (count <= 0) return []
      const end = start + count
      const pageSize = PRODUCTS_PER_PAGE
      let pageNumber = Math.floor(start / pageSize)
      const collected: ProductSimpleDTO[] = []

      while (true) {
        const page = await ensureProductPage(pageNumber)
        if (!page) {
          break
        }

        const pageStart = page.number * page.size
        const pageEnd = pageStart + page.content.length
        const sliceStart = Math.max(start, pageStart)
        const sliceEnd = Math.min(end, pageEnd)

        if (sliceStart < sliceEnd) {
          collected.push(...page.content.slice(sliceStart - pageStart, sliceEnd - pageStart))
        }

        if (page.content.length === 0 || pageEnd >= end) {
          break
        }

        pageNumber += 1
      }

      return collected
    }

    const collectCombosRange = async (start: number, count: number) => {
      if (count <= 0) return []
      const end = start + count
      const pageSize = COMBOS_PER_PAGE
      let pageNumber = Math.floor(start / pageSize)
      const collected: ComboSimpleDTO[] = []

      while (true) {
        const page = await ensureComboPage(pageNumber)
        if (!page) {
          break
        }

        const pageStart = page.number * page.size
        const pageEnd = pageStart + page.content.length
        const sliceStart = Math.max(start, pageStart)
        const sliceEnd = Math.min(end, pageEnd)

        if (sliceStart < sliceEnd) {
          collected.push(...page.content.slice(sliceStart - pageStart, sliceEnd - pageStart))
        }

        if (page.content.length === 0 || pageEnd >= end) {
          break
        }

        pageNumber += 1
      }

      return collected
    }

    const loadPage = async () => {
      setLoadingProducts(true)
      setLoadingCombos(true)

      try {
        const firstProductPage = await ensureProductPage(0)
        const firstComboPage = await ensureComboPage(0)


        if (!cancelled) {
          if (firstProductPage) {
            setProductTotals({ totalElements: firstProductPage.totalElements, totalPages: firstProductPage.totalPages })
          }
          if (firstComboPage) {
            setComboTotals({ totalElements: firstComboPage.totalElements, totalPages: firstComboPage.totalPages })
          }
        }

        const totalProducts = firstProductPage?.totalElements ?? productTotals?.totalElements ?? 0
        const totalCombos = firstComboPage?.totalElements ?? comboTotals?.totalElements ?? 0

        const totalPages = Math.ceil((totalProducts + totalCombos) / DEFAULT_PAGE_SIZE)

        if (totalPages === 0) {
          if (!cancelled) {
            setProductSlice([])
            setComboSlice([])
          }
          return
        }

        if (itemsPageIndex >= totalPages) {
          if (!cancelled) {
            setItemsPageIndex(Math.max(totalPages - 1, 0))
          }
          return
        }

        const plan = computePagePlan(totalProducts, totalCombos, itemsPageIndex)

        const [products, combos] = await Promise.all([
          collectProductsRange(plan.productStart, plan.productCount),
          collectCombosRange(plan.comboStart, plan.comboCount),
        ])

        if (cancelled) return

        const mappedProducts = products.map(mapProductToMenuItem)
        const mappedCombos = combos.map((combo) => mapComboToMenuItem(combo, products))

        setProductSlice(mappedProducts)
        setComboSlice(mappedCombos)
        updateFilterOptions([...mappedProducts, ...mappedCombos])


        if (itemsPageIndex === 0 && Object.keys(productQueryBase).length === 0) {
          setCarouselItems([...mappedProducts, ...mappedCombos])
        }
      } catch (error) {
        if (!cancelled) {
          console.error("Error al cargar página combinada:", error)
          setProductSlice([])
          setComboSlice([])
        }
      } finally {
        if (!cancelled) {
          setLoadingProducts(false)
          setLoadingCombos(false)
        }
      }
    }

    loadPage()

    return () => {
      cancelled = true
    }
  }, [accessToken, itemsPageIndex, productQueryBase, comboQueryBase, updateFilterOptions, comboTotals?.totalElements, productTotals?.totalElements])

  useEffect(() => {
    setItemsPageIndex(0)
  }, [
    selectedCategory,
    selectedCategoryFilters,
    selectedTypeFilters,
    searchQuery,
    minPriceValue,
    maxPriceValue,
    showPromotions,
    showAvailableOnly,
  ])

  const combinedItems = useMemo(() => {
    return [...productSlice, ...comboSlice]
  }, [productSlice, comboSlice])


  const carouselSlides = useMemo(() => {
    return generateCarouselSlides({
      items: carouselItems,
      promotions: activePromotions,
      categories: categories,
    })
  }, [carouselItems, activePromotions, categories])

  const nonFreeCartItemsKey = useMemo(() => {
    return cart
      .filter((item) => !item.customizationId.startsWith("free-"))
      .map((item) => `${item.id}-${item.quantity}`)
      .join(",")
  }, [cart])

  const activePromotionsIdsKey = useMemo(() => {
    return activePromotions.map((p) => p.id).join(",")
  }, [activePromotions])

  useEffect(() => {
    if (!activePromotions.length || !accessToken) return

    setCart((prevCart) => {

      const originalCartSubtotal = prevCart
        .filter((item) => !item.customizationId.startsWith("free-"))
        .reduce((total, item) => {
          const originalPrice = item.originalPrice || item.price
          return total + originalPrice * item.quantity
        }, 0)

      const freeProductsToAdd: Array<{ promo: PromotionDTO; productId: number; quantity: number }> = []

      activePromotions.forEach((promo) => {
        try {
          const expression = JSON.parse(promo.expression)
          const condition = expression.condition
          const action = expression.action

          if (action?.type !== "freeProduct") return


          if (!evaluateCondition(condition, prevCart, originalCartSubtotal, userEmail)) return

          const productId = action.productId
          const quantity = action.quantity || 1

          if (productId) {
            freeProductsToAdd.push({ promo, productId, quantity })
          }
        } catch (error) {
          console.error("Error evaluando promoción freeProduct:", error)
        }
      })

      let newCart = [...prevCart]

      const freeProductPromoIds = freeProductsToAdd.map((fp) => fp.promo.id)
      newCart = newCart.filter((item) => {
        if (!item.customizationId.startsWith("free-")) return true
        const promoId = item.customizationId.split("-")[1]
        return freeProductPromoIds.includes(parseInt(promoId))
      })

      freeProductsToAdd.forEach(({ promo, productId, quantity }) => {
        const freeCustomizationId = `free-${promo.id}-${productId}`
        const existingFreeItem = newCart.find((item) => item.customizationId === freeCustomizationId)

        if (!existingFreeItem) {
          const freeItem = findItemById(productId, combinedItems)
          if (freeItem) {
            newCart.push({
              ...freeItem,
              price: 0,
              originalPrice: freeItem.price,
              quantity,
              customizationId: freeCustomizationId,
            })
          }
        } else {
          if (existingFreeItem.quantity !== quantity) {
            newCart = newCart.map((item) =>
              item.customizationId === freeCustomizationId
                ? { ...item, quantity }
                : item
            )
          }
        }
      })

      saveCartToStorage(newCart)
      return newCart
    })
  }, [nonFreeCartItemsKey, activePromotionsIdsKey, combinedItems, accessToken, activePromotions, saveCartToStorage])

  const filteredCategories = useMemo(() => {
    return categories.filter((category) => category.toLowerCase().includes(categorySearchQuery.toLowerCase()))
  }, [categorySearchQuery, categories])

  const filteredTypes = useMemo(() => {
    return types.filter((type) => type.toLowerCase().includes(typeSearchQuery.toLowerCase()))
  }, [typeSearchQuery, types])

  const totalProductElements = productTotals?.totalElements ?? 0
  const totalComboElements = comboTotals?.totalElements ?? 0
  const totalAvailableItems = totalProductElements + totalComboElements

  const [allItemsForPromotions, setAllItemsForPromotions] = useState<MenuItem[]>([])
  const [loadingAllItemsForPromotions, setLoadingAllItemsForPromotions] = useState(false)

  useEffect(() => {
    if (!showPromotions || !accessToken) {
      setAllItemsForPromotions([])
      setLoadingAllItemsForPromotions(false)
      return
    }

    let cancelled = false
    setLoadingAllItemsForPromotions(true)

    const MAX_PROMOTION_ITEMS = 1000
    const MAX_PROMOTION_PAGES = 20
    const loadAllItems = async () => {
      try {
        const allProducts: ProductDTO[] = []
        const allCombos: Combo[] = []

        let productPage = 0
        let productPageCount = 0
        while (true) {
          const response = await fetchProductsPage(accessToken, {
            ...productQueryBase,
            page: productPage,
            size: PRODUCTS_PER_PAGE
          })
          if (cancelled || !response || response.content.length === 0) break
          allProducts.push(...response.content)
          productPageCount++
          if (allProducts.length >= MAX_PROMOTION_ITEMS || productPageCount >= MAX_PROMOTION_PAGES) break
          if (response.last || response.content.length < PRODUCTS_PER_PAGE) break
          productPage++
        }

        let comboPage = 0
        let comboPageCount = 0
        while (true) {
          const response = await fetchCombosPage(accessToken, {
            ...comboQueryBase,
            page: comboPage,
            size: COMBOS_PER_PAGE
          })
          if (cancelled || !response || response.content.length === 0) break
          allCombos.push(...response.content)
          comboPageCount++
          if (allProducts.length + allCombos.length >= MAX_PROMOTION_ITEMS || comboPageCount >= MAX_PROMOTION_PAGES) break
          if (response.last || response.content.length < COMBOS_PER_PAGE) break
          comboPage++
        }

        if (!cancelled) {
          const mappedProducts = allProducts.map(mapProductToMenuItem)
          const mappedCombos = allCombos.map((combo) => mapComboToMenuItem(combo, allProducts))
          setAllItemsForPromotions([...mappedProducts, ...mappedCombos])
          setLoadingAllItemsForPromotions(false)
        }
      } catch (error) {
        console.error("Error al cargar todos los items para promociones:", error)
        if (!cancelled) {
          setAllItemsForPromotions([])
          setLoadingAllItemsForPromotions(false)
        }
      }
    }

    loadAllItems()

    return () => {
      cancelled = true
      setLoadingAllItemsForPromotions(false)
    }
  }, [showPromotions, accessToken, productQueryBase, comboQueryBase])

  const filteredItems = useMemo(() => {
    const itemsToFilter = showPromotions ? allItemsForPromotions : combinedItems

    const filtered = itemsToFilter.filter((item) => {
      if (showPromotions) {
        return itemHasRelevantPromotion(item, activePromotions, userEmail)
      }
      return true
    })

    const sorted = filtered.sort((a, b) => {
      if (a.available === b.available) return 0
      return a.available ? -1 : 1
    })

    if (showPromotions) {
      const startIndex = itemsPageIndex * DEFAULT_PAGE_SIZE
      const endIndex = startIndex + DEFAULT_PAGE_SIZE
      return sorted.slice(startIndex, endIndex)
    }

    return sorted
  }, [combinedItems, allItemsForPromotions, showPromotions, activePromotions, itemsPageIndex, userEmail])

  const totalFilteredItems = useMemo(() => {
    if (showPromotions) {
      return allItemsForPromotions.filter((item) => itemHasRelevantPromotion(item, activePromotions, userEmail)).length
    }
    return totalProductElements + totalComboElements
  }, [allItemsForPromotions, showPromotions, activePromotions, totalProductElements, totalComboElements, userEmail])

  const totalPages = useMemo(() => {
    if (showPromotions) {
      return Math.ceil(totalFilteredItems / DEFAULT_PAGE_SIZE)
    }
    return Math.ceil((totalProductElements + totalComboElements) / DEFAULT_PAGE_SIZE)
  }, [showPromotions, totalFilteredItems, totalProductElements, totalComboElements])
  const isPrevDisabled = itemsPageIndex === 0 || loadingProducts || loadingCombos
  const isNextDisabled =
    totalPages === 0 || itemsPageIndex >= totalPages - 1 || loadingProducts || loadingCombos
  const paginationPages = Array.from({ length: totalPages }, (_, index) => index + 1)

  const addToCart = (item: MenuItem) => {
    setCart((prevCart) => {
      const customizationId = `${item.id}-default`
      const existingItem = prevCart.find((cartItem) => cartItem.customizationId === customizationId)

      let newCart: CartItem[]
      if (existingItem) {
        newCart = prevCart.map((cartItem) =>
          cartItem.customizationId === customizationId
            ? { ...cartItem, quantity: cartItem.quantity + 1 }
            : cartItem,
        )
        toast({
          title: "Cantidad actualizada",
          description: `${item.name} - Cantidad: ${existingItem.quantity + 1}`,
        })
      } else {
        newCart = [...prevCart, { ...item, quantity: 1, customizationId }]
        toast({
          title: "Producto agregado",
          description: `${item.name} se agregó al carrito`,
        })
      }

      saveCartToStorage(newCart)
      return newCart
    })
  }

  const removeFromCart = (customizationId: string) => {
    setCart((prevCart) => {
      const newCart = prevCart.filter((item) => item.customizationId !== customizationId)
      saveCartToStorage(newCart)
      return newCart
    })
  }

  const updateQuantity = (customizationId: string, newQuantity: number) => {
    if (newQuantity === 0) {
      removeFromCart(customizationId)
      return
    }
    setCart((prevCart) => {
      const newCart = prevCart.map((item) =>
        item.customizationId === customizationId ? { ...item, quantity: newQuantity } : item,
      )
      saveCartToStorage(newCart)
      return newCart
    })
  }

  const handleCarouselClick = useCallback(
    async (action?: CarouselAction) => {
      if (!action) return

      if (action.type === "category") {
        setSelectedCategory(action.category)
        setShowPromotions(false)
        window.scrollTo({ top: 600, behavior: "smooth" })
      } else if (action.type === "promotion") {
        setShowPromotions(true)
        setSelectedCategory("Todos")
        window.scrollTo({ top: 600, behavior: "smooth" })
      } else if (action.type === "product") {
        const product = combinedItems.find((item) => item.id === action.productId)
        if (product) {
          setSelectedProduct(product)
        }
      }
    },
    [combinedItems, accessToken, productSlice],
  )

  const toggleCategoryFilter = (category: FoodCategory) => {
    setSelectedCategoryFilters((prev) =>
      prev.includes(category) ? prev.filter((c) => c !== category) : [...prev, category],
    )
  }

  const toggleTypeFilter = (type: string) => {
    setSelectedTypeFilters((prev) =>
      prev.includes(type) ? prev.filter((t) => t !== type) : [...prev, type],
    )
  }

  const clearAllFilters = () => {
    setSelectedCategory("Todos")
    setSearchQuery("")
    setShowAvailableOnly(false)
    setSelectedCategoryFilters([])
    setSelectedTypeFilters([])
    setShowPromotions(false)
    setCategorySearchQuery("")
    setTypeSearchQuery("")
    setPriceMin("")
    setPriceMax("")
    setItemsPageIndex(0)
  }

  const hasActiveFilters =
    selectedCategory !== "Todos" ||
    searchQuery !== "" ||
    showAvailableOnly ||
    selectedCategoryFilters.length > 0 ||
    selectedTypeFilters.length > 0 ||
    showPromotions ||
    priceMin !== "" ||
    priceMax !== ""

  const handleAddCustomizedToCart = () => {
    if (selectedProduct) {

      addToCart(selectedProduct)
    }
  }

  const handlePlaceOrder = async ({
    paymentMethod,
  }: {
    paymentMethod: PaymentMethod
    summary: OrderPricingSummary
  }) => {
    if (cart.length === 0) {
      return
    }

    try {
      setIsPlacingOrder(true)
      const order = await createOrderMutation.mutateAsync({ cartItems: cart, paymentMethod })

      setCart([])
      saveCartToStorage([])
      toast({
        title: "Pedido realizado",
        description: `Tu pedido ${order.id} está ${ORDER_STATUS_LABELS[order.status].toLowerCase()}.`,
      })

      setLastOrder(order)
      setShowPaymentDetailsDialog(true)

    } catch (error) {
      const message = error instanceof Error ? error.message : "Intenta nuevamente más tarde."
      toast({
        title: "No se pudo completar el pedido",
        description: message,
        variant: "destructive",
      })
      throw error
    } finally {
      setIsPlacingOrder(false)
    }
  }

  const loading = showPromotions
    ? loadingAllItemsForPromotions && allItemsForPromotions.length === 0
    : (loadingProducts || loadingCombos) && combinedItems.length === 0

  return (
    <div className="min-h-screen bg-background dark">
      <header className="sticky top-0 z-50 border-b border-border bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="container mx-auto flex h-16 items-center justify-between px-4">
          <div className="flex items-center gap-2 md:gap-3">
            <UtensilsCrossed className="h-6 w-6 lg:h-8 lg:w-8 text-primary" />
            <h1 className="text-lg md:text-xl lg:text-2xl font-bold text-foreground">Comedor FIUBA</h1>
          </div>

          <div className="flex items-center gap-2 md:gap-3">
            <Link href="/orders">
              <Button variant="outline" className="flex items-center gap-2 text-foreground">
                <ShoppingBag className="h-4 w-4" />
                <span className="hidden sm:inline">Mis Pedidos</span>
              </Button>
            </Link>

            {userRole === "ROLE_ADMIN" && (
              <>
                <Link href="/admin">
                  <Button variant="outline" className="flex items-center gap-2 text-foreground">
                    <Settings className="h-4 w-4" />
                    <span className="hidden sm:inline">Administración</span>
                  </Button>
                </Link>
                <Link href="/employee">
                  <Button variant="outline" className="flex items-center gap-2 text-foreground">
                    <ChefHat className="h-4 w-4" />
                    <span className="hidden sm:inline">Órdenes</span>
                  </Button>
                </Link>
              </>
            )}
            {userRole === "ROLE_EMPLOYEE" && (
              <Link href="/employee">
                <Button variant="outline" className="flex items-center gap-2 text-foreground">
                  <ChefHat className="h-4 w-4" />
                  <span className="hidden sm:inline">Órdenes</span>
                </Button>
              </Link>
            )}

            <UserProfileDropdown userName={userName} userRole={userRole} />
            <CartSheet
              cart={cart}
              onUpdateQuantity={updateQuantity}
              onRemoveItem={removeFromCart}
              onCheckout={handlePlaceOrder}
              isProcessing={isPlacingOrder}
            />
          </div>
        </div>
      </header>

      <section className="border-b border-border bg-card">
        <div className="container mx-auto px-4 py-6 md:py-8 lg:py-12">
          <div className="max-w-3xl">
            <h2 className="text-2xl md:text-3xl lg:text-5xl font-bold tracking-tight text-foreground mb-2 md:mb-3 lg:mb-4 text-balance">
              Pide tu comida favorita
            </h2>
            <p className="text-sm md:text-base lg:text-xl text-muted-foreground text-pretty">
              Explora nuestro menú y realiza tu pedido de forma rápida y sencilla. Comida fresca preparada diariamente
              para la comunidad universitaria.
            </p>
          </div>
        </div>
      </section>

      <section className="border-b border-border bg-background">
        <div className="container mx-auto px-4 py-4 md:py-6 lg:py-8">
          <Carousel slides={carouselSlides} onSlideClick={handleCarouselClick} />
        </div>
      </section>

      <PromotionsSection promotions={activePromotions} loading={loadingPromotions} />

      <section className="border-b border-border bg-background">
        <div className="container mx-auto px-4 py-3 md:py-4 lg:py-6">
          <SearchFilters
            searchQuery={searchQuery}
            onSearchChange={setSearchQuery}
            showAvailableOnly={showAvailableOnly}
            onAvailableOnlyChange={setShowAvailableOnly}
            showPromotions={showPromotions}
            onPromotionsChange={setShowPromotions}
            selectedCategoryFilters={selectedCategoryFilters}
            onCategoryFilterToggle={toggleCategoryFilter}
            categorySearchQuery={categorySearchQuery}
            onCategorySearchChange={setCategorySearchQuery}
            filteredCategories={filteredCategories}
            isFilterOpen={isFilterOpen}
            onFilterOpenChange={setIsFilterOpen}
            selectedTypeFilters={selectedTypeFilters}
            onTypeFilterToggle={toggleTypeFilter}
            typeSearchQuery={typeSearchQuery}
            onTypeSearchChange={setTypeSearchQuery}
            filteredTypes={filteredTypes}
            isTypeFilterOpen={isTypeFilterOpen}
            onTypeFilterOpenChange={setIsTypeFilterOpen}
            priceMin={priceMin}
            priceMax={priceMax}
            onPriceMinChange={setPriceMin}
            onPriceMaxChange={setPriceMax}
            isPriceFilterOpen={isPriceFilterOpen}
            onPriceFilterOpenChange={setIsPriceFilterOpen}
          />
        </div>
      </section>

      <main className="container mx-auto px-4 py-6 md:py-8">
        <div className="mb-4 md:mb-6 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
          <div>
            <p className="text-sm lg:text-base text-muted-foreground">
              {filteredItems.length} {filteredItems.length === 1 ? "resultado" : "resultados"} visibles
              {totalAvailableItems > 0 && ` de ${totalAvailableItems} totales`}
            </p>
            {showPromotions && <p className="text-xs lg:text-sm text-primary mt-1">Mostrando solo promociones</p>}
            {selectedCategory !== "Todos" && (
              <p className="text-xs lg:text-sm text-primary mt-1">Categoría: {selectedCategory}</p>
            )}
          </div>

          {hasActiveFilters && (
            <Button variant="outline" size="sm" onClick={clearAllFilters} className="text-foreground bg-transparent">
              <X className="h-4 w-4 mr-2" />
              Limpiar filtros
            </Button>
          )}
        </div>

        {loading ? (
          <div className="text-center py-12">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
            <p className="text-muted-foreground text-sm mt-4">Cargando productos...</p>
          </div>
        ) : (
          <>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 md:gap-6">
              {filteredItems.map((item) => {
                const itemPromotions = getItemPromotions(item, activePromotions)

                return (
                  <ProductCard
                    key={item.id}
                    item={item}
                    onAddToCart={addToCart}
                    onViewDetails={(it) => {
                      setSelectedProduct(it)
                    }}
                    promotions={itemPromotions}
                  />
                )
              })}
            </div>

            {filteredItems.length === 0 && !loading && (
              <div className="text-center py-12">
                <p className="text-muted-foreground text-sm md:text-base lg:text-lg">
                  No se encontraron productos que coincidan con tu búsqueda.
                </p>
                {hasActiveFilters && (
                  <Button variant="outline" onClick={clearAllFilters} className="mt-4 text-foreground bg-transparent">
                    Limpiar filtros
                  </Button>
                )}
              </div>
            )}

            {totalPages > 1 && (
              <div className="mt-8 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">

                <Pagination className="w-full sm:w-auto">
                  <PaginationContent>
                    <PaginationItem>
                      <PaginationLink
                        href="#"
                        size="default"
                        className={(isPrevDisabled ? "pointer-events-none opacity-50" : "") + " text-white"}
                        aria-disabled={isPrevDisabled}
                        tabIndex={isPrevDisabled ? -1 : 0}
                        onClick={(event) => {
                          event.preventDefault()
                          if (!isPrevDisabled) {
                            setItemsPageIndex((prev) => Math.max(prev - 1, 0))
                          }
                        }}
                      >
                        <ChevronLeft className="h-4 w-4" />
                        <span className="hidden sm:inline">Anterior</span>
                      </PaginationLink>
                    </PaginationItem>
                    {paginationPages.map((pageNumber) => {
                      const pageIndex = pageNumber - 1
                      const isActive = pageIndex === itemsPageIndex
                      return (
                        <PaginationItem key={pageNumber}>
                          <PaginationLink
                            href="#"
                            isActive={isActive}
                            className="text-white"
                            onClick={(event) => {
                              event.preventDefault()
                              if (!isActive) {
                                setItemsPageIndex(pageIndex)
                              }
                            }}
                          >
                            {pageNumber}
                          </PaginationLink>
                        </PaginationItem>
                      )
                    })}
                    <PaginationItem>
                      <PaginationLink
                        href="#"
                        size="default"
                        className={(isNextDisabled ? "pointer-events-none opacity-50" : "") + " text-white"}
                        aria-disabled={isNextDisabled}
                        tabIndex={isNextDisabled ? -1 : 0}
                        onClick={(event) => {
                          event.preventDefault()
                          if (!isNextDisabled) {
                            setItemsPageIndex((prev) => Math.min(prev + 1, totalPages - 1))
                          }
                        }}
                      >
                        <span className="hidden sm:inline">Siguiente</span>
                        <ChevronRight className="h-4 w-4" />
                      </PaginationLink>
                    </PaginationItem>
                  </PaginationContent>
                </Pagination>
              </div>
            )}
          </>
        )}
      </main>

      <ProductDetailsDialog
        product={selectedProduct}
        open={!!selectedProduct}
        onOpenChange={(open) => !open && setSelectedProduct(null)}
        onAddToCart={handleAddCustomizedToCart}
        promotions={selectedProduct ? getRelevantPromotions(selectedProduct, activePromotions, userEmail) : []}
        accessToken={accessToken}
      />


      <PaymentDetailsDialog
        order={lastOrder}
        open={showPaymentDetailsDialog}
        onOpenChange={setShowPaymentDetailsDialog}
      />

      <footer className="border-t border-border bg-card mt-8 md:mt-12">
        <div className="container mx-auto px-4 py-6 lg:py-8">
          <div className="text-center text-muted-foreground">
            <p className="text-sm lg:text-base">© 2025 Comedor FIUBA. Todos los derechos reservados.</p>
            <p className="mt-2 text-xs lg:text-sm">Horario: Lunes a Viernes, 8:00 - 20:00</p>
          </div>
        </div>
      </footer>
    </div>
  )
}
