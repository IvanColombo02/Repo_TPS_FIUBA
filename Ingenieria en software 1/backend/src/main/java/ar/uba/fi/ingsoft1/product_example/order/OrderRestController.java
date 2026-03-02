package ar.uba.fi.ingsoft1.product_example.order;

import static ar.uba.fi.ingsoft1.product_example.order.OrderConstants.*;
import ar.uba.fi.ingsoft1.product_example.config.security.JwtUserDetails;
import ar.uba.fi.ingsoft1.product_example.user.User;
import ar.uba.fi.ingsoft1.product_example.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for order management endpoints.
 */
@RestController
@RequestMapping(ORDERS_ENDPOINT)
@Tag(name = "3 - Orders", description = "Endpoints para gestión de órdenes")
public class OrderRestController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    /**
     * Creates a new order for the authenticated user.
     * POST /orders
     */
    @PostMapping
    @Operation(summary = "Crear una nueva orden", description = "Crea una orden con los items especificados. Requiere autenticación.")
    @ApiResponse(responseCode = RESPONSE_CREATED, description = "Orden creada exitosamente")
    @ApiResponse(responseCode = RESPONSE_BAD_REQUEST, description = "Error: orden no encontrada, no autorizada, estado inválido o stock insuficiente")
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = "No autenticado")
    @PreAuthorize("hasAnyRole('" + USER_ROLE + "','" + ADMIN_ROLE + "','" + EMPLOYEE_ROLE + "')")
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderCreateDTO dto, Authentication authentication) {
        Long userId = resolveUserId(authentication);
        OrderDTO order = orderService.createOrder(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @PostMapping(CALCULATE_DISCOUNTS_ENDPOINT)
    @Operation(summary = "Calcular descuentos del carrito", description = "Calcula los descuentos aplicables para los items del carrito sin crear una orden. Requiere autenticación.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Mapa de items del carrito: ID del componente (producto o combo) a cantidad. Ejemplo: {\"852\": 3, \"853\": 1}", required = true, content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(type = "object", example = "{\"852\": 3, \"853\": 1}", additionalProperties = Schema.AdditionalPropertiesValue.TRUE)))
    @ApiResponse(responseCode = RESPONSE_OK, description = "Descuentos calculados exitosamente")
    @ApiResponse(responseCode = RESPONSE_BAD_REQUEST, description = "Error: componentes no encontrados")
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = "No autenticado")
    @PreAuthorize("hasAnyRole('" + USER_ROLE + "','" + ADMIN_ROLE + "','" + EMPLOYEE_ROLE + "')")
    public ResponseEntity<CartDiscountCalculationDTO> calculateCartDiscounts(
            @org.springframework.web.bind.annotation.RequestBody Map<Long, Integer> items,
            Authentication authentication) {
        Long userId = resolveUserId(authentication);
        CartDiscountCalculationDTO result = orderService.calculateCartDiscounts(items, userId);
        return ResponseEntity.ok(result);
    }

    /**
     * Gets all orders for the authenticated user.
     * GET /orders
     */
    @GetMapping
    @Operation(summary = "Obtener mis órdenes", description = "Lista todas las órdenes del usuario autenticado. Requiere autenticación.")
    @ApiResponse(responseCode = RESPONSE_OK, description = "Lista de órdenes obtenida exitosamente")
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = "No autenticado")
    @PreAuthorize("hasAnyRole('" + USER_ROLE + "','" + ADMIN_ROLE + "','" + EMPLOYEE_ROLE + "')")
    public ResponseEntity<List<OrderDTO>> getMyOrders(Authentication authentication) {
        Long userId = resolveUserId(authentication);
        List<OrderDTO> orders = orderService.getMyOrders(userId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Gets a specific order by ID.
     * GET /orders/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener orden por ID", description = "Obtiene una orden específica por su ID.")
    @ApiResponse(responseCode = RESPONSE_OK, description = "Orden obtenida exitosamente")
    @ApiResponse(responseCode = RESPONSE_BAD_REQUEST, description = "Orden no encontrada o no autorizada")
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = "No autenticado")
    @PreAuthorize("hasAnyRole('" + USER_ROLE + "','" + ADMIN_ROLE + "','" + EMPLOYEE_ROLE + "')")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id, Authentication authentication) {
        Long userId = resolveUserId(authentication);
        OrderDTO order = orderService.getOrderById(id, userId);
        return ResponseEntity.ok(order);
    }

    /**
     * Cancels an order (only PENDING orders).
     * DELETE /orders/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar orden", description = "Cancela una orden. Solo se pueden cancelar órdenes con estado PENDING. Requiere autenticación.")
    @ApiResponse(responseCode = RESPONSE_OK, description = "Orden cancelada exitosamente")
    @ApiResponse(responseCode = RESPONSE_BAD_REQUEST, description = "Orden no encontrada, no autorizada o no se puede cancelar (estado inválido)")
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = "No autenticado")
    @PreAuthorize("hasAnyRole('" + USER_ROLE + "','" + ADMIN_ROLE + "','" + EMPLOYEE_ROLE + "')")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Long id, Authentication authentication) {
        Long userId = resolveUserId(authentication);
        OrderDTO order = orderService.cancelOrder(id, userId);
        return ResponseEntity.ok(order);
    }

    private Long resolveUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return user.getId();
        }
        if (principal instanceof JwtUserDetails jwtUserDetails) {
            return userService.findByUsername(jwtUserDetails.username())
                    .map(User::getId)
                    .orElseThrow(
                            () -> new RuntimeException(ERROR_USER_NOT_FOUND + jwtUserDetails.username()));
        }
        throw new RuntimeException(ERROR_UNSUPPORTED_PRINCIPAL + principal.getClass().getName());
    }

    /**
     * Updates order status (employee only).
     * PATCH /orders/{id}/status
     */
    @PatchMapping("/{id}" + STATUS_ENDPOINT)
    @Operation(summary = "Actualizar estado de orden", description = "Actualiza el estado de una orden. Solo ADMIN o EMPLOYEE. Requiere autenticación.")
    @ApiResponse(responseCode = RESPONSE_OK, description = "Estado de orden actualizado exitosamente")
    @ApiResponse(responseCode = RESPONSE_BAD_REQUEST, description = "Orden no encontrada o estado inválido")
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = "No autenticado")
    @ApiResponse(responseCode = RESPONSE_FORBIDDEN, description = "No autorizado (requiere rol ADMIN o EMPLOYEE)")
    @PreAuthorize("hasAnyRole('" + ADMIN_ROLE + "','" + EMPLOYEE_ROLE + "')")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody OrderStatusUpdateDTO dto) {
        OrderDTO order = orderService.updateOrderStatus(id, dto);
        return ResponseEntity.ok(order);
    }

    /**
     * Gets all orders by status (employee only).
     * GET /orders/status/{status}
     */
    @GetMapping(STATUS_ENDPOINT + "/{status}")
    @Operation(summary = "Listar órdenes por estado", description = "Lista todas las órdenes con un estado específico. Solo ADMIN o EMPLOYEE. Requiere autenticación.")
    @ApiResponse(responseCode = RESPONSE_OK, description = "Lista de órdenes obtenida exitosamente")
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = "No autenticado")
    @ApiResponse(responseCode = RESPONSE_FORBIDDEN, description = "No autorizado (requiere rol ADMIN o EMPLOYEE)")
    @PreAuthorize("hasAnyRole('" + ADMIN_ROLE + "','" + EMPLOYEE_ROLE + "')")
    public ResponseEntity<List<OrderDTO>> getOrdersByStatus(@PathVariable OrderStatus status) {
        List<OrderDTO> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    /**
     * Gets all active orders (PENDING + IN_PREPARATION) for kitchen view.
     * GET /orders/active
     */
    @GetMapping(ACTIVE_ENDPOINT)
    @Operation(summary = "Listar órdenes activas", description = "Lista todas las órdenes activas (PENDING, IN_PREPARATION, READY, DELIVERED, CANCELLED) para vista de cocina. Solo ADMIN o EMPLOYEE. Requiere autenticación.")
    @ApiResponse(responseCode = RESPONSE_OK, description = "Lista de órdenes activas obtenida exitosamente")
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = "No autenticado")
    @ApiResponse(responseCode = RESPONSE_FORBIDDEN, description = "No autorizado (requiere rol ADMIN o EMPLOYEE)")
    @PreAuthorize("hasAnyRole('" + ADMIN_ROLE + "','" + EMPLOYEE_ROLE + "')")
    public ResponseEntity<List<OrderDTO>> getActiveOrders() {
        List<OrderDTO> orders = orderService.getActiveOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * Exception handler for insufficient stock errors.
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                ERROR_CODE_INSUFFICIENT_STOCK);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Exception handler for generic runtime exceptions.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                ERROR_CODE_BAD_REQUEST);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Exception handler for illegal state exceptions.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                ERROR_CODE_CONFLICT);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Error response structure.
     */
    public static class ErrorResponse {
        private int status;
        private String message;
        private String errorCode;

        public ErrorResponse(int status, String message, String errorCode) {
            this.status = status;
            this.message = message;
            this.errorCode = errorCode;
        }

        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }
}
