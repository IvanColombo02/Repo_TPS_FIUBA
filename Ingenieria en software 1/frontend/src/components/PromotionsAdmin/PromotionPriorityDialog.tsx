import { useCallback, useEffect, useMemo, useState } from "react"
import type { PromotionDTO } from "@/lib/api/promotions"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { ScrollArea } from "@/components/ui/scroll-area"
import { Badge } from "@/components/ui/badge"
import { AlertCircle, GripVertical, Loader2, RefreshCcw, ArrowUpDown, Info } from "lucide-react"
import { cn } from "@/lib/utils"

interface PromotionPriorityDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  fetchPromotions: () => Promise<PromotionDTO[]>
  onSaveOrder: (orderedIds: number[]) => Promise<void>
}

export function PromotionPriorityDialog({
  open,
  onOpenChange,
  fetchPromotions,
  onSaveOrder,
}: PromotionPriorityDialogProps) {
  const [items, setItems] = useState<PromotionDTO[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [draggingId, setDraggingId] = useState<number | null>(null)
  const [saving, setSaving] = useState(false)

  const loadItems = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await fetchPromotions()
      setItems(data)
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudieron cargar las promociones")
    } finally {
      setLoading(false)
    }
  }, [fetchPromotions])

  useEffect(() => {
    if (open) {
      loadItems().catch((err) => {
        console.error("Error loading promotions for priorities", err)
      })
    } else {
      setItems([])
      setError(null)
      setDraggingId(null)
    }
  }, [open, loadItems])

  const handleReorder = useCallback(
    (targetId: number) => {
      if (draggingId === null || draggingId === targetId) return

      setItems((prev) => {
        const next = [...prev]
        const fromIndex = next.findIndex((item) => item.id === draggingId)
        const toIndex = next.findIndex((item) => item.id === targetId)
        if (fromIndex === -1 || toIndex === -1) return prev
        const [moved] = next.splice(fromIndex, 1)
        next.splice(toIndex, 0, moved)
        return next
      })
    },
    [draggingId],
  )

  const handleSave = useCallback(async () => {
    if (!items.length) {
      setError("No hay promociones para ordenar")
      return
    }
    setSaving(true)
    setError(null)
    try {
      await onSaveOrder(items.map((item) => item.id))
      onOpenChange(false)
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error al guardar prioridades")
    } finally {
      setSaving(false)
    }
  }, [items, onSaveOrder, onOpenChange])

  const disabled = useMemo(() => loading || saving, [loading, saving])

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-3xl">
        <DialogHeader>
          <div className="flex items-center gap-2">
            <ArrowUpDown className="h-5 w-5 text-primary" />
            <DialogTitle>Orden de ejecución de promociones</DialogTitle>
          </div>
          <DialogDescription className="flex items-start gap-2 pt-2">
            <Info className="h-4 w-4 text-muted-foreground mt-0.5 flex-shrink-0" />
            <span>
              Arrastrá y soltá las promociones activas para definir el orden de ejecución. Las promociones con menor número se evalúan primero. Solo se muestran las promociones que están actualmente vigentes.
            </span>
          </DialogDescription>
        </DialogHeader>

        {error && (
          <div className="flex items-center gap-2 rounded-md border border-destructive/40 bg-destructive/10 px-4 py-3 text-sm text-destructive">
            <AlertCircle className="h-4 w-4 flex-shrink-0" />
            <span className="flex-1">{error}</span>
            {!loading && (
              <Button variant="ghost" size="sm" className="h-7 px-2" onClick={loadItems}>
                <RefreshCcw className="h-3.5 w-3.5 mr-1.5" />
                Reintentar
              </Button>
            )}
          </div>
        )}

        <div className="rounded-lg border bg-card shadow-sm">
          {loading ? (
            <div className="flex flex-col items-center justify-center gap-3 py-16 text-sm text-muted-foreground">
              <Loader2 className="h-6 w-6 animate-spin text-primary" />
              <span>Cargando promociones...</span>
            </div>
          ) : items.length === 0 ? (
            <div className="py-16 text-center">
              <div className="flex flex-col items-center gap-2 text-sm text-muted-foreground">
                <ArrowUpDown className="h-8 w-8 opacity-50" />
                <p className="font-medium">No hay promociones activas</p>
                <p className="text-xs">Solo se muestran las promociones que están actualmente vigentes</p>
              </div>
            </div>
          ) : (
            <ScrollArea className="h-[400px]">
              <div className="p-2">
                <ul className="space-y-2">
                  {items.map((promotion, index) => {
                    const isDragging = draggingId === promotion.id
                    const isOver = draggingId !== null && draggingId !== promotion.id
                    
                    return (
                  
                      <li
                        key={promotion.id}
                        draggable
                        onDragStart={() => setDraggingId(promotion.id)}
                        onDragEnd={() => setDraggingId(null)}
                        onDrop={(event) => {
                          event.preventDefault()
                          handleReorder(promotion.id)
                        }}
                        onDragOver={(event) => {
                          event.preventDefault()
                          event.dataTransfer.dropEffect = "move"
                        }}
                        className={cn(
                          "group relative flex items-center gap-3 rounded-lg border bg-background px-4 py-3 transition-all duration-200",
                          "hover:border-primary/50 hover:shadow-md",
                          isDragging && "opacity-50 cursor-grabbing border-primary shadow-lg",
                          isOver && "border-primary/30 bg-primary/5"
                        )}
                      >
                        <div className={cn(
                          "flex items-center justify-center w-8 h-8 rounded-md transition-colors",
                          "bg-muted group-hover:bg-primary/10",
                          isDragging && "bg-primary/20"
                        )}>
                          <GripVertical className={cn(
                            "h-4 w-4 text-muted-foreground transition-colors",
                            "group-hover:text-primary",
                            isDragging && "text-primary"
                          )} />
                        </div>
                        
                        <Badge 
                          variant="secondary" 
                          className={cn(
                            "w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold transition-colors",
                            "bg-primary/10 text-primary border-primary/20",
                            isDragging && "bg-primary/20"
                          )}
                        >
                          {index + 1}
                        </Badge>
                        
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2 mb-1">
                            <span className="font-semibold text-foreground truncate">
                              {promotion.name}
                            </span>
                          </div>
                          <div className="flex items-center gap-2 text-xs text-muted-foreground">
                            <span>
                              {new Date(promotion.fromDate).toLocaleDateString("es-AR", {
                                day: "2-digit",
                                month: "short",
                                year: "numeric"
                              })}
                            </span>
                            <span>•</span>
                            <span>
                              {new Date(promotion.toDate).toLocaleDateString("es-AR", {
                                day: "2-digit",
                                month: "short",
                                year: "numeric"
                              })}
                            </span>
                          </div>
                        </div>
                        
                        {isDragging && (
                          <div className="absolute inset-0 rounded-lg border-2 border-dashed border-primary bg-primary/5" />
                        )}
                      </li>
                    )
                  })}
                </ul>
              </div>
            </ScrollArea>
          )}
        </div>

        <DialogFooter className="gap-2 sm:gap-0">
          <Button variant="outline" onClick={() => loadItems()} disabled={disabled}>
            <RefreshCcw className={cn("mr-2 h-4 w-4", loading && "animate-spin")} />
            Refrescar
          </Button>
          <Button onClick={handleSave} disabled={disabled || items.length === 0}>
            {saving ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Guardando...
              </>
            ) : (
              "Guardar orden"
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
