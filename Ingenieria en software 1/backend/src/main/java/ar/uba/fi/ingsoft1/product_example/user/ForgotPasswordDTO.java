package ar.uba.fi.ingsoft1.product_example.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO para solicitar recuperación de contraseña")
public record ForgotPasswordDTO(
                @Schema(description = "Email del usuario", example = "jperez@fi.uba.ar", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank @Email String email) {
}
