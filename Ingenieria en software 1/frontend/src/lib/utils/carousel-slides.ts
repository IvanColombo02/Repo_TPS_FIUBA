import type { CarouselSlide, MenuItem, FoodCategory } from "@/lib/types"
import type { PromotionDTO } from "@/lib/api/promotions"
import { itemHasRelevantPromotion } from "./promotions"

interface GenerateCarouselSlidesParams {
  items: MenuItem[]
  promotions: PromotionDTO[]
  categories: FoodCategory[]
}


export function generateCarouselSlides({
  items,
  promotions,
  categories,
}: GenerateCarouselSlidesParams): CarouselSlide[] {
  const slides: CarouselSlide[] = []
  let slideId = 1
  const usedItemIds = new Set<string | number>()

  const getUnusedItem = (itemList: MenuItem[]): MenuItem | undefined => {
    return itemList.find((item) => {
      const itemId = typeof item.id === 'string' ? item.id : item.id
      return !usedItemIds.has(itemId)
    })
  }

  const markAsUsed = (item: MenuItem) => {
    const itemId = typeof item.id === 'string' ? item.id : item.id
    usedItemIds.add(itemId)
  }

  if (promotions.length > 0) {
    const itemsWithPromotions = items.filter((item) => itemHasRelevantPromotion(item, promotions))
    
    if (itemsWithPromotions.length > 0) {
      const featuredItem = getUnusedItem(itemsWithPromotions) || itemsWithPromotions[0]
      const promotionCount = promotions.length
      
      if (featuredItem) {
        markAsUsed(featuredItem)
        slides.push({
          id: slideId++,
          title: promotionCount === 1 
            ? "¡Oferta Especial!" 
            : `¡${promotionCount} Ofertas Especiales!`,
          description: promotionCount === 1
            ? "Aprovecha nuestra promoción del día"
            : `Descubre nuestras ${promotionCount} promociones activas`,
          image: featuredItem.image || "/placeholder.svg",
          action: { type: "promotion" },
        })
      }
    }
  }

  const mainCategories = categories.slice(0, 4)
  
  for (const category of mainCategories) {
    const categoryItems = items.filter((item) => {
      if (!item.available) return false
      if (item.categories && item.categories.length > 0) {
        return item.categories.includes(category)
      }
      return item.category === category
    })
    
    if (categoryItems.length > 0) {
        // prioritize items with promotions, then by price (cheapest first)
      const sortedItems = [...categoryItems].sort((a, b) => {
        const aHasPromo = itemHasRelevantPromotion(a, promotions)
        const bHasPromo = itemHasRelevantPromotion(b, promotions)
        
        if (aHasPromo && !bHasPromo) return -1
        if (!aHasPromo && bHasPromo) return 1
        
        return a.price - b.price
      })
      
      const featuredItem = getUnusedItem(sortedItems)
      if (!featuredItem) continue
      
      markAsUsed(featuredItem)
      const itemCount = categoryItems.length
      const itemsWithPromoInCategory = categoryItems.filter((item) => itemHasRelevantPromotion(item, promotions))
      const hasPromotions = itemsWithPromoInCategory.length > 0
      
      let title: string
      if (hasPromotions) {
        title = `${category}${itemsWithPromoInCategory.length > 1 ? ` - ${itemsWithPromoInCategory.length} ofertas` : ' - ¡En Oferta!'}`
      } else {
        title = category
      }
      
      let description: string
      if (hasPromotions) {
        description = `${itemsWithPromoInCategory.length} ${itemsWithPromoInCategory.length === 1 ? 'producto' : 'productos'} en oferta. ${itemCount} ${itemCount === 1 ? 'opción' : 'opciones'} disponibles`
      } else if (featuredItem.description) {
        description = featuredItem.description
      } else {
        description = `${itemCount} ${itemCount === 1 ? 'producto disponible' : 'productos disponibles'} en ${category.toLowerCase()}`
      }
      
      slides.push({
        id: slideId++,
        title,
        description,
        image: featuredItem.image || "/placeholder.svg",
        action: { type: "category", category },
      })
    }
  }

  if (slides.length < 3 && items.length > 0) {
    const availableItems = items.filter((item) => item.available)
    
    if (availableItems.length > 0) {
      const sortedItems = [...availableItems].sort((a, b) => {
        const aHasPromo = itemHasRelevantPromotion(a, promotions)
        const bHasPromo = itemHasRelevantPromotion(b, promotions)
        
        if (aHasPromo && !bHasPromo) return -1
        if (!aHasPromo && bHasPromo) return 1
        
        return a.price - b.price
      })
      
      const featuredItem = getUnusedItem(sortedItems)
      
      if (featuredItem) {
        const hasPromo = itemHasRelevantPromotion(featuredItem, promotions)
        markAsUsed(featuredItem)
        
        slides.push({
          id: slideId++,
          title: hasPromo 
            ? `${featuredItem.name} - ¡En Oferta!`
            : featuredItem.name,
          description: featuredItem.description || "Descubre este producto especial",
          image: featuredItem.image || "/placeholder.svg",
          action: { type: "product", productId: Number(featuredItem.id) },
        })
      }
    }
  }

  
  if (slides.length === 0) {
    slides.push({
      id: slideId++,
      title: "Bienvenido al Comedor FIUBA",
      description: "Explora nuestro menú y realiza tu pedido",
      image: "/placeholder.svg",
    })
  }

  return slides
}

