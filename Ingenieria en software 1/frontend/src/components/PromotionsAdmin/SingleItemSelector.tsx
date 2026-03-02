import { useState, useEffect, useMemo } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog"
import { Search, Check } from "lucide-react"
import { useProducts } from "@/hooks/use-products"
import { useCombos } from "@/hooks/use-combos"

export interface SelectableItem {
  id: number
  name: string
  base64Image?: string
  description?: string
  price?: number
  stock?: number
  categories?: string[]
  type?: string
  isCombo?: boolean
}

interface SingleItemSelectorProps {
  isOpen: boolean
  onClose: () => void
  onSelect: (item: SelectableItem | null) => void
  initialItemId?: number | null
  allowCombos?: boolean
  allowProducts?: boolean
  title: string
  description: string
}

export function SingleItemSelector({
  isOpen,
  onClose,
  onSelect,
  initialItemId,
  allowCombos = true,
  allowProducts = true,
  title,
  description,
}: SingleItemSelectorProps) {
  const { products, loading: loadingProducts } = useProducts({ mode: "all" })
  const { combos, loading: loadingCombos } = useCombos({ mode: "all" })
  const [searchTerm, setSearchTerm] = useState("")
  const [selectedItem, setSelectedItem] = useState<SelectableItem | null>(null)

  const loading = loadingProducts || loadingCombos

  const allItems: SelectableItem[] = useMemo(
    () => [
      ...(allowProducts ? products.map((p) => ({ ...p, isCombo: false })) : []),
      ...(allowCombos ? combos.map((c) => ({ ...c, isCombo: true })) : []),
    ],
    [products, combos, allowProducts, allowCombos]
  )

  useEffect(() => {
    if (isOpen) {
      if (initialItemId) {
        const item = allItems.find((i) => i.id === initialItemId)
        setSelectedItem(item || null)
      } else {
        setSelectedItem(null)
      }
      setSearchTerm("")
    }
  }, [isOpen, initialItemId, allItems])

  const filteredItems = allItems.filter((item) =>
    item.name.toLowerCase().includes(searchTerm.toLowerCase())
  )

  const handleSelect = (item: SelectableItem) => {
    setSelectedItem(item)
  }

  const handleSave = () => {
    onSelect(selectedItem)
    onClose()
  }

  const handleClose = () => {
    setSelectedItem(null)
    onClose()
  }

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="max-w-2xl max-h-[80vh] overflow-hidden flex flex-col">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          <DialogDescription>{description}</DialogDescription>
        </DialogHeader>

        <div className="flex-1 overflow-hidden flex flex-col space-y-4">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
            <Input
              placeholder="Buscar productos o combos..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>

          <div className="flex-1 overflow-y-auto space-y-2">
            {loading ? (
              <div className="flex items-center justify-center py-8">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
              </div>
            ) : filteredItems.length === 0 ? (
              <div className="text-center py-8">
                <p className="text-muted-foreground">No se encontraron items</p>
              </div>
            ) : (
              filteredItems.map((item) => {
                const isSelected = selectedItem?.id === item.id

                return (
                  <div
                    key={`${item.isCombo ? "combo" : "product"}-${item.id}`}
                    className={`flex items-center gap-3 p-3 rounded-lg border cursor-pointer transition-colors ${
                      isSelected
                        ? "border-primary bg-primary/10"
                        : "border-border hover:border-primary/50 hover:bg-primary/5"
                    }`}
                    onClick={() => handleSelect(item)}
                  >
                    {item.base64Image ? (
                      <img
                        src={item.base64Image}
                        alt={item.name}
                        className="w-12 h-12 object-cover rounded-md flex-shrink-0"
                      />
                    ) : (
                      <div className="w-12 h-12 bg-gray-700 rounded-md flex items-center justify-center text-sm text-white flex-shrink-0">
                        IMG
                      </div>
                    )}

                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2">
                        <span className="font-medium break-words">{item.name}</span>
                        {item.isCombo && (
                          <span className="text-xs px-2 py-0.5 bg-primary/20 text-primary rounded">
                            Combo
                          </span>
                        )}
                        {isSelected && <Check className="h-4 w-4 text-primary flex-shrink-0" />}
                      </div>
                      {item.description && (
                        <p className="text-sm text-muted-foreground mt-1 line-clamp-1">
                          {item.description}
                        </p>
                      )}
                      {item.price !== undefined && (
                        <p className="text-sm font-medium mt-1">${item.price}</p>
                      )}
                    </div>
                  </div>
                )
              })
            )}
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={handleClose}>
            Cancelar
          </Button>
          <Button onClick={handleSave} disabled={!selectedItem}>
            Seleccionar
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}


