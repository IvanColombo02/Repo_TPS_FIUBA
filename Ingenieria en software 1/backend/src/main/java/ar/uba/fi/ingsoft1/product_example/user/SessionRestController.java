package ar.uba.fi.ingsoft1.product_example.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/sessions")
@Tag(name = "2 - Sessions", description = "Endpoints para gestión de sesiones (login y refresh)")
class SessionRestController {

    private final UserService userService;

    @Autowired
    SessionRestController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(produces = "application/json")
    @Operation(summary = "Iniciar sesión", description = "Inicia sesión y devuelve los tokens de autenticación. Endpoint público.")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "201", description = "Sesión iniciada exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "401", description = "Credenciales inválidas o email no verificado", content = @Content)
    public TokenDTO login(
            @Valid @NonNull @RequestBody UserLoginDTO data) throws MethodArgumentNotValidException {
        return userService.loginUserOrThrow(data);
    }

    @PutMapping(produces = "application/json")
    @Operation(summary = "Refrescar token", description = "Genera nuevos tokens usando un refresh token válido. Endpoint público.")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "200", description = "Tokens refrescados exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "401", description = "Refresh token inválido o expirado", content = @Content)
    public TokenDTO refresh(
            @Valid @NonNull @RequestBody RefreshDTO data) throws MethodArgumentNotValidException {
        return userService
                .refresh(data)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
