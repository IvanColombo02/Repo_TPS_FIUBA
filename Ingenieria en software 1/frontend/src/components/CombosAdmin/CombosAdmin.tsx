import { Combo, ComboCreateForm, ComboUpdateForm } from "@/models/Combo";
import { useCombos } from "@/hooks/use-combos";
import { fetchCombosSimplePage, fetchComboById, type ComboSimpleDTO } from "@/lib/api/combos";
import { useToken } from "@/services/TokenContext";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ItemsDropdown } from "@/components/ui/ItemsDropdown";
import { ComboCreateFormComponent } from "@/components/CombosAdmin/ComboCreateForm";
import { ComboEditFormComponent } from "@/components/CombosAdmin/ComboEditForm";
import { useCallback, useEffect, useState } from "react";
import { PaginationToolbar } from "@/components/ui/PaginationToolbar";

interface CombosAdminProps {
  onRefreshRef?: React.MutableRefObject<((productIds?: number[]) => void) | null>;
}

export function CombosAdmin({ onRefreshRef }: CombosAdminProps) {
  const {
    combos,
    loading,
    isSubmitting,
    addCombo,
    removeCombo,
    updateCombo,
    recalculateComboStocks,
    pagination,
    setPage,
    updateQuery,
  } = useCombos({ mode: "paginated", pageSize: 10 });

  useEffect(() => {
    if (onRefreshRef) {
      onRefreshRef.current = recalculateComboStocks;
    }
  }, [onRefreshRef, recalculateComboStocks]);

  const [tokenState] = useToken();
  const accessToken = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : null;
  const [simpleCombos, setSimpleCombos] = useState<ComboSimpleDTO[]>([]);
  const [simpleLoading, setSimpleLoading] = useState(false);
  const [simplePagination, setSimplePagination] = useState<{ page: number; size: number; totalElements: number; totalPages: number } | null>(null);

  const loadSimpleCombos = useCallback(async (page = 0) => {
    if (!accessToken) return;
    setSimpleLoading(true);
    try {
      const resp = await fetchCombosSimplePage(accessToken, { page, size: 10 });
      setSimpleCombos(resp.content);
      setSimplePagination({ page: resp.number, size: resp.size, totalElements: resp.totalElements, totalPages: resp.totalPages });
    } catch (e) {
      console.error('Error loading simple combos:', e);
      setSimpleCombos([]);
      setSimplePagination(null);
    } finally {
      setSimpleLoading(false);
    }
  }, [accessToken]);

  useEffect(() => {
    loadSimpleCombos(0);
  }, [accessToken, loadSimpleCombos]);

  const [editingCombo, setEditingCombo] = useState<Combo | null>(null);

  useEffect(() => {
    if (editingCombo) {
      const updatedCombo = combos.find(c => c.id === editingCombo.id);
      if (updatedCombo && updatedCombo.stock !== editingCombo.stock) {
        setEditingCombo(updatedCombo);
      }
    }
  }, [combos, editingCombo]);

  const handleCreateCombo = async (data: ComboCreateForm) => {
    await addCombo(data);
  };

  const handleEditCombo = async (data: ComboUpdateForm) => {
    if (editingCombo) {
      await updateCombo(editingCombo.id, data);
      setEditingCombo(null);
    }
  };

  const handleSearch = useCallback(async (value: string) => {
    const q = value.trim() || undefined;
    await updateQuery({ name: q });
    await loadSimpleCombos(0);
  }, [updateQuery, loadSimpleCombos]);

  return (
    <div className="space-y-6">
      {!editingCombo && (
        <Card>
          <CardHeader className="pb-4">
            <CardTitle className="text-lg">Nuevo Combo</CardTitle>
          </CardHeader>
          <CardContent>
            <ComboCreateFormComponent
              onSave={handleCreateCombo}
              isSubmitting={isSubmitting}
            />
          </CardContent>
        </Card>
      )}

      {editingCombo && (
        <Card data-edit-form>
          <CardHeader className="pb-4">
            <CardTitle className="text-lg">Editar Combo: {editingCombo.name}</CardTitle>
          </CardHeader>
          <CardContent>
            <ComboEditFormComponent
              combo={editingCombo}
              onSave={handleEditCombo}
              isSubmitting={isSubmitting}
            />
            <div className="mt-4">
              <button
                onClick={() => {
                  setEditingCombo(null);
                  window.scrollTo({ top: 0, behavior: 'smooth' });
                }}
                className="text-sm text-gray-500 hover:text-gray-700"
              >
                ← Volver a crear combo
              </button>
            </div>
          </CardContent>
        </Card>
      )}

      <ItemsDropdown
        title="Combos"
        items={simpleCombos}
        loading={loading || simpleLoading}
        searchable={true}
        searchPlaceholder="Buscar combos por nombre..."
        onSearch={handleSearch}
        onEdit={(combo) => {
          try {
            (async () => {
              if (!accessToken) return;
              const full = await fetchComboById(accessToken, combo.id);
              setEditingCombo(full as Combo);
            })();
            setTimeout(() => {
              const editForm = document.querySelector('[data-edit-form]');
              if (editForm) {
                editForm.scrollIntoView({ behavior: 'smooth', block: 'start' });
              }
            }, 100);
          } catch (error) {
            console.error('Error al editar combo:', error);
          }
        }}
        onDelete={async (id) => { await removeCombo(id); await loadSimpleCombos(simplePagination?.page ?? 0); }}
        isDeleting={isSubmitting}
        emptyMessage="No hay combos creados aún."
        renderItem={(item) => (
          <div className="flex items-center gap-4">
            {item.base64Image && (
              <img src={item.base64Image} alt={item.name} loading="lazy" decoding="async" className="w-10 h-10 object-cover rounded-md" />
            )}
            <div>
              <span className="font-medium text-white">{item.name}</span>
              <span className="text-sm text-white/70 ml-2">
                ${item.price} - Stock: {item.stock}
              </span>
            </div>
          </div>
        )}
        totalItems={simplePagination?.totalElements ?? pagination?.totalElements}
      />

      {((pagination && pagination.totalElements > 0) || (simplePagination && simplePagination.totalElements > 0)) && (
        <PaginationToolbar
          page={pagination?.page ?? simplePagination?.page ?? 0}
          size={pagination?.size ?? simplePagination?.size ?? 10}
          totalPages={pagination?.totalPages ?? simplePagination?.totalPages ?? 0}
          totalElements={pagination?.totalElements ?? simplePagination?.totalElements ?? 0}
          onPageChange={async (page) => { await loadSimpleCombos(page); await setPage(page) }}
          disabled={loading || isSubmitting || simpleLoading}
          className="bg-background"
        />
      )}
    </div>
  );
}
