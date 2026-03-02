"use client"

import { Search, SlidersHorizontal, DollarSign } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "@/components/ui/collapsible"
import type { FoodCategory } from "@/lib/types"

interface SearchFiltersProps {
  searchQuery: string
  onSearchChange: (query: string) => void
  showAvailableOnly: boolean
  onAvailableOnlyChange: (value: boolean) => void
  showPromotions: boolean
  onPromotionsChange: (value: boolean) => void
  selectedCategoryFilters: FoodCategory[]
  onCategoryFilterToggle: (category: FoodCategory) => void
  categorySearchQuery: string
  onCategorySearchChange: (query: string) => void
  filteredCategories: FoodCategory[]
  isFilterOpen: boolean
  onFilterOpenChange: (open: boolean) => void
  selectedTypeFilters: string[]
  onTypeFilterToggle: (type: string) => void
  typeSearchQuery: string
  onTypeSearchChange: (query: string) => void
  filteredTypes: string[]
  isTypeFilterOpen: boolean
  onTypeFilterOpenChange: (open: boolean) => void
  priceMin: string
  priceMax: string
  onPriceMinChange: (value: string) => void
  onPriceMaxChange: (value: string) => void
  isPriceFilterOpen: boolean
  onPriceFilterOpenChange: (open: boolean) => void
}

