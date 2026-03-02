package ar.uba.fi.ingsoft1.product_example.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO para actualizar el rol de un usuario")
public record UpdateRoleDTO(
        @Schema(description = "Nuevo rol del usuario", example = "ROLE_ADMIN", allowableValues = {
                "ROLE_USER", "ROLE_ADMIN",
                "ROLE_EMPLOYEE" }, requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank String role){
}
