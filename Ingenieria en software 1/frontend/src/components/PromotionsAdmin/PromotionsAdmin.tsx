import { useCallback, useEffect, useMemo, useState } from "react"
import { PaginationToolbar } from "@/components/ui/PaginationToolbar"
import { Search, Edit, Trash2, Tag, Calendar, Clock, Sparkles, Plus, TrendingUp, TrendingDown, ListOrdered, Info } from "lucide-react"
import type { PromotionDTO } from "@/lib/api/promotions"
import { Badge } from "@/components/ui/badge"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import type { PromotionCreateForm, PromotionUpdateForm } from "@/models/Promotion"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { usePromotions } from "@/hooks/use-promotions"
import { PromotionFormComponent } from "./PromotionForm"
import { PromotionPriorityDialog } from "./PromotionPriorityDialog"
import { getPromotionConditionDescription } from "@/lib/utils/promotions"

type EditablePromotion = PromotionDTO

interface PromotionsAdminProps {
  onRefreshRef?: React.MutableRefObject<(() => Promise<void>) | null>
}

const formatDate = (dateString: string) =>
  new Date(dateString).toLocaleDateString("es-AR", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  })

export function PromotionsAdmin({ onRefreshRef }: PromotionsAdminProps) {
  const [error, setError] = useState<string | null>(null)
  const [activeFilter, setActiveFilter] = useState<"ALL" | "ACTIVE" | "INACTIVE">("ALL")
  const {
    promotions,
    loading,
    isSubmitting,
    addPromotion,
    removePromotion,
    updatePromotion,
    pagination,
    setPage,
    updateQuery,
    loadPrioritizablePromotions,
    reorderPromotions,
  } = usePromotions({ mode: "paginated", pageSize: 10 })

  useEffect(() => {
    if (error) {
      const timer = setTimeout(() => setError(null), 5000)
      return () => clearTimeout(timer)
    }
  }, [error])

  useEffect(() => {
    if (onRefreshRef) {
      onRefreshRef.current = async () => {
        // This will be handled by the hook
      }
    }
    return () => {
      if (onRefreshRef) {
        onRefreshRef.current = null
      }
    }
  }, [onRefreshRef])

  const [editingPromotion, setEditingPromotion] = useState<EditablePromotion | null>(null)
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [promotionToDelete, setPromotionToDelete] = useState<number | null>(null)
  const [searchQuery, setSearchQuery] = useState("")
  const [showForm, setShowForm] = useState(false)
  const [priorityDialogOpen, setPriorityDialogOpen] = useState(false)

  useEffect(() => {
    if (editingPromotion) {
      const updated = promotions.find((p) => p.id === editingPromotion.id)
      if (updated) {
        setEditingPromotion(updated as EditablePromotion)
      }
    }
  }, [promotions, editingPromotion])

  const handleCreatePromotion = async (data: PromotionCreateForm | PromotionUpdateForm) => {
    try {
      await addPromotion(data as PromotionCreateForm)
      setError(null)
      setShowForm(false)
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error al crear promoción")
    }
  }

  const handleEditPromotion = async (data: PromotionCreateForm | PromotionUpdateForm) => {
    if (editingPromotion) {
      try {
        await updatePromotion(editingPromotion.id, data as PromotionUpdateForm)
        setEditingPromotion(null)
        setError(null)
      } catch (err) {
        setError(err instanceof Error ? err.message : "Error al actualizar promoción")
      }
    }
  }

  const handleDeleteClick = (id: number) => {
    setPromotionToDelete(id)
    setDeleteDialogOpen(true)
  }

  const handleDeleteConfirm = async () => {
    if (promotionToDelete) {
      try {
        await removePromotion(promotionToDelete)
        setDeleteDialogOpen(false)
        setPromotionToDelete(null)
        setError(null)
      } catch (err) {
        setError(err instanceof Error ? err.message : "Error al eliminar promoción")
        setDeleteDialogOpen(false)
      }
    }
  }

  const handleSearch = useCallback(
    async (value: string) => {
      setSearchQuery(value)
      await updateQuery({ name: value.trim() || undefined })
    },
    [updateQuery]
  )

  const isPromotionActive = (promotion: PromotionDTO): boolean => {
    const today = new Date()
    const todayStr = today.toISOString().split('T')[0] // YYYY-MM-DD

    return todayStr >= promotion.fromDate && todayStr <= promotion.toDate
  }

  const metrics = useMemo(() => {
    const total = promotions.length
    const active = promotions.filter((p) => isPromotionActive(p)).length
    const inactive = total - active
    return { total, active, inactive }
  }, [promotions])

  const filteredPromotions = useMemo(() => {
    if (activeFilter === "ALL") {
      return promotions
    }
    return promotions.filter((p) => {
      const active = isPromotionActive(p)
      return activeFilter === "ACTIVE" ? active : !active
    })
  }, [promotions, activeFilter])

  const effectivePagination = useMemo(() => {
    if (activeFilter === "ALL" && pagination) {
      return pagination
    }
    const totalElements = filteredPromotions.length
    const size = pagination?.size ?? 10
    const totalPages = Math.ceil(totalElements / size)
    const currentPage = 0
    return {
      page: currentPage,
      size,
      totalPages,
      totalElements,
      first: currentPage === 0,
      last: totalPages === 0 || currentPage === totalPages - 1,
    }
  }, [activeFilter, pagination, filteredPromotions.length])

  return (
    <div className="space-y-8">
      {error && (
        <Card className="border-destructive">
          <CardContent className="pt-6">
            <p className="text-sm text-destructive">{error}</p>
          </CardContent>
        </Card>
      )}


      <section className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Total de promociones</CardDescription>
            <CardTitle className="text-3xl text-foreground">{metrics.total}</CardTitle>
          </CardHeader>
          <CardContent className="flex items-center gap-2 text-muted-foreground">
            <Tag className="h-4 w-4" />
            Todas las promociones
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Promociones activas</CardDescription>
            <CardTitle className="text-3xl text-green-500">{metrics.active}</CardTitle>
          </CardHeader>
          <CardContent className="flex items-center gap-2 text-muted-foreground">
            <TrendingUp className="h-4 w-4 text-green-500" />
            En vigencia actualmente
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Promociones inactivas</CardDescription>
            <CardTitle className="text-3xl text-muted-foreground">{metrics.inactive}</CardTitle>
          </CardHeader>
          <CardContent className="flex items-center gap-2 text-muted-foreground">
            <TrendingDown className="h-4 w-4 text-muted-foreground" />
            Fuera de vigencia
          </CardContent>
        </Card>
      </section>

      {(editingPromotion || showForm) && (
        <Card className="border-primary/20" data-edit-form>
          <CardHeader className="pb-4">
            <div className="flex items-center justify-between">
              <div>
                <CardTitle className="text-lg">
                  {editingPromotion ? "Editar Promoción" : "Nueva Promoción"}
                </CardTitle>
                <CardDescription>
                  {editingPromotion ? "Actualiza los datos de la promoción" : "Completa el formulario para crear una nueva promoción"}
                </CardDescription>
              </div>
              <Button
                variant="ghost"
                size="sm"
                onClick={() => {
                  setEditingPromotion(null)
                  setShowForm(false)
                }}
              >
                Cerrar
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            <PromotionFormComponent
              onSave={editingPromotion ? handleEditPromotion : handleCreatePromotion}
              isSubmitting={isSubmitting}
              initialData={editingPromotion}
            />
          </CardContent>
        </Card>
      )}


      <section>
        <div className="flex items-center justify-between mb-6">
          <div>
            <h2 className="text-2xl font-bold text-foreground">Promociones</h2>
            <p className="text-sm text-muted-foreground mt-1">Gestiona tus promociones activas e inactivas</p>
          </div>
          <div className="flex items-center gap-3">
            <div className="relative">
              <Search className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Buscar por nombre..."
                value={searchQuery}
                onChange={(e) => handleSearch(e.target.value)}
                className="pl-9 w-64"
              />
            </div>
            <Button variant="secondary" onClick={() => setPriorityDialogOpen(true)} className="gap-2">
              <ListOrdered className="h-4 w-4" />
              Prioridades
            </Button>
            {!showForm && !editingPromotion && (
              <Button onClick={() => setShowForm(true)} className="gap-2">
                <Plus className="h-4 w-4" />
                Nueva Promoción
              </Button>
            )}
          </div>
        </div>

        <Tabs value={activeFilter} onValueChange={(v) => setActiveFilter(v as typeof activeFilter)} className="space-y-4">
          <TabsList>
            <TabsTrigger value="ALL">
              Todas
              <Badge variant="secondary" className="ml-2">
                {metrics.total}
              </Badge>
            </TabsTrigger>
            <TabsTrigger value="ACTIVE">
              Activas
              <Badge variant="default" className="ml-2 bg-green-500">
                {metrics.active}
              </Badge>
            </TabsTrigger>
            <TabsTrigger value="INACTIVE">
              Inactivas
              <Badge variant="outline" className="ml-2">
                {metrics.inactive}
              </Badge>
            </TabsTrigger>
          </TabsList>

          <TabsContent value={activeFilter} className="space-y-4">
            {loading ? (
              <div className="flex justify-center items-center py-12">
                <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent" />
              </div>
            ) : filteredPromotions.length === 0 ? (
              <Card className="border-dashed">
                <CardContent className="py-12 text-center">
                  <Tag className="h-12 w-12 text-muted-foreground mx-auto mb-4 opacity-50" />
                  <p className="text-lg font-medium text-foreground mb-2">
                    {activeFilter === "ALL"
                      ? "No hay promociones disponibles"
                      : activeFilter === "ACTIVE"
                        ? "No hay promociones activas"
                        : "No hay promociones inactivas"}
                  </p>
                  <p className="text-sm text-muted-foreground mb-4">
                    {activeFilter === "ALL" && "Crea una nueva promoción para comenzar"}
                  </p>
                  {activeFilter === "ALL" && !showForm && (
                    <Button onClick={() => setShowForm(true)} className="gap-2">
                      <Plus className="h-4 w-4" />
                      Crear Promoción
                    </Button>
                  )}
                </CardContent>
              </Card>
            ) : (
              <>
                <div className="space-y-4">
                  {filteredPromotions.map((promotion) => {
                    const active = isPromotionActive(promotion)
                    const conditionDescription = getPromotionConditionDescription(promotion)

                    return (
                      <Card
                        key={promotion.id}
                        className="border-border shadow-lg hover:shadow-xl transition-all duration-200"
                      >
                        <CardHeader className="pb-4">
                          <div className="flex items-start justify-between gap-4">
                            <div className="flex-1">
                              <div className="flex items-center gap-3 mb-2">
                                <Tag className={`h-5 w-5 flex-shrink-0 ${active ? "text-green-500" : "text-muted-foreground"}`} />
                                <CardTitle className="text-lg text-foreground">{promotion.name}</CardTitle>
                                {active ? (
                                  <Badge variant="default" className="bg-green-500 hover:bg-green-600 flex items-center gap-1">
                                    <Sparkles className="h-3 w-3" />
                                    Activa
                                  </Badge>
                                ) : (
                                  <Badge variant="secondary" className="flex items-center gap-1">
                                    <Clock className="h-3 w-3" />
                                    Inactiva
                                  </Badge>
                                )}
                              </div>
                              {promotion.description && (
                                <CardDescription className="text-sm mt-2">{promotion.description}</CardDescription>
                              )}
                              {conditionDescription && (
                                <div className="flex items-center gap-2 mt-2 text-xs text-primary bg-primary/10 px-2 py-1 rounded-md w-fit">
                                  <Info className="h-3.5 w-3.5" />
                                  <span className="font-medium">Condición:</span>
                                  <span>{conditionDescription}</span>
                                </div>
                              )}
                              <div className="flex items-center gap-4 mt-3 text-xs text-muted-foreground">
                                <div className="flex items-center gap-1">
                                  <Calendar className="h-3.5 w-3.5" />
                                  <span>
                                    {formatDate(promotion.fromDate)} - {formatDate(promotion.toDate)}
                                  </span>
                                </div>
                              </div>
                            </div>
                            <div className="flex items-center gap-2 flex-shrink-0">
                              <Button
                                variant="outline"
                                size="sm"
                                onClick={() => {
                                  setEditingPromotion(promotion as EditablePromotion)
                                  setShowForm(false)
                                  setTimeout(() => {
                                    const editForm = document.querySelector('[data-edit-form]')
                                    if (editForm) {
                                      editForm.scrollIntoView({ behavior: 'smooth', block: 'start' })
                                    }
                                  }, 100)
                                }}
                                className="hover:bg-primary/10"
                              >
                                <Edit className="h-4 w-4 mr-1" />
                                Editar
                              </Button>
                              <Button
                                variant="destructive"
                                size="sm"
                                onClick={() => handleDeleteClick(promotion.id)}
                                className="hover:bg-destructive/90"
                              >
                                <Trash2 className="h-4 w-4 mr-1" />
                                Eliminar
                              </Button>
                            </div>
                          </div>
                        </CardHeader>
                        {promotion.base64Image && (
                          <CardContent className="pt-0">
                            <div className="w-full h-48 overflow-hidden rounded-lg">
                              <img
                                src={promotion.base64Image}
                                alt={promotion.name}
                                className="w-full h-full object-cover"
                              />
                            </div>
                          </CardContent>
                        )}
                      </Card>
                    )
                  })}
                </div>

                {effectivePagination && (
                  <PaginationToolbar
                    page={effectivePagination.page}
                    size={effectivePagination.size}
                    totalPages={effectivePagination.totalPages}
                    totalElements={effectivePagination.totalElements}
                    onPageChange={activeFilter === "ALL" ? setPage : () => { }}
                    disabled={loading}
                  />
                )}
              </>
            )}
          </TabsContent>
        </Tabs>
      </section>

      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Estás seguro?</AlertDialogTitle>
            <AlertDialogDescription>
              Esta acción no se puede deshacer. Se eliminará permanentemente la promoción.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancelar</AlertDialogCancel>
            <AlertDialogAction onClick={handleDeleteConfirm} className="bg-destructive">
              Eliminar
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      <PromotionPriorityDialog
        open={priorityDialogOpen}
        onOpenChange={setPriorityDialogOpen}
        fetchPromotions={loadPrioritizablePromotions}
        onSaveOrder={async (orderedIds) => {
          await reorderPromotions(orderedIds)
        }}
      />
    </div>
  )
}


