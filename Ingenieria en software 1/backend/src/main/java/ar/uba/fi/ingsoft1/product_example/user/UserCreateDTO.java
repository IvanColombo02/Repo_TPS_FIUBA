package ar.uba.fi.ingsoft1.product_example.user;

import jakarta.validation.constraints.*;

import java.util.function.Function;

public record UserCreateDTO(
        @Size(min = 3, max = 100) String firstName,
        @Size(min = 3, max = 100) String lastName,
        @Min(0) @NotNull Integer age,
        @NotBlank String gender,
        @Size(min = 3, max = 50)String address,
        String base64Image,
        @Size(min = 3, max = 100) String username,
        @Size(min = 6, max = 50) String password,
        @NotBlank String role,
        @Email @NotBlank String email
) implements UserCredentials {
    public User asUser(Function<String, String> encryptPassword) {
        return new User(firstName, lastName, age.intValue(),gender,address, base64Image,username, encryptPassword.apply(password), role);
    }
}
