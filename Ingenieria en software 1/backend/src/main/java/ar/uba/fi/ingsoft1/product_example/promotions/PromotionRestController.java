package ar.uba.fi.ingsoft1.product_example.promotions;

import static ar.uba.fi.ingsoft1.product_example.promotions.PromotionConstants.*;
import io.swagger.v3.oas.annotations.Operation;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(PROMOTIONS_ENDPOINT)
@Validated
@RequiredArgsConstructor
@Tag(name = "7 - Promotions", description = "Endpoints para gestión de promociones")
class PromotionRestController {
    private final PromotionService promotionService;

    @GetMapping
    @Operation(summary = "Listar promociones", description = "Lista promociones con paginación ordenadas por prioridad (menor número = mayor prioridad).")
    @ApiResponse(responseCode = RESPONSE_OK, description = "Lista de promociones obtenida exitosamente")
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = ERROR_NOT_AUTHENTICATED)
    @ApiResponse(responseCode = RESPONSE_FORBIDDEN, description = ERROR_UNAUTHORIZED_ADMIN)
    public Page<PromotionDTO> getPromotions(Pageable pageable) {
        return promotionService.getPromotions(pageable);
    }

    @GetMapping(ACTIVE_ENDPOINT)
    @Operation(summary = "Listar promociones activas", description = "Lista todas las promociones activas ordenadas por prioridad.")
    @ApiResponse(responseCode = RESPONSE_OK, description = "Lista de promociones activas obtenida exitosamente")
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = ERROR_NOT_AUTHENTICATED)
    @ApiResponse(responseCode = RESPONSE_FORBIDDEN, description = ERROR_UNAUTHORIZED_ADMIN)
    public List<PromotionDTO> getActivePromotions() {
        return promotionService.getActivePromotions(LocalDate.now());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener promoción por ID", description = "Obtiene una promoción específica por su ID.")
    @ApiResponse(responseCode = RESPONSE_OK, description = "Promoción obtenida exitosamente")
    @ApiResponse(responseCode = RESPONSE_NOT_FOUND, description = "Promoción no encontrada")
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = ERROR_NOT_AUTHENTICATED)
    @ApiResponse(responseCode = RESPONSE_FORBIDDEN, description = ERROR_UNAUTHORIZED_ADMIN)
    public ResponseEntity<PromotionDTO> getPromotionById(@PathVariable long id) {
        return promotionService.getPromotionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    @Operation(summary = "Crear promoción", description = "Crea una nueva promoción. Solo ADMIN. Requiere autenticación.")
    @ApiResponse(responseCode = RESPONSE_CREATED, description = "Promoción creada exitosamente")
    @ApiResponse(responseCode = RESPONSE_BAD_REQUEST, description = ERROR_INVALID_DATA)
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = ERROR_NOT_AUTHENTICATED)
    @ApiResponse(responseCode = RESPONSE_FORBIDDEN, description = ERROR_UNAUTHORIZED_ADMIN)
    @ResponseStatus(HttpStatus.CREATED)
    public PromotionDTO createPromotion(
            @Validated @RequestBody PromotionCreateDTO data) {
        return promotionService.createPromotion(data);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar promoción", description = "Actualiza una promoción existente. Solo ADMIN. Requiere autenticación.")
    @ApiResponse(responseCode = RESPONSE_OK, description = "Promoción actualizada exitosamente")
    @ApiResponse(responseCode = RESPONSE_BAD_REQUEST, description = ERROR_INVALID_DATA + " (formato de fecha incorrecto, fromDate después de toDate, etc.)")
    @ApiResponse(responseCode = RESPONSE_NOT_MODIFIED, description = "Promoción no modificada")
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = ERROR_NOT_AUTHENTICATED)
    @ApiResponse(responseCode = RESPONSE_FORBIDDEN, description = ERROR_UNAUTHORIZED_ADMIN)
    @ApiResponse(responseCode = RESPONSE_NOT_FOUND, description = "Promoción no encontrada")
    public ResponseEntity<PromotionDTO> updatePromotion(
            @PathVariable Long id,
            @Validated @RequestBody PromotionUpdateDTO data) {
        return promotionService.updatePromotion(id, data)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_MODIFIED).build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar promoción", description = "Elimina una promoción. Solo ADMIN. Requiere autenticación.")
    @ApiResponse(responseCode = RESPONSE_OK, description = "Promoción eliminada exitosamente")
    @ApiResponse(responseCode = RESPONSE_NOT_FOUND, description = "Promoción no encontrada")
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = ERROR_NOT_AUTHENTICATED)
    @ApiResponse(responseCode = RESPONSE_FORBIDDEN, description = ERROR_UNAUTHORIZED_ADMIN)
    public ResponseEntity<Void> deletePromotionById(@PathVariable long id) {
        boolean deleted = promotionService.deletePromotionById(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PutMapping(PRIORITIES_ENDPOINT)
    @Operation(summary = "Actualizar prioridades", description = "Reordena todas las promociones según el orden enviado. Solo ADMIN. Requiere autenticación.")
    @ApiResponse(responseCode = RESPONSE_OK, description = "Prioridades actualizadas exitosamente")
    @ApiResponse(responseCode = RESPONSE_BAD_REQUEST, description = ERROR_INVALID_DATA + " (IDs repetidos o inexistentes)")
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = ERROR_NOT_AUTHENTICATED)
    @ApiResponse(responseCode = RESPONSE_FORBIDDEN, description = ERROR_UNAUTHORIZED_ADMIN)
    public List<PromotionDTO> updatePriorities(@Validated @RequestBody PromotionPriorityUpdateDTO data) {
        return promotionService.updatePriorities(data);
    }
}
