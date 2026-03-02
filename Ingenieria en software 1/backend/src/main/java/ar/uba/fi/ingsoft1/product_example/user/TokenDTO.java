package ar.uba.fi.ingsoft1.product_example.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO con los tokens de autenticación")
public record TokenDTO(
        @Schema(description = "Token de acceso JWT", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull String accessToken,
        @Schema(description = "Token de refresco", example = "abc123...") String refreshToken) {
}
