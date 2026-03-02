package ar.uba.fi.ingsoft1.product_example.items.combos;

import ar.uba.fi.ingsoft1.product_example.items.ComponentSearchDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static ar.uba.fi.ingsoft1.product_example.items.combos.ComboConstants.*;

@RestController
@RequestMapping(COMBOS_ENDPOINT)
@Validated
@RequiredArgsConstructor
@Tag(name = "6 - Combos", description = "Endpoints para gestión de combos")
class ComboRestController {
    private final ComboService comboService;

    @GetMapping
    @Operation(summary = "Listar combos", description = "Lista combos con paginación y filtros.", parameters = {
            @Parameter(name = "filter", description = "Filtros opcionales para combos", in = ParameterIn.QUERY, required = false, schema = @Schema(implementation = ComponentSearchDTO.class))
    })
    @ApiResponse(responseCode = RESPONSE_OK, description = "Lista de combos obtenida exitosamente")
    public Page<ComboSimpleDTO> getCombos(
            @Parameter(hidden = true) ComponentSearchDTO filter,
            Pageable pageable) {
        return comboService.getCombos(filter, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener combo por ID", description = "Obtiene un combo específico por su ID.")
    @ApiResponse(responseCode = RESPONSE_OK, description = "Combo obtenido exitosamente")
    @ApiResponse(responseCode = RESPONSE_NOT_FOUND, description = "Combo no encontrado")
    public ResponseEntity<ComboDTO> getComboById(@PathVariable long id) {
        return comboService.getComboById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    @Operation(summary = "Crear combo", description = "Crea un nuevo combo. Solo ADMIN. Requiere autenticación.")
    @ApiResponse(responseCode = RESPONSE_CREATED, description = "Combo creado exitosamente")
    @ApiResponse(responseCode = RESPONSE_BAD_REQUEST, description = ERROR_INVALID_DATA)
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = ERROR_NOT_AUTHENTICATED)
    @ApiResponse(responseCode = RESPONSE_FORBIDDEN, description = ERROR_UNAUTHORIZED_ADMIN)
    @ResponseStatus(HttpStatus.CREATED)
    public ComboDTO createCombo(
            @Validated @RequestBody ComboCreateDTO data) {
        var combo = comboService.createCombo(data);
        if (combo == null) throw new ResponseStatusException(HttpStatus.CONFLICT,
                ERROR_DUPLICATE_NAME);
        return combo;
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar combo", description = "Actualiza un combo existente. Solo ADMIN. Requiere autenticación.")
    @ApiResponse(responseCode = RESPONSE_OK, description = "Combo actualizado exitosamente")
    @ApiResponse(responseCode = RESPONSE_NOT_MODIFIED, description = "Combo no modificado")
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = ERROR_NOT_AUTHENTICATED)
    @ApiResponse(responseCode = RESPONSE_FORBIDDEN, description = ERROR_UNAUTHORIZED_ADMIN)
    public ResponseEntity<ComboDTO> updateCombo(
            @PathVariable Long id,
            @Validated @RequestBody ComboUpdateDTO data) {
        return comboService.updateCombo(id, data)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_MODIFIED).build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar combo", description = "Elimina un combo. Solo ADMIN. Requiere autenticación.")
    @ApiResponse(responseCode = RESPONSE_OK, description = "Combo eliminado exitosamente")
    @ApiResponse(responseCode = RESPONSE_NOT_FOUND, description = "Combo no encontrado")
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = ERROR_NOT_AUTHENTICATED)
    @ApiResponse(responseCode = RESPONSE_FORBIDDEN, description = ERROR_UNAUTHORIZED_ADMIN)
    public ResponseEntity<Void> deleteComboById(@PathVariable long id) {
        boolean deleted = comboService.deleteComboById(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/stock")
    @io.swagger.v3.oas.annotations.Hidden
    public ResponseEntity<ComboDTO> updateStockCombo(
            @PathVariable Long id,
            @Validated @RequestBody ComboStockDTO data) {
        return comboService.updateStock(id, data)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_MODIFIED).build());
    }
}
