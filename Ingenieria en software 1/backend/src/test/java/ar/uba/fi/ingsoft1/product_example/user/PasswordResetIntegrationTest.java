package ar.uba.fi.ingsoft1.product_example.user;

import ar.uba.fi.ingsoft1.product_example.user.refresh_token.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.Duration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
class PasswordResetIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setup() {
        tokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setUsername("juan");
        user.setEmail("juan@fi.uba.ar");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole("USER");
        userRepository.save(user);

        PasswordResetToken token = new PasswordResetToken(
                null,
                "valid-token",
                user,
                Instant.now().plus(Duration.ofMinutes(30)),
                false);
        tokenRepository.save(token);
    }

    @Test
    void whenValidTokenAndPassword_thenReturnsSuccess() throws Exception {
        String json = """
                    {
                        "token": "valid-token",
                        "newPassword": "newPassword123"
                    }
                """;

        mockMvc.perform(post("/users/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void whenInvalidToken_thenReturnsError() throws Exception {
        String json = """
                    {
                        "token": "invalid-token",
                        "newPassword": "newPassword123"
                    }
                """;

        mockMvc.perform(post("/users/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("TOKEN_INVALID"));
    }

    @Test
    void whenPasswordTooShort_thenReturnsPasswordInvalid() throws Exception {
        User user = userRepository.findByEmail("juan@fi.uba.ar").orElseThrow();

        PasswordResetToken tokenForShortPassword = new PasswordResetToken(
                null,
                "token-short-password",
                user,
                Instant.now().plus(Duration.ofMinutes(30)),
                false);
        tokenRepository.save(tokenForShortPassword);

        String json = """
                    {
                        "token": "token-short-password",
                        "newPassword": "12345"
                    }
                """;

        mockMvc.perform(post("/users/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("PASSWORD_INVALID"));
    }

    @Test
    void whenPasswordSameAsOld_thenReturnsConflict() throws Exception {

        User user = userRepository.findByEmail("juan@fi.uba.ar").orElseThrow();
        user.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(user);

        PasswordResetToken tokenForSamePassword = new PasswordResetToken(
                null,
                "token-same-password",
                user,
                Instant.now().plus(Duration.ofMinutes(30)),
                false);
        tokenRepository.save(tokenForSamePassword);

        String json = """
                    {
                        "token": "token-same-password",
                        "newPassword": "password123"
                    }
                """;

        mockMvc.perform(post("/users/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict())
                .andExpect(content().string("PASSWORD_SAME_AS_OLD"));
    }

    @Test
    void whenTokenExpired_thenReturnsTokenInvalid() throws Exception {
        User user = userRepository.findByEmail("juan@fi.uba.ar").orElseThrow();
        tokenRepository.deleteAll();

        PasswordResetToken expiredToken = new PasswordResetToken(
                null,
                "expired-token",
                user,
                Instant.now().minus(Duration.ofMinutes(1)),
                false);
        tokenRepository.save(expiredToken);

        String json = """
                    {
                        "token": "expired-token",
                        "newPassword": "newPassword123"
                    }
                """;

        mockMvc.perform(post("/users/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("TOKEN_INVALID"));
    }

    @Test
    void whenTokenAlreadyUsed_thenReturnsTokenInvalid() throws Exception {
        User user = userRepository.findByEmail("juan@fi.uba.ar").orElseThrow();
        tokenRepository.deleteAll();

        PasswordResetToken usedToken = new PasswordResetToken(
                null,
                "used-token",
                user,
                Instant.now().plus(Duration.ofMinutes(30)),
                true);
        tokenRepository.save(usedToken);

        String json = """
                    {
                        "token": "used-token",
                        "newPassword": "newPassword123"
                    }
                """;

        mockMvc.perform(post("/users/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("TOKEN_INVALID"));
    }

    @Test
    void forgotPassword_whenEmailExists_thenReturnsOk() throws Exception {
        String json = """
                    {
                        "email": "juan@fi.uba.ar"
                    }
                """;

        mockMvc.perform(post("/users/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void forgotPassword_whenEmailDoesNotExist_thenReturnsNotFound() throws Exception {
        String json = """
                    {
                        "email": "nonexistent@fi.uba.ar"
                    }
                """;

        mockMvc.perform(post("/users/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void forgotPassword_whenEmailIsInvalid_thenReturnsBadRequest() throws Exception {
        String json = """
                    {
                        "email": "invalid-email"
                    }
                """;

        mockMvc.perform(post("/users/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }
}