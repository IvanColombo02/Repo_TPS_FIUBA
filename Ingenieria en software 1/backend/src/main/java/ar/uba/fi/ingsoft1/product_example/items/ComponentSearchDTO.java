package ar.uba.fi.ingsoft1.product_example.items;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Optional;

@Schema(description = "DTO para filtrar productos/combos. Todos los campos son opcionales.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
public record ComponentSearchDTO(
        @Schema(description = "Nombre a buscar", example = "Hamburguesa") Optional<String> name,
        @Schema(description = "Ordenar por stock ascendente", example = "true") Optional<Boolean> stockAsc,
        @Schema(description = "Precio mínimo", example = "1000") Optional<Integer> priceMin,
        @Schema(description = "Precio máximo", example = "5000") Optional<Integer> priceMax,
        @Schema(description = "Ordenar por precio ascendente", example = "true") Optional<Boolean> priceAsc,
        @Schema(description = "Categorías a filtrar", example = "MAIN_COURSE") Optional<List<String>> categories,
        @Schema(description = "Tipos a filtrar", example = "FOOD") Optional<List<String>> type
) {
    public static ComponentSearchDTO empty(){
        return new ComponentSearchDTO(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
    }
}