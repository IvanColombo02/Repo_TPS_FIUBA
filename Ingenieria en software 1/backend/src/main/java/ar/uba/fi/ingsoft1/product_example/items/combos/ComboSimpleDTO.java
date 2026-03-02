package ar.uba.fi.ingsoft1.product_example.items.combos;

import ar.uba.fi.ingsoft1.product_example.items.products.Product;
import ar.uba.fi.ingsoft1.product_example.items.products.ProductSimpleDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Schema(description = "DTO con la información de un combo pero más simple")
public record ComboSimpleDTO(
        @Schema(description = "ID del combo", example = "1") long id,
        @Schema(description = "Nombre del combo", example = "Combo Clásico") String name,
        @Schema(description = "Descripción del combo", example = "Hamburguesa con papas y gaseosa") String description,
        @Schema(description = "Precio del combo", example = "3500.0") float price,
        @Schema(description = "Stock disponible", example = "50") int stock,
        @Schema(description = "Lista de categorías", example = "[\"MAIN_COURSE\", \"FAST_FOOD\"]") List<String> categories,
        @Schema(description = "Lista de tipos", example = "[\"FOOD\", \"DRINK\"]") List<String> types,
        @Schema(description = "Imagen del combo en base64", example = "data:image/png;base64,iVBORw0KG...") String base64Image
) {
    public ComboSimpleDTO(Combo combo) {
        this(
                combo.getId(),
                combo.getName(),
                combo.getDescription(),
                combo.getPrice(),
                combo.getStock(),
                combo.getCategories(),
                combo.getTypes(),
                combo.getBase64Image()
        );
    }
}