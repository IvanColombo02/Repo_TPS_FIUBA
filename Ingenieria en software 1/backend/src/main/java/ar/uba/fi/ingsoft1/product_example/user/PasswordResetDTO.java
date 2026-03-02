package ar.uba.fi.ingsoft1.product_example.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO para restablecer la contraseña")
public record PasswordResetDTO(
                @Schema(description = "Token de recuperación de contraseña", example = "abc123...", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank String token,
                @Schema(description = "Nueva contraseña (mínimo 8 caracteres)", example = "NewPass1234!", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank String newPassword) {
}
