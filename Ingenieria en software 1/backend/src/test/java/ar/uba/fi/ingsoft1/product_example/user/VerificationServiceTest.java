package ar.uba.fi.ingsoft1.product_example.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VerificationServiceTest {

    private static final String EXPECTED_EVENT = "user_signup";
    private static final String EXPECTED_EMAIL = "juan@fi.uba.ar";
    private static final String EXPECTED_USERNAME = "juan123";
    private static final String WEBHOOK_URL = "http://fake-n8n-webhook.test";
    private static final String BACKEND_URL = "http://localhost:21103/api";

    private VerificationTokenRepository tokenRepository;
    private UserRepository userRepository;
    private VerificationService verificationService;
    private User testUser;

    @BeforeEach
    void setup() {
        tokenRepository = mock(VerificationTokenRepository.class);
        userRepository = mock(UserRepository.class);
        verificationService = new VerificationService(tokenRepository, userRepository);

        ReflectionTestUtils.setField(verificationService, "backendUrl", BACKEND_URL);
        ReflectionTestUtils.setField(verificationService, "expiration", Duration.ofHours(24));
        ReflectionTestUtils.setField(verificationService, "n8nWebhookUrl", "");

        testUser = new User("Juan", "Pérez", 25, "M", "Street 123", null, EXPECTED_USERNAME, "password", "ROLE_USER");
        testUser.setId(1L);
        testUser.setEmail(EXPECTED_EMAIL);
        testUser.setEmailVerified(false);
    }

    private RestTemplate setupN8NWebhook(String webhookUrl) throws Exception {
        ReflectionTestUtils.setField(verificationService, "n8nWebhookUrl", webhookUrl);
        RestTemplate restTemplateMock = mock(RestTemplate.class);
        Field restField = VerificationService.class.getDeclaredField("rest");
        restField.setAccessible(true);
        restField.set(verificationService, restTemplateMock);
        return restTemplateMock;
    }

    private void setupTokenRepositoryMock() {
        when(tokenRepository.save(any(VerificationToken.class))).thenAnswer(invocation -> {
            VerificationToken token = invocation.getArgument(0);
            token.setId(1L);
            return token;
        });
    }

    @Test
    void sendVerificationEmailCreatesAndSavesToken() {
        setupTokenRepositoryMock();

        verificationService.sendVerificationEmail(testUser);

        verify(tokenRepository, times(1)).save(any(VerificationToken.class));
    }

    /**
     * Tests that verify returns true when token is valid, unused, and not expired.
     * Verifies that the token is marked as used and user email is verified.
     */
    @Test
    void verifyReturnsTrueForValidToken() {
        String tokenValue = "valid-token-123";
        VerificationToken token = new VerificationToken(1L, tokenValue, testUser,
                Instant.now().plus(Duration.ofHours(24)), false);

        when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));
        when(tokenRepository.save(any(VerificationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = verificationService.verify(tokenValue);

        assertTrue(result);
        assertTrue(token.isUsed());
        assertTrue(testUser.isEmailVerified());
        verify(tokenRepository, times(1)).save(token);
        verify(userRepository, times(1)).save(testUser);
    }

    /**
     * Tests that verify returns false when token is not found.
     * Verifies that non-existent tokens are rejected.
     */
    @Test
    void verifyReturnsFalseWhenTokenNotFound() {
        String tokenValue = "nonexistent-token";
        when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.empty());

        boolean result = verificationService.verify(tokenValue);

        assertFalse(result);
        verify(tokenRepository, never()).save(any(VerificationToken.class));
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Tests that verify returns false when token is already used.
     * Verifies that used tokens cannot be reused for verification.
     */
    @Test
    void verifyReturnsFalseWhenTokenAlreadyUsed() {
        String tokenValue = "used-token";
        VerificationToken token = new VerificationToken(1L, tokenValue, testUser,
                Instant.now().plus(Duration.ofHours(24)), true); // Already used

        when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

        boolean result = verificationService.verify(tokenValue);

        assertFalse(result);
        verify(tokenRepository, never()).save(any(VerificationToken.class));
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Tests that verify returns false when token is expired.
     * Verifies that expired tokens cannot be used for verification.
     */
    @Test
    void verifyReturnsFalseWhenTokenExpired() {
        String tokenValue = "expired-token";
        VerificationToken token = new VerificationToken(1L, tokenValue, testUser,
                Instant.now().minus(Duration.ofHours(1)), false); // Expired 1 hour ago

        when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

        boolean result = verificationService.verify(tokenValue);

        assertFalse(result);
        verify(tokenRepository, never()).save(any(VerificationToken.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void sendVerificationEmailCallsN8NWebhookWhenConfigured() throws Exception {
        RestTemplate restTemplateMock = setupN8NWebhook(WEBHOOK_URL);
        setupTokenRepositoryMock();

        when(restTemplateMock.postForEntity(eq(WEBHOOK_URL), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        verificationService.sendVerificationEmail(testUser);

        verify(tokenRepository, times(1)).save(any(VerificationToken.class));
        verify(restTemplateMock, times(1)).postForEntity(eq(WEBHOOK_URL), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    void sendVerificationEmailSendsCorrectPayloadToN8N() throws Exception {
        RestTemplate restTemplateMock = setupN8NWebhook(WEBHOOK_URL);
        setupTokenRepositoryMock();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<Map<String, Object>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        when(restTemplateMock.postForEntity(eq(WEBHOOK_URL), captor.capture(), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        verificationService.sendVerificationEmail(testUser);

        Map<String, Object> payload = captor.getValue().getBody();
        assertNotNull(payload);
        assertEquals(EXPECTED_EVENT, payload.get("event"));
        assertEquals(EXPECTED_EMAIL, payload.get("email"));
        assertEquals(EXPECTED_USERNAME, payload.get("username"));
        assertNotNull(payload.get("token"));
        assertTrue(payload.get("verificationUrl").toString().contains("/users/verify?token="));
    }

    @Test
    void sendVerificationEmailThrowsExceptionWhenN8NWebhookFails() throws Exception {
        RestTemplate restTemplateMock = setupN8NWebhook(WEBHOOK_URL);
        setupTokenRepositoryMock();

        RestClientException originalException = new RestClientException("Connection failed");
        when(restTemplateMock.postForEntity(eq(WEBHOOK_URL), any(HttpEntity.class), eq(Void.class)))
                .thenThrow(originalException);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            verificationService.sendVerificationEmail(testUser);
        });

        assertTrue(exception.getMessage().contains("Error enviando notificación a N8N"));
        assertEquals(originalException, exception.getCause());

        verify(tokenRepository, times(1)).save(any(VerificationToken.class));
        verify(restTemplateMock, times(1)).postForEntity(eq(WEBHOOK_URL), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    void sendVerificationEmailSkipsN8NWhenUrlIsNull() {
        ReflectionTestUtils.setField(verificationService, "n8nWebhookUrl", (String) null);
        setupTokenRepositoryMock();

        verificationService.sendVerificationEmail(testUser);

        verify(tokenRepository, times(1)).save(any(VerificationToken.class));
    }

    @Test
    void sendVerificationEmailSkipsN8NWhenUrlIsBlank() {
        ReflectionTestUtils.setField(verificationService, "n8nWebhookUrl", "");
        setupTokenRepositoryMock();

        verificationService.sendVerificationEmail(testUser);

        verify(tokenRepository, times(1)).save(any(VerificationToken.class));
    }

}
