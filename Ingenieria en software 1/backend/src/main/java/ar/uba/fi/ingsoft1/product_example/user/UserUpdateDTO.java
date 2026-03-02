package ar.uba.fi.ingsoft1.product_example.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.Optional;

@Schema(description = "DTO para actualizar los datos del usuario. Todos los campos son opcionales excepto base64Image que puede ser null para eliminar la imagen.")
public record UserUpdateDTO(
        @Schema(description = "Nombre del usuario", example = "Juan") Optional<@Size(min = 3, max = 100) String> firstName,
        @Schema(description = "Apellido del usuario", example = "Pérez") Optional<@Size(min = 3, max = 100) String> lastName,
        @Schema(description = "Edad del usuario", example = "25") Optional<@Min(0) Integer> age,
        @Schema(description = "Género del usuario", example = "MASCULINO") Optional<String> gender,
        @Schema(description = "Dirección del usuario", example = "Av. Paseo Colón 850") Optional<String> address,
        @Schema(description = "Imagen del usuario en base64 (null para eliminar)", example = "data:image/png;base64,iVBORw0KG...") String base64Image, 
        @Schema(description = "Nombre de usuario", example = "jperez") Optional<@Size(min = 3, max = 100) String> username,
        @Schema(description = "Email del usuario (debe ser @fi.uba.ar)", example = "jperez@fi.uba.ar") Optional<String> email) {
    public User applyTo(User user) {
        firstName.ifPresent(user::setFirstName);
        lastName.ifPresent(user::setLastName);
        age.ifPresent(user::setAge);
        gender.ifPresent(user::setGender);
        address.ifPresent(user::setAddress);
        // Handle base64Image: always apply (even if it is null to remove it)
        user.setBase64Image(base64Image);
        username.ifPresent(user::setUsername);
        email.ifPresent(user::setEmail);
        return user;
    }
}