import { useProducts } from "@/hooks/use-products";
import { ItemSelectionDialog } from "@/components/ui/ItemSelectionDialog";
import { Package } from "lucide-react";

interface ProductSelectionDialogProps {
  isOpen: boolean;
  onClose: () => void;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onSave: (products: Record<string, number>, selectedProductsData?: any[]) => void;
  initialProducts: Record<string, number>;
}

export function ProductSelectionDialog({
  isOpen,
  onClose,
  onSave,
  initialProducts,
}: ProductSelectionDialogProps) {
  const { products, loading } = useProducts({ enabled: isOpen, resetOnDisable: true });

  const handleSave = (selectedProducts: Record<string, number>) => {

    const selectedProductsData = Object.keys(selectedProducts)
      .map(id => products.find(p => p.id === parseInt(id)))
      .filter(Boolean);

    onSave(selectedProducts, selectedProductsData);
  };

  return (
    <ItemSelectionDialog
      isOpen={isOpen}
      onClose={onClose}
      onSave={handleSave}
      initialItems={initialProducts}
      items={products}
      loading={loading}
      title="Seleccionar Productos para el Combo"
      description="Selecciona los productos que formarán parte del combo y especifica la cantidad de cada uno."
      searchPlaceholder="Buscar productos..."
      icon={<Package className="h-5 w-5" />}
      emptyMessage="No hay productos disponibles"
      noResultsMessage="No se encontraron productos"
    />
  );
}
