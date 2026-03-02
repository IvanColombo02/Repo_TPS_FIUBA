package ar.uba.fi.ingsoft1.product_example.user;

import static ar.uba.fi.ingsoft1.product_example.user.UserConstants.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(USERS_ENDPOINT)
@Tag(name = "1 - Users", description = "Endpoints para gestión de usuarios")
class UserRestController {
    private final UserService userService;
    private final VerificationService verificationService;
    private final PasswordResetService passwordResetService;

    @Autowired
    UserRestController(UserService userService, VerificationService verificationService,
            PasswordResetService passwordResetService) {
        this.userService = userService;
        this.verificationService = verificationService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping(produces = CONTENT_TYPE_JSON)
    @Operation(summary = "Registrar nuevo usuario", description = "Crea un nuevo usuario y devuelve los tokens de autenticación. Endpoint público. El email debe ser @fi.uba.ar.")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = RESPONSE_CREATED, description = "Usuario creado exitosamente")
    @ApiResponse(responseCode = RESPONSE_BAD_REQUEST, description = "Datos inválidos (validaciones no cumplidas)")
    @ApiResponse(responseCode = RESPONSE_CONFLICT, description = "Usuario ya existe (username o email duplicado)", content = @Content)
    ResponseEntity<TokenDTO> signUp(
            @Valid @Validated @NonNull @RequestBody UserCreateDTO data) throws MethodArgumentNotValidException {
        return userService.createUser(data)
                .map(tk -> ResponseEntity.status(HttpStatus.CREATED).body(tk))
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping(VERIFY_ENDPOINT)
    @Operation(summary = "Verificar email", description = "Verifica el email del usuario usando el token recibido por correo. Endpoint público.")
    @ApiResponse(responseCode = RESPONSE_FOUND, description = "Redirección según resultado de verificación")
    public ResponseEntity<Void> verify(@RequestParam @NotBlank String token) {
        boolean ok = verificationService.verify(token);
        if (ok) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", LOGIN_URL + "?" + QUERY_PARAM_MESSAGE + "=" + MESSAGE_EMAIL_VERIFIED).build();
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", VERIFY_URL + "?" + QUERY_PARAM_TOKEN + "=" + token + "&" + QUERY_PARAM_ERROR + "=" + MESSAGE_INVALID).build();
    }

    @PostMapping(FORGOT_PASSWORD_ENDPOINT)
    @Operation(summary = "Solicitar recuperación de contraseña", description = "Solicita un email de recuperación de contraseña. Endpoint público.")
    @ApiResponse(responseCode = RESPONSE_OK, description = "Solicitud procesada (siempre devuelve 200 por seguridad)")
    @ApiResponse(responseCode = RESPONSE_NOT_FOUND, description = "Email no encontrado")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordDTO dto) {
        boolean found = passwordResetService.requestResetForEmail(dto.email());
        if (found) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PostMapping(RESET_PASSWORD_ENDPOINT)
    @Operation(summary = "Restablecer contraseña", description = "Restablece la contraseña usando el token recibido por correo. Endpoint público.")
    @ApiResponse(responseCode = RESPONSE_OK, description = "Contraseña restablecida exitosamente")
    @ApiResponse(responseCode = RESPONSE_BAD_REQUEST, description = "Token inválido o contraseña no cumple requisitos")
    @ApiResponse(responseCode = RESPONSE_CONFLICT, description = "La nueva contraseña es igual a la anterior")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid @Validated PasswordResetDTO dto) {
        var result = passwordResetService.resetPassword(dto.token(), dto.newPassword());
        return switch (result) {
            case SUCCESS -> ResponseEntity.ok(PASSWORD_RESET_SUCCESS);
            case INVALID_LENGTH -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(PASSWORD_INVALID);
            case SAME_AS_OLD -> ResponseEntity.status(HttpStatus.CONFLICT).body(PASSWORD_SAME_AS_OLD);
            case INVALID_TOKEN -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(TOKEN_INVALID);
        };
    }

    @PatchMapping(ME_ENDPOINT)
    @Operation(summary = "Actualizar mi perfil", description = "Actualiza los datos del usuario autenticado. Requiere autenticación.")
    @ApiResponse(responseCode = RESPONSE_OK, description = "Perfil actualizado exitosamente")
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = "No autenticado")
    public Optional<UserDTO> updateMe(
            Principal principal,
            @RequestBody @Valid @Validated UserUpdateDTO dto) {
        return userService.updateMe(principal, dto);
    }

    @GetMapping(ME_ENDPOINT)
    @Operation(summary = "Obtener mi perfil", description = "Obtiene los datos del usuario autenticado. Requiere autenticación.")
    @ApiResponse(responseCode = RESPONSE_OK, description = "Perfil obtenido exitosamente")
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = "No autenticado")
    public Optional<UserDTO> getMe(
            Principal principal) {
        return userService.getByUsername(principal);
    }

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Lista todos los usuarios. Solo ADMIN. Requiere autenticación.")
    @ApiResponse(responseCode = RESPONSE_OK, description = "Lista de usuarios obtenida exitosamente")
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = "No autenticado")
    @ApiResponse(responseCode = RESPONSE_FORBIDDEN, description = "No autorizado (requiere rol ADMIN)")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @PatchMapping("/{id}" + ROLE_ENDPOINT)
    @Operation(summary = "Actualizar rol de usuario", description = "Actualiza el rol de un usuario. Solo ADMIN. Requiere autenticación.")
    @ApiResponse(responseCode = RESPONSE_OK, description = "Rol actualizado exitosamente")
    @ApiResponse(responseCode = RESPONSE_BAD_REQUEST, description = "Rol inválido o usuario no encontrado")
    @ApiResponse(responseCode = RESPONSE_UNAUTHORIZED, description = "No autenticado")
    @ApiResponse(responseCode = RESPONSE_FORBIDDEN, description = "No autorizado (requiere rol ADMIN)")
    public Optional<UserDTO> updateUserRole(
            @PathVariable Long id,
            @RequestBody @Valid UpdateRoleDTO dto) {
        return userService.updateUserRole(id, dto.role());
    }

}
