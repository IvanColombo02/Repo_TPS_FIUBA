package ar.uba.fi.ingsoft1.product_example.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO con la información de un usuario")
public record UserDTO(
        @Schema(description = "ID del usuario", example = "1") Long id,
        @Schema(description = "Nombre del usuario", example = "Juan") String firstName,
        @Schema(description = "Apellido del usuario", example = "Pérez") String lastName,
        @Schema(description = "Edad del usuario", example = "25") Integer age,
        @Schema(description = "Género del usuario", example = "MASCULINO") String gender,
        @Schema(description = "Dirección del usuario", example = "Av. Paseo Colón 850") String address,
        @Schema(description = "Imagen del usuario en base64", example = "data:image/png;base64,iVBORw0KG...") String base64Image,
        @Schema(description = "Nombre de usuario", example = "jperez") String username,
        @Schema(description = "Email fiuba del usuario", example = "jperez@fi.uba.ar") String email,
        @Schema(description = "Rol del usuario", example = "ROLE_USER") String role) {
    public UserDTO(User user) {
        this(user.getId(), user.getFirstName(), user.getLastName(),
                user.getAge(), user.getGender(), user.getAddress(), user.getBase64Image(),
                user.getUsername(), user.getEmail(), user.getRole());
    }
}