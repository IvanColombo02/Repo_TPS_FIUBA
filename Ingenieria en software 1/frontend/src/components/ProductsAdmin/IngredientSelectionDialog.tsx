import { useIngredients } from "@/hooks/use-ingredients";
import { ItemSelectionDialog } from "@/components/ui/ItemSelectionDialog";
import { ChefHat } from "lucide-react";

interface IngredientSelectionDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (ingredients: Record<string, number>) => void;
  initialIngredients?: Record<string, number>;
}

export function IngredientSelectionDialog({
  isOpen,
  onClose,
  onSave,
  initialIngredients = {}
}: IngredientSelectionDialogProps) {
  const { ingredients, loading } = useIngredients(undefined, { enabled: isOpen, resetOnDisable: true });

  return (
    <ItemSelectionDialog
      isOpen={isOpen}
      onClose={onClose}
      onSave={onSave}
      initialItems={initialIngredients}
      items={ingredients}
      loading={loading}
      title="Seleccionar Ingredientes"
      description="Elige los ingredientes y sus cantidades para este producto."
      searchPlaceholder="Buscar ingredientes..."
      icon={<ChefHat className="h-5 w-5" />}
      emptyMessage="No hay ingredientes disponibles. Crea algunos ingredientes primero."
      noResultsMessage="No se encontraron ingredientes"
    />
  );
}
