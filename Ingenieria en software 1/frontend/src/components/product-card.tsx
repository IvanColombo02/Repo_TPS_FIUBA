"use client"

import { Eye, Clock, Gift } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import type { MenuItem } from "@/lib/types"
import type { PromotionDTO } from "@/lib/api/promotions"

interface ProductCardProps {
  item: MenuItem
  onAddToCart: (item: MenuItem) => void
  onViewDetails: (item: MenuItem) => void
  promotions?: PromotionDTO[]
}

const getPromotionBadge = (promotion: PromotionDTO): string | null => {
  try {
    const expression = JSON.parse(promotion.expression)
    const action = expression.action

    if (action?.type === "percentageDiscount" && action?.percentage) {
      return `-${action.percentage}%`
    }
    if (action?.type === "fixedDiscount" && action?.amount) {
      return `-$${action.amount}`
    }
    if (action?.type === "quantityDiscount") {
      return `${action.buyQuantity}x${action.payQuantity}`
    }
    if (action?.type === "freeProduct") {
      return "Gratis"
    }
  } catch {
    // Ignorar errores de parsing
  }
  return "Promo"
}

export function ProductCard({ item, onAddToCart, onViewDetails, promotions = [] }: ProductCardProps) {
  const promotionBadge = promotions.length > 0 ? getPromotionBadge(promotions[0]) : null

  return (
    <Card className="overflow-hidden flex flex-col">
      <div className="relative">
        <img
          src={item.image || "/placeholder.svg"}
          alt={item.name}
          loading="lazy"
          decoding="async"
          className={`w-full h-48 object-cover ${!item.available ? "grayscale opacity-60" : ""}`}
        />
        {promotions.length > 0 && item.available && promotionBadge && (
          <Badge className="absolute top-2 right-2 flex items-center gap-1 bg-primary text-primary-foreground">
            <Gift className="h-3 w-3" />
            {promotionBadge}
          </Badge>
        )}
        {!item.available && (
          <Badge variant="destructive" className="absolute top-2 right-2">
            No disponible
          </Badge>
        )}
      </div>
      <CardHeader>
        <div className="flex items-start justify-between gap-2">
          <CardTitle className="text-lg text-foreground">{item.name}</CardTitle>
          <Badge variant="secondary" className="shrink-0">
            {item.category}
          </Badge>
        </div>
        <CardDescription className="text-muted-foreground">{item.description}</CardDescription>
        <div className="flex items-center gap-1 text-sm text-muted-foreground mt-2">
          <Clock className="h-4 w-4" />
          <span>{item.preparationTime} min</span>
        </div>
      </CardHeader>
      <CardContent className="flex-1">
        <div className="flex items-center gap-2">
          <p className="text-2xl font-bold text-primary">${item.price.toFixed(2)} ARS</p>
        </div>
      </CardContent>
      <CardFooter className="flex gap-2">
        <Button
          variant="outline"
          className="flex-1 text-foreground hover:text-foreground bg-transparent"
          onClick={() => onViewDetails(item)}
          disabled={!item.available}
        >
          <Eye className="h-4 w-4 mr-2" />
          Ver detalles
        </Button>
        <Button className="flex-1" onClick={() => onAddToCart(item)} disabled={!item.available}>
          Añadir
        </Button>
      </CardFooter>
    </Card>
  )
}
