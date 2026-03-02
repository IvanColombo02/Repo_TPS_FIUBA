package ar.uba.fi.ingsoft1.product_example.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO para refrescar el token de acceso")
public record RefreshDTO(
                @Schema(description = "Token de refresco", example = "abc123...", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank String refreshToken) {
}
