package ar.uba.fi.ingsoft1.product_example.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for login error messages to ensure clear and differentiated error responses.
 * Verifies that backend returns distinct error messages for invalid credentials and unvalidated email.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LoginErrorMessagesTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String VALID_USERNAME = "testuser";
    private static final String VALID_EMAIL = "testuser@fi.uba.ar";
    private static final String VALID_PASSWORD = "password123";
    private static final String WRONG_PASSWORD = "wrongpassword";

    /**
     * Test that login returns clear error message when credentials are incorrect.
     * Verifies that backend returns 401 UNAUTHORIZED with "Contraseña incorrecta" message.
     */
    @Test
    void loginReturnsClearErrorWhenPasswordIsIncorrect() throws Exception {
        // Arrange: Create a user with validated email and correct password
        User user = new User("Test", "User", 25, "M", "Test Address", null, VALID_USERNAME, 
                passwordEncoder.encode(VALID_PASSWORD), "ROLE_USER");
        user.setEmail(VALID_EMAIL);
        user.setEmailVerified(true);
        userRepository.save(user);

        // Act: Attempt login with wrong password
        String loginJson = """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(VALID_USERNAME, WRONG_PASSWORD);

        // Assert: Verify that error response is returned with clear message
        mockMvc.perform(post("/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Contraseña incorrecta"));
    }

    /**
     * Test that login returns clear error message when user does not exist.
     * Verifies that backend returns 401 UNAUTHORIZED with "Usuario o email inexistente" message.
     */
    @Test
    void loginReturnsClearErrorWhenUserDoesNotExist() throws Exception {
        // Arrange: No user exists in repository

        // Act: Attempt login with non-existent user
        String loginJson = """
                {
                    "username": "nonexistentuser",
                    "password": "%s"
                }
                """.formatted(VALID_PASSWORD);

        // Assert: Verify that error response is returned with clear message
        mockMvc.perform(post("/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Usuario o email inexistente"));
    }

    /**
     * Test that login returns distinct error message when email is not validated.
     * Verifies that backend returns 401 UNAUTHORIZED with "EMAIL_NOT_VERIFIED" message.
     */
    @Test
    void loginReturnsDistinctErrorWhenEmailNotValidated() throws Exception {
        // Arrange: Create a user with unvalidated email
        User user = new User("Test", "User", 25, "M", "Test Address", null, VALID_USERNAME, 
                passwordEncoder.encode(VALID_PASSWORD), "ROLE_USER");
        user.setEmail(VALID_EMAIL);
        user.setEmailVerified(false); // Email not validated
        userRepository.save(user);

        // Act: Attempt login with correct credentials but unvalidated email
        String loginJson = """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(VALID_USERNAME, VALID_PASSWORD);

        // Assert: Verify that error response is returned with distinct message for email verification
        mockMvc.perform(post("/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("EMAIL_NOT_VERIFIED"));
    }

    /**
     * Test that error messages are differentiated correctly.
     * Verifies that invalid credentials error is different from email not validated error.
     */
    @Test
    void errorMessagesAreDifferentiatedCorrectly() throws Exception {
        // Arrange: Create user with unvalidated email
        User unvalidatedUser = new User("Unvalidated", "User", 25, "M", "Address", null, "unvalidated", 
                passwordEncoder.encode(VALID_PASSWORD), "ROLE_USER");
        unvalidatedUser.setEmail("unvalidated@fi.uba.ar");
        unvalidatedUser.setEmailVerified(false);
        userRepository.save(unvalidatedUser);

        // Create user with validated email
        User validatedUser = new User("Validated", "User", 25, "M", "Address", null, "validated", 
                passwordEncoder.encode(VALID_PASSWORD), "ROLE_USER");
        validatedUser.setEmail("validated@fi.uba.ar");
        validatedUser.setEmailVerified(true);
        userRepository.save(validatedUser);

        // Act & Assert: Test unvalidated email error
        String unvalidatedLoginJson = """
                {
                    "username": "unvalidated",
                    "password": "%s"
                }
                """.formatted(VALID_PASSWORD);

        mockMvc.perform(post("/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(unvalidatedLoginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("EMAIL_NOT_VERIFIED"));

        // Act & Assert: Test invalid password error (different from email error)
        String invalidPasswordLoginJson = """
                {
                    "username": "validated",
                    "password": "%s"
                }
                """.formatted(WRONG_PASSWORD);

        mockMvc.perform(post("/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPasswordLoginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Contraseña incorrecta"));
    }

    /**
     * Test that login with validated email and correct credentials succeeds.
     * Verifies that login works when all conditions are met.
     */
    @Test
    void loginSucceedsWhenEmailValidatedAndCredentialsCorrect() throws Exception {
        // Arrange: Create user with validated email and correct password
        User user = new User("Test", "User", 25, "M", "Test Address", null, VALID_USERNAME, 
                passwordEncoder.encode(VALID_PASSWORD), "ROLE_USER");
        user.setEmail(VALID_EMAIL);
        user.setEmailVerified(true);
        userRepository.save(user);

        // Act: Attempt login with correct credentials
        String loginJson = """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(VALID_USERNAME, VALID_PASSWORD);

        // Assert: Verify that login succeeds
        mockMvc.perform(post("/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }
}
