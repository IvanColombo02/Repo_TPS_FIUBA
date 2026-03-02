import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogDescription,
    DialogFooter,
} from "@/components/ui/dialog";
import { Search, Plus, Minus, Check } from "lucide-react";

export interface SelectableItem {
    id: number;
    name: string;
    base64Image?: string;
    description?: string;
    price?: number;
    stock?: number;
    categories?: string[];
    type?: string;
    estimatedTime?: number;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    [key: string]: any;
}

interface ItemSelectionDialogProps<T extends SelectableItem> {
    isOpen: boolean;
    onClose: () => void;
    onSave: (items: Record<string, number>) => void;
    initialItems?: Record<string, number>;
    items: T[];
    loading?: boolean;
    title: string;
    description: string;
    searchPlaceholder?: string;
    icon?: React.ReactNode;
    emptyMessage?: string;
    noResultsMessage?: string;
}

export function ItemSelectionDialog<T extends SelectableItem>({
    isOpen,
    onClose,
    onSave,
    initialItems = {},
    items,
    loading = false,
    title,
    description,
    searchPlaceholder = "Buscar...",
    icon,
    emptyMessage = "No hay items disponibles",
    noResultsMessage = "No se encontraron items",
}: ItemSelectionDialogProps<T>) {
    const [selectedItems, setSelectedItems] = useState<Record<string, number>>(initialItems);
    const [lastOpenState, setLastOpenState] = useState(false);
    const [searchTerm, setSearchTerm] = useState("");

    useEffect(() => {
        if (isOpen && !lastOpenState) {
            const validItems = initialItems && typeof initialItems === 'object' ? initialItems : {};
            setSelectedItems(validItems);
        }
        setLastOpenState(isOpen);
    }, [isOpen, initialItems, lastOpenState]);

    const filteredItems = items.filter(item =>
        item.name.toLowerCase().includes(searchTerm.toLowerCase())
    );

    const handleQuantityChange = (itemId: string, quantity: number) => {
        const newItems = { ...selectedItems };
        if (quantity > 0) {
            newItems[itemId] = quantity;
        } else {
            delete newItems[itemId];
        }
        setSelectedItems(newItems);
    };

    const handleSave = () => {
        onSave(selectedItems);
        onClose();
    };

    const handleClose = () => {
        onClose();
    };

    const hasSelectedItems = Object.keys(selectedItems).length > 0;

    return (

        <Dialog open={isOpen} onOpenChange={handleClose}>
            <DialogContent className="max-w-2xl max-h-[80vh] overflow-hidden flex flex-col">
                <DialogHeader>
                    <DialogTitle className="flex items-center gap-2">
                        {icon}
                        {title}
                    </DialogTitle>
                    <DialogDescription>{description}</DialogDescription>
                </DialogHeader>

                <div className="flex-1 overflow-hidden flex flex-col space-y-4">

                    <div className="relative">
                        <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
                        <Input
                            placeholder={searchPlaceholder}
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="pl-10"
                        />
                    </div>

                    <div className="flex-1 overflow-y-auto space-y-3">
                        {loading ? (
                            <div className="flex items-center justify-center py-8">
                                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
                            </div>
                        ) : items.length === 0 ? (
                            <div className="text-center py-8">
                                <p className="text-muted-foreground">{emptyMessage}</p>
                            </div>
                        ) : filteredItems.length === 0 ? (
                            <div className="text-center py-8">
                                <p className="text-muted-foreground">{noResultsMessage}</p>
                            </div>
                        ) : (
                            filteredItems.map((item) => {
                                const quantity = selectedItems[item.id.toString()] || 0;
                                const isSelected = quantity > 0;

                                return (
                                    <div
                                        key={item.id}
                                        className={`flex items-center gap-3 p-3 rounded-lg border transition-colors ${isSelected
                                            ? 'border-primary bg-primary/5'
                                            : 'border-border hover:border-primary/50'
                                            }`}
                                    >

                                        <div className="flex items-center gap-3 flex-1 min-w-0">
                                            {item.base64Image ? (
                                                <img src={item.base64Image} alt={item.name} loading="lazy" decoding="async" className="w-12 h-12 object-cover rounded-md" />
                                            ) : (
                                                <div className="w-12 h-12 bg-gray-700 rounded-md flex items-center justify-center text-sm text-white">IMG</div>
                                            )}

                                            <div className="flex-1 min-w-0">
                                                <div className="flex items-center gap-2 min-w-0">
                                                    <span className="font-medium break-words whitespace-normal leading-snug" style={{ wordBreak: 'break-word' }}>{item.name}</span>
                                                    {isSelected && (
                                                        <Check className="h-4 w-4 text-primary" />
                                                    )}
                                                </div>

                                                <div className="text-sm text-muted-foreground mt-1 flex flex-col gap-1 min-w-0">
                                                    <div className="flex items-center gap-3 min-w-0">
                                                        {item.price !== undefined && (
                                                            <span className="text-black/90">${item.price}</span>
                                                        )}
                                                        {item.description && (
                                                            <span className="break-words whitespace-normal leading-snug" style={{ wordBreak: 'break-word' }}>{item.description}</span>
                                                        )}
                                                    </div>
                                                </div>

                                                {item.stock !== undefined && (
                                                    <div className="text-sm text-muted-foreground mt-1">
                                                        Stock disponible: {item.stock}
                                                    </div>
                                                )}
                                            </div>
                                        </div>

                                        <div className="flex items-center gap-2 flex-shrink-0">
                                            <Button
                                                type="button"
                                                variant="outline"
                                                size="icon"
                                                className="h-8 w-8"
                                                onClick={() => handleQuantityChange(item.id.toString(), Math.max(0, quantity - 1))}
                                                disabled={quantity === 0}
                                            >
                                                <Minus className="h-4 w-4" />
                                            </Button>

                                            <Input
                                                type="number"
                                                min="0"
                                                value={quantity}
                                                onChange={(e) => {
                                                    const val = e.target.value === '' ? 0 : parseInt(e.target.value);
                                                    handleQuantityChange(item.id.toString(), isNaN(val) ? 0 : val);
                                                }}
                                                className="w-16 text-center"
                                            />

                                            <Button
                                                type="button"
                                                variant="outline"
                                                size="icon"
                                                className="h-8 w-8"
                                                onClick={() => handleQuantityChange(item.id.toString(), quantity + 1)}
                                            >
                                                <Plus className="h-4 w-4" />
                                            </Button>
                                        </div>
                                    </div>
                                );
                            })
                        )}
                    </div>

                    {Object.keys(selectedItems).length > 0 && (
                        <div className="mt-4 border-t pt-3">
                            <div className="font-semibold mb-2 text-sm text-muted-foreground">Seleccionados:</div>
                            <div className="flex flex-wrap gap-2">
                                {items.filter(item => selectedItems[item.id.toString()] > 0).map(item => (
                                    <div key={item.id} className="flex items-center gap-2 px-2 py-1 rounded bg-muted text-xs">
                                        {item.base64Image ? (
                                            <img src={item.base64Image} alt={item.name} loading="lazy" decoding="async" className="w-6 h-6 object-cover rounded" />
                                        ) : (
                                            <div className="w-6 h-6 bg-gray-700 rounded flex items-center justify-center text-[10px] text-white">IMG</div>
                                        )}
                                        <span className="font-medium max-w-[120px] truncate">{item.name}</span>
                                        <span className="text-primary font-bold">x{selectedItems[item.id.toString()]}</span>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}
                </div>
                <DialogFooter>
                    <Button variant="outline" onClick={handleClose}>
                        Cancelar
                    </Button>
                    <Button onClick={handleSave} disabled={!hasSelectedItems}>
                        Guardar {hasSelectedItems && `(${Object.keys(selectedItems).length})`}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
