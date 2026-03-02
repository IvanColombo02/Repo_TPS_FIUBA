package ar.uba.fi.ingsoft1.product_example.items.ingredients;

import ar.uba.fi.ingsoft1.product_example.items.ComponentSearchDTO;
import static ar.uba.fi.ingsoft1.product_example.items.ingredients.IngredientConstants.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(INGREDIENTS_ENDPOINT)
@Validated
@RequiredArgsConstructor
@Tag(name = "5 - Ingredients", description = "Endpoints para gestión de ingredientes")
class IngredientRestController {
    private final IngredientService ingredientService;

    @GetMapping
    @Operation(summary = "Listar ingredientes", description = "Lista ingredientes con paginación y filtros. Solo ADMIN. Requiere autenticación.", parameters = {
            @Parameter(name = "filter", description = "Filtros opcionales para ingredientes", in = ParameterIn.QUERY, required = false, schema = @Schema(implementation = ComponentSearchDTO.class))
    })
    @ApiResponse(responseCode = RESPONSE_OK, description = "Lista de ingredientes obtenida exitosamente")
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = ERROR_NOT_AUTHENTICATED)
    @ApiResponse(responseCode = RESPONSE_FORBIDDEN, description = ERROR_UNAUTHORIZED_ADMIN)
    public Page<IngredientDTO> getIngredients(
            @Parameter(hidden = true) ComponentSearchDTO filter,
            Pageable pageable) {
        return ingredientService.getIngredients(filter, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener ingrediente por ID", description = "Obtiene un ingrediente específico por su ID. Solo ADMIN. Requiere autenticación.")
    @ApiResponse(responseCode = RESPONSE_OK, description = "Ingrediente obtenido exitosamente")
    @ApiResponse(responseCode = RESPONSE_NOT_FOUND, description = "Ingrediente no encontrado")
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = ERROR_NOT_AUTHENTICATED)
    @ApiResponse(responseCode = RESPONSE_FORBIDDEN, description = ERROR_UNAUTHORIZED_ADMIN)
    public ResponseEntity<IngredientDTO> getIngredientById(@PathVariable long id) {
        return ingredientService.getIngredientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    @Operation(summary = "Crear ingrediente", description = "Crea un nuevo ingrediente. Solo ADMIN. Requiere autenticación.")
    @ApiResponse(responseCode = RESPONSE_CREATED, description = "Ingrediente creado exitosamente")
    @ApiResponse(responseCode = RESPONSE_BAD_REQUEST, description = ERROR_INVALID_DATA)
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = ERROR_NOT_AUTHENTICATED)
    @ApiResponse(responseCode = RESPONSE_FORBIDDEN, description = ERROR_UNAUTHORIZED_ADMIN)
    @ResponseStatus(HttpStatus.CREATED)
    public IngredientDTO createIngredient(
            @Validated @RequestBody IngredientCreateDTO data) {
        var ingredient = ingredientService.createIngredient(data);
        if (ingredient == null) throw new ResponseStatusException(HttpStatus.CONFLICT,
                ERROR_DUPLICATE_NAME);
        return ingredient;
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar ingrediente", description = "Actualiza un ingrediente existente. Solo ADMIN. Requiere autenticación.")
    @ApiResponse(responseCode = RESPONSE_OK, description = "Ingrediente actualizado exitosamente")
    @ApiResponse(responseCode = RESPONSE_NOT_MODIFIED, description = "Ingrediente no modificado")
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = ERROR_NOT_AUTHENTICATED)
    @ApiResponse(responseCode = RESPONSE_FORBIDDEN, description = ERROR_UNAUTHORIZED_ADMIN)
    public ResponseEntity<IngredientDTO> updateIngredient(
            @PathVariable Long id,
            @Validated @RequestBody IngredientUpdateDTO data) {
        return ingredientService.updateIngredient(id, data)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_MODIFIED).build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar ingrediente", description = "Elimina un ingrediente. Solo ADMIN. Requiere autenticación.")
    @ApiResponse(responseCode = RESPONSE_OK, description = "Ingrediente eliminado exitosamente")
    @ApiResponse(responseCode = RESPONSE_NOT_FOUND, description = "Ingrediente no encontrado")
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = ERROR_NOT_AUTHENTICATED)
    @ApiResponse(responseCode = RESPONSE_FORBIDDEN, description = ERROR_UNAUTHORIZED_ADMIN)
    public ResponseEntity<Void> deleteIngredientById(@PathVariable long id) {
        boolean deleted = ingredientService.deleteIngredientById(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}" + STOCK_ENDPOINT)
    @Operation(summary = "Actualizar stock de ingrediente", description = "Actualiza el stock de un ingrediente. Solo ADMIN. Requiere autenticación.")
    @ApiResponse(responseCode = RESPONSE_OK, description = "Stock actualizado exitosamente")
    @ApiResponse(responseCode = RESPONSE_NOT_MODIFIED, description = "Stock no modificado")
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = ERROR_NOT_AUTHENTICATED)
    @ApiResponse(responseCode = RESPONSE_FORBIDDEN, description = ERROR_UNAUTHORIZED_ADMIN)
    public ResponseEntity<IngredientDTO> updateStockIngredient(
            @PathVariable Long id,
            @NonNull @RequestBody IngredientStockDTO data) {
        return ingredientService.updateStock(id, data)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_MODIFIED).build());
    }
}
