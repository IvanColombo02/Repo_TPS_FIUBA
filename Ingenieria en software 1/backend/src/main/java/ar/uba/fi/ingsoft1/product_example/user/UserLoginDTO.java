package ar.uba.fi.ingsoft1.product_example.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO para iniciar sesión")
public record UserLoginDTO(
        @Schema(description = "Nombre de usuario", example = "jperez", requiredMode = Schema.RequiredMode.REQUIRED) @Size(min = 3, max = 50) String username,
        @Schema(description = "Contraseña", example = "Pass1234!", requiredMode = Schema.RequiredMode.REQUIRED) @Size(min = 6, max = 50) String password)
        implements UserCredentials {
}