export function SearchFilters({
  searchQuery,
  onSearchChange,
  showAvailableOnly,
  onAvailableOnlyChange,
  showPromotions,
  onPromotionsChange,
  selectedCategoryFilters,
  onCategoryFilterToggle,
  categorySearchQuery,
  onCategorySearchChange,
  filteredCategories,
  isFilterOpen,
  onFilterOpenChange,
  selectedTypeFilters,
  onTypeFilterToggle,
  typeSearchQuery,
  onTypeSearchChange,
  filteredTypes,
  isTypeFilterOpen,
  onTypeFilterOpenChange,
  priceMin,
  priceMax,
  onPriceMinChange,
  onPriceMaxChange,
  isPriceFilterOpen,
  onPriceFilterOpenChange,
}: SearchFiltersProps) {
  const hasPriceFilter = priceMin !== "" || priceMax !== ""
  const clearPriceFilter = () => {
    onPriceMinChange("")
    onPriceMaxChange("")
  }
  return (
    <div className="flex flex-col gap-4">
      <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
        <div className="relative flex-1 max-w-md w-full">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            type="search"
            placeholder="Buscar productos..."
            className="pl-10 text-foreground"
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
          />
        </div>
        <div className="flex flex-col gap-2">
          <Button
            variant={showAvailableOnly ? "default" : "outline"}
            onClick={() => onAvailableOnlyChange(!showAvailableOnly)}
            size="sm"
            className={showAvailableOnly ? "" : "text-foreground hover:text-foreground"}
          >
            Solo disponibles
          </Button>
          <Button
            variant={showPromotions ? "default" : "outline"}
            onClick={() => onPromotionsChange(!showPromotions)}
            size="sm"
            className={showPromotions ? "" : "text-foreground hover:text-foreground"}
          >
            Promociones
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <Collapsible open={isFilterOpen} onOpenChange={onFilterOpenChange} className="flex-1">
          <CollapsibleTrigger asChild>
            <Button
              variant="outline"
              className="w-full flex items-center gap-2 text-foreground hover:text-foreground bg-transparent"
            >
              <SlidersHorizontal className="h-4 w-4" />
              Filtrar por categoría
              {selectedCategoryFilters.length > 0 && (
                <Badge variant="secondary" className="ml-2">
                  {selectedCategoryFilters.length}
                </Badge>
              )}
            </Button>
          </CollapsibleTrigger>
          <CollapsibleContent className="mt-4">
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Seleccionar categorías</CardTitle>
                <CardDescription>Busca y selecciona las categorías que deseas ver</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                  <Input
                    type="search"
                    placeholder="Buscar categorías..."
                    className="pl-10 text-foreground"
                    value={categorySearchQuery}
                    onChange={(e) => onCategorySearchChange(e.target.value)}
                  />
                </div>
                <div className="flex flex-wrap gap-2">
                  {filteredCategories.map((category) => (
                    <Button
                      key={category}
                      variant={selectedCategoryFilters.includes(category) ? "default" : "outline"}
                      onClick={() => onCategoryFilterToggle(category)}
                      size="sm"
                      className={
                        selectedCategoryFilters.includes(category) ? "" : "text-foreground hover:text-foreground"
                      }
                    >
                      {category}
                    </Button>
                  ))}
                  {filteredCategories.length === 0 && (
                    <p className="text-sm text-muted-foreground">No se encontraron categorías</p>
                  )}
                </div>
                {selectedCategoryFilters.length > 0 && (
                  <Button
                    variant="ghost"
                    onClick={() => selectedCategoryFilters.forEach(onCategoryFilterToggle)}
                    size="sm"
                    className="text-muted-foreground hover:text-foreground"
                  >
                    Limpiar selección
                  </Button>
                )}
              </CardContent>
            </Card>
          </CollapsibleContent>
        </Collapsible>

        <Collapsible open={isTypeFilterOpen} onOpenChange={onTypeFilterOpenChange} className="flex-1">
          <CollapsibleTrigger asChild>
            <Button
              variant="outline"
              className="w-full flex items-center gap-2 text-foreground hover:text-foreground bg-transparent"
            >
              <SlidersHorizontal className="h-4 w-4" />
              Filtrar por tipo
              {selectedTypeFilters.length > 0 && (
                <Badge variant="secondary" className="ml-2">
                  {selectedTypeFilters.length}
                </Badge>
              )}
            </Button>
          </CollapsibleTrigger>
          <CollapsibleContent className="mt-4">
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Seleccionar tipos</CardTitle>
                <CardDescription>Busca y selecciona los tipos que deseas ver</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                  <Input
                    type="search"
                    placeholder="Buscar tipos..."
                    className="pl-10 text-foreground"
                    value={typeSearchQuery}
                    onChange={(e) => onTypeSearchChange(e.target.value)}
                  />
                </div>
                <div className="flex flex-wrap gap-2">
                  {filteredTypes.map((type) => (
                    <Button
                      key={type}
                      variant={selectedTypeFilters.includes(type) ? "default" : "outline"}
                      onClick={() => onTypeFilterToggle(type)}
                      size="sm"
                      className={
                        selectedTypeFilters.includes(type) ? "" : "text-foreground hover:text-foreground"
                      }
                    >
                      {type}
                    </Button>
                  ))}
                  {filteredTypes.length === 0 && (
                    <p className="text-sm text-muted-foreground">No se encontraron tipos</p>
                  )}
                </div>
                {selectedTypeFilters.length > 0 && (
                  <Button
                    variant="ghost"
                    onClick={() => selectedTypeFilters.forEach(onTypeFilterToggle)}
                    size="sm"
                    className="text-muted-foreground hover:text-foreground"
                  >
                    Limpiar selección
                  </Button>
                )}
              </CardContent>
            </Card>
          </CollapsibleContent>
        </Collapsible>

        <Collapsible open={isPriceFilterOpen} onOpenChange={onPriceFilterOpenChange} className="flex-1">
          <CollapsibleTrigger asChild>
            <Button
              variant="outline"
              className="w-full flex items-center gap-2 text-foreground hover:text-foreground bg-transparent"
            >
              <DollarSign className="h-4 w-4" />
              Filtrar por precio
              {hasPriceFilter && (
                <Badge variant="secondary" className="ml-2">
                  1
                </Badge>
              )}
            </Button>
          </CollapsibleTrigger>
          <CollapsibleContent className="mt-4">
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Rango de precio</CardTitle>
                <CardDescription>Ingresa el rango de precio en pesos argentinos (ARS)</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <label htmlFor="price-min" className="text-sm font-medium text-foreground">
                      Precio mínimo
                    </label>
                    <Input
                      id="price-min"
                      type="number"
                      min="0"
                      step="1"
                      placeholder="$0"
                      value={priceMin}
                      onChange={(e) => onPriceMinChange(e.target.value)}
                      className="text-foreground"
                    />
                  </div>
                  <div className="space-y-2">
                    <label htmlFor="price-max" className="text-sm font-medium text-foreground">
                      Precio máximo
                    </label>
                    <Input
                      id="price-max"
                      type="number"
                      min="0"
                      step="1"
                      placeholder="Sin límite"
                      value={priceMax}
                      onChange={(e) => onPriceMaxChange(e.target.value)}
                      className="text-foreground"
                    />
                  </div>
                </div>
                {hasPriceFilter && (
                  <Button
                    variant="ghost"
                    onClick={clearPriceFilter}
                    size="sm"
                    className="text-muted-foreground hover:text-foreground"
                  >
                    Limpiar filtro
                  </Button>
                )}
              </CardContent>
            </Card>
          </CollapsibleContent>
        </Collapsible>
      </div>
    </div>
  )
}
