"use client"

import { Percent, TrendingUp, Gift, Clock, Mail } from "lucide-react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import type { PromotionDTO } from "@/lib/api/promotions"
import { getPromotionConditionDescription } from "@/lib/utils/promotions"

interface PromotionsSectionProps {
  promotions: PromotionDTO[]
  loading?: boolean
}

const getPromotionIcon = (actionType: string, conditionType?: string) => {
  if (conditionType === "time") {
    return <Clock className="h-5 w-5" />
  }
  if (conditionType === "mailContains") {
    return <Mail className="h-5 w-5" />
  }

  if (actionType?.includes("Discount")) {
    return <Percent className="h-5 w-5" />
  }
  if (actionType?.includes("freeProduct") || actionType?.includes("Product")) {
    return <Gift className="h-5 w-5" />
  }
  return <TrendingUp className="h-5 w-5" />
}

const getPromotionBadge = (_actionType: string, expression: string): string | null => {
  try {
    const parsed = JSON.parse(expression)
    const action = parsed.action

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
    // Ignore parsing errors
  }
  return null
}

export function PromotionsSection({ promotions, loading = false }: PromotionsSectionProps) {
  if (loading) {
    return (
      <section className="border-b border-border bg-background">
        <div className="container mx-auto px-4 py-6 md:py-8">
          <h3 className="text-xl md:text-2xl lg:text-3xl font-bold text-foreground mb-4 md:mb-6">Promociones Activas</h3>
          <p className="text-sm text-muted-foreground">Cargando promociones...</p>
        </div>
      </section>
    )
  }

  if (promotions.length === 0) {
    return (
      <section className="border-b border-border bg-background">
        <div className="container mx-auto px-4 py-6 md:py-8">
          <h3 className="text-xl md:text-2xl lg:text-3xl font-bold text-foreground mb-4 md:mb-6">Promociones Activas</h3>
          <p className="text-sm text-muted-foreground">Sin promociones activas</p>
        </div>
      </section>
    )
  }

  return (
    <section className="border-b border-border bg-background">
      <div className="container mx-auto px-4 py-6 md:py-8">
        <h3 className="text-xl md:text-2xl lg:text-3xl font-bold text-foreground mb-4 md:mb-6">Promociones Activas</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {promotions.map((promo) => {
            let actionType = ""
            let conditionType = ""
            try {
              const parsed = JSON.parse(promo.expression)
              actionType = parsed.action?.type || ""
              conditionType = parsed.condition?.type || ""
            } catch {
              // Ignore errors
            }

            const badge = getPromotionBadge(actionType, promo.expression)
            const conditionDescription = getPromotionConditionDescription(promo)

            return (
              <Card key={promo.id} className="border-2 border-primary/20 flex flex-col">
                <CardHeader className="pb-3">
                  <div className="flex items-start justify-between gap-2">
                    <div className="flex items-center gap-2">
                      {getPromotionIcon(actionType, conditionType)}
                      <CardTitle className="text-base md:text-lg text-foreground">{promo.name}</CardTitle>
                    </div>
                    {badge && (
                      <Badge variant="secondary" className="shrink-0 bg-primary text-primary-foreground">
                        {badge}
                      </Badge>
                    )}
                  </div>
                </CardHeader>
                <CardContent className="flex-1 flex flex-col gap-2">
                  <CardDescription className="text-sm text-muted-foreground">{promo.description}</CardDescription>
                  {conditionDescription && (
                    <p className="text-xs text-primary/80 italic">
                      📋 {conditionDescription}
                    </p>
                  )}
                </CardContent>
                {promo.base64Image && (
                  <div className="w-full h-32 overflow-hidden">
                    <img
                      src={promo.base64Image}
                      alt={promo.name}
                      className="w-full h-full object-cover"
                    />
                  </div>
                )}
              </Card>
            )
          })}
        </div>
      </div>
    </section>
  )
}
