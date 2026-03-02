import { useState, useMemo } from "react";
import { ChevronDown, ChevronUp, Edit2, Trash2, Loader2, Search } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger } from "@/components/ui/alert-dialog";

export interface Item {
  id: number;
  name: string;
  stock?: number;
  base64Image?: string;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  [key: string]: any;
}

interface ItemsDropdownProps {
  title: string;
  items: Item[];
  loading: boolean;
  onEdit: (item: Item) => void;
  onDelete: (id: number) => void;
  isDeleting?: boolean;
  emptyMessage?: string;
  renderItem?: (item: Item) => React.ReactNode;
  searchable?: boolean;
  searchPlaceholder?: string;
  onSearch?: (query: string) => void;
  totalItems?: number;
}

export function ItemsDropdown({
  title,
  items,
  loading,
  onEdit,
  onDelete,
  isDeleting = false,
  emptyMessage = "No hay elementos creados aún.",
  renderItem,
  searchable = false,
  searchPlaceholder = "Buscar...",
  onSearch,
  totalItems,
}: ItemsDropdownProps) {
  const [isOpen, setIsOpen] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");

  const handleSearchChange = (query: string) => {
    setSearchQuery(query);

    if (onSearch) {
      onSearch(query);
    }
  };

  const filteredItems = useMemo(() => {
    if (!onSearch && searchQuery) {
      return items.filter(item =>
        item.name.toLowerCase().includes(searchQuery.toLowerCase())
      );
    }
    return items;
  }, [items, searchQuery, onSearch]);

  const handleDelete = async (id: number) => {
    await onDelete(id);
  };

  const defaultRenderItem = (item: Item) => (
    <div className="flex items-center gap-4">
      <div>
        <span className="font-medium text-white">{item.name}</span>
        {item.stock !== undefined && (
          <span className="text-sm text-white/70 ml-2">
            Stock: {item.stock}
          </span>
        )}
      </div>
    </div>
  );

  const totalCount = totalItems ?? items.length;
  const showTotal = totalItems !== undefined || (!!searchQuery && !onSearch);

  return (
    <div className="border rounded-lg bg-card">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="w-full flex items-center justify-between p-4 hover:bg-muted/50 transition-colors"
      >
        <div className="flex items-center gap-2">
          <span className="font-medium text-white">{title}</span>
          <span className="text-sm text-muted-foreground">
            ({filteredItems.length}{showTotal ? ` de ${totalCount}` : ""})
          </span>
        </div>
        {isOpen ? (
          <ChevronUp className="h-4 w-4 text-muted-foreground" />
        ) : (
          <ChevronDown className="h-4 w-4 text-muted-foreground" />
        )}
      </button>

      {isOpen && (
        <div className="border-t">

          {searchable && (
            <div className="p-4 border-b">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground h-4 w-4" />
                <Input
                  placeholder={searchPlaceholder}
                  value={searchQuery}
                  onChange={(e) => handleSearchChange(e.target.value)}
                  className="pl-10 text-white placeholder:text-white/50"
                />
              </div>
            </div>
          )}

          {loading ? (
            <div className="flex items-center justify-center py-8">
              <Loader2 className="h-6 w-6 animate-spin" />
              <span className="ml-2">Cargando...</span>
            </div>
          ) : filteredItems.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              {searchQuery ? "No se encontraron resultados" : emptyMessage}
            </div>
          ) : (
            <div className="p-4 space-y-2">
              {filteredItems.map((item) => (
                <div
                  key={`${item.id}-${item.stock ?? 0}`}
                  className="flex items-center justify-between p-3 border rounded-lg bg-background"
                >
                  <div className="text-white">
                    {renderItem ? renderItem(item) : defaultRenderItem(item)}
                  </div>

                  <div className="flex gap-2">
                    <Button
                      variant="outline"
                      className="text-white border-white/30 hover:bg-white/10"
                      size="sm"
                      onClick={() => onEdit(item)}
                    >
                      <Edit2 className="h-4 w-4 text-white" />
                    </Button>

                    <AlertDialog>
                      <AlertDialogTrigger asChild>
                        <Button
                          variant="destructive"
                          size="sm"
                          disabled={isDeleting}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </AlertDialogTrigger>
                      <AlertDialogContent>
                        <AlertDialogHeader>
                          <AlertDialogTitle>¿Eliminar elemento?</AlertDialogTitle>
                          <AlertDialogDescription>
                            Esta acción no se puede deshacer. Se eliminará permanentemente <strong>"{item.name}"</strong>.
                          </AlertDialogDescription>
                        </AlertDialogHeader>
                        <AlertDialogFooter className="flex-col sm:flex-row gap-2">
                          <AlertDialogCancel className="w-full sm:w-auto">Cancelar</AlertDialogCancel>
                          <AlertDialogAction
                            onClick={() => handleDelete(item.id)}
                            className="bg-red-600 text-white hover:bg-red-700 w-full sm:w-auto min-w-[120px]"
                            disabled={isDeleting}
                          >
                            {isDeleting ? (
                              <>
                                <Loader2 className="h-4 w-4 mr-1 animate-spin" />
                                Eliminando...
                              </>
                            ) : (
                              <>
                                <Trash2 className="h-4 w-4 mr-1" />
                                Eliminar
                              </>
                            )}
                          </AlertDialogAction>
                        </AlertDialogFooter>
                      </AlertDialogContent>
                    </AlertDialog>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
