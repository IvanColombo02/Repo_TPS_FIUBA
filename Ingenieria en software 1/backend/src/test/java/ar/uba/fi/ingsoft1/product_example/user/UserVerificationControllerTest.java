package ar.uba.fi.ingsoft1.product_example.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserVerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VerificationService verificationService;

    @Test
    void verifyWithValidTokenRedirectsToLogin() throws Exception {
        String validToken = "valid-token-123";
        when(verificationService.verify(validToken)).thenReturn(true);

        mockMvc.perform(get("/users/verify")
                        .param("token", validToken))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://grupo-11.tp1.ingsoft1.fiuba.ar/login?message=email_verified"));
    }

    @Test
    void verifyWithInvalidTokenRedirectsToVerifyWithError() throws Exception {
        String invalidToken = "invalid-token-456";
        when(verificationService.verify(invalidToken)).thenReturn(false);

        mockMvc.perform(get("/users/verify")
                        .param("token", invalidToken))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://grupo-11.tp1.ingsoft1.fiuba.ar/verify?token=" + invalidToken + "&error=invalid"));
    }

    @Test
    void verifyWithExpiredTokenRedirectsToVerifyWithError() throws Exception {
        String expiredToken = "expired-token-789";
        when(verificationService.verify(expiredToken)).thenReturn(false);

        mockMvc.perform(get("/users/verify")
                        .param("token", expiredToken))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://grupo-11.tp1.ingsoft1.fiuba.ar/verify?token=" + expiredToken + "&error=invalid"));
    }
}

