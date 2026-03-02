import { ChevronsLeft, ChevronLeft, ChevronRight, ChevronsRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

export interface PaginationToolbarProps {
    page: number;
    size: number;
    totalPages: number;
    totalElements: number;
    onPageChange: (page: number) => void;
    disabled?: boolean;
    className?: string;
}

export function PaginationToolbar({
    page,
    size,
    totalPages,
    totalElements,
    onPageChange,
    disabled = false,
    className,
}: PaginationToolbarProps) {
    if (totalElements === 0) {
        return null;
    }

    const start = page * size + 1;
    const end = Math.min(totalElements, start + size - 1);
    const hasPrevious = page > 0;
    const hasNext = page < totalPages - 1;

    const handlePageChange = (nextPage: number) => {
        if (nextPage === page) {
            return;
        }
        onPageChange(nextPage);
    };

    return (
        <div className={cn(
            "flex flex-col gap-3 rounded-lg border border-border bg-card p-3 sm:flex-row sm:items-center sm:justify-between",
            className
        )}>
            <span className="text-sm text-muted-foreground">
                Mostrando {start.toLocaleString()} - {end.toLocaleString()} de {totalElements.toLocaleString()} resultados
            </span>
            {totalPages > 1 && (
                <div className="flex items-center gap-2">
                    <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        onClick={() => handlePageChange(0)}
                        disabled={disabled || !hasPrevious}
                        className="text-white border-white/30 hover:bg-white/10"
                    >
                        <ChevronsLeft className="h-4 w-4" />
                    </Button>
                    <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        onClick={() => handlePageChange(page - 1)}
                        disabled={disabled || !hasPrevious}
                        className="text-white border-white/30 hover:bg-white/10"
                    >
                        <ChevronLeft className="h-4 w-4" />
                    </Button>
                    <span className="text-sm text-muted-foreground">
                        Página {page + 1} de {totalPages}
                    </span>
                    <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        onClick={() => handlePageChange(page + 1)}
                        disabled={disabled || !hasNext}
                        className="text-white border-white/30 hover:bg-white/10"
                    >
                        <ChevronRight className="h-4 w-4" />
                    </Button>
                    <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        onClick={() => handlePageChange(totalPages - 1)}
                        disabled={disabled || !hasNext}
                        className="text-white border-white/30 hover:bg-white/10"
                    >
                        <ChevronsRight className="h-4 w-4" />
                    </Button>
                </div>
            )}
        </div>
    );
}
