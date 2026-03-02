package ar.uba.fi.ingsoft1.product_example.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class PasswordResetTest {

    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        passwordResetService.expiration = Duration.ofMinutes(30);
        passwordResetService.resetWebhookUrl = ""; // so it doesn't calls N8N

        user = new User();
        user.setUsername("juan");
        user.setEmail("juan@fi.uba.ar");
    }

    @Test
    void whenEmailExists_tokenIsGeneratedAndSaved() {
        when(userRepository.findByEmail("juan@fi.uba.ar")).thenReturn(Optional.of(user));

        boolean result = passwordResetService.requestResetForEmail("juan@fi.uba.ar");

        assertThat(result).isTrue();
        verify(tokenRepository, times(1)).save(argThat(token ->
                token.getUser().equals(user)
                        && token.getToken() != null
                        && token.getExpiresAt().isAfter(Instant.now())
        ));
    }

    @Test
    void whenEmailDoesNotExist_noTokenIsCreated() {
        when(userRepository.findByEmail("inexistente@fi.uba.ar")).thenReturn(Optional.empty());

        boolean result = passwordResetService.requestResetForEmail("inexistente@fi.uba.ar");

        assertThat(result).isFalse();
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void generatedTokensAreUnique() {
        when(userRepository.findByEmail("juan@fi.uba.ar")).thenReturn(Optional.of(user));

        passwordResetService.requestResetForEmail("juan@fi.uba.ar");
        passwordResetService.requestResetForEmail("juan@fi.uba.ar");

        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository, times(2)).save(captor.capture());

        var tokens = captor.getAllValues();
        String token1 = tokens.get(0).getToken();
        String token2 = tokens.get(1).getToken();

        assertThat(token1).isNotEqualTo(token2); // It's nearly impossible that the tokens are the same
    }
    
    @Test
    void tokenExpirationIsCorrect() {
        when(userRepository.findByEmail("juan@fi.uba.ar")).thenReturn(Optional.of(user));

        passwordResetService.requestResetForEmail("juan@fi.uba.ar");

        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(captor.capture());

        Instant expectedExpiration = Instant.now().plus(passwordResetService.expiration);
        
        assertThat(captor.getValue().getExpiresAt())
            .isCloseTo(expectedExpiration, within(1, ChronoUnit.SECONDS));
    }

    @Test
    void whenEmailExists_webhookIsCalledWithToken() throws Exception {
        // Checks if the arguments sent in the payload are correct
        when(userRepository.findByEmail("juan@fi.uba.ar")).thenReturn(Optional.of(user));

        // Sets a random fake resetWebhookUrl
        passwordResetService.resetWebhookUrl = "http://fake-webhook.test"; 

        RestTemplate restTemplateMock = mock(RestTemplate.class);
        Field restField = PasswordResetService.class.getDeclaredField("rest");
        restField.setAccessible(true);
        restField.set(passwordResetService, restTemplateMock);

        boolean result = passwordResetService.requestResetForEmail("juan@fi.uba.ar");

        assertThat(result).isTrue();

        // "Catches" the payload
        ArgumentCaptor<HttpEntity<Map<String, Object>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplateMock).postForEntity(eq("http://fake-webhook.test"), captor.capture(), eq(Void.class));

        Map<String, Object> payload = captor.getValue().getBody();


        assertThat(payload.get("email")).isEqualTo("juan@fi.uba.ar");
        assertThat(payload.get("username")).isEqualTo("juan");
        assertThat(payload.get("event")).isEqualTo("forgot_password");
        assertThat(payload.get("token")).isNotNull();

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        PasswordResetToken savedToken = tokenCaptor.getValue();

        assertThat(savedToken.getUser()).isEqualTo(user);
        assertThat(savedToken.getToken()).isNotNull();
        assertThat(savedToken.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void whenTokenIsValid_passwordIsResetAndTokenInvalidated() {
        String token = "valid-token";
        user.setPassword("oldEncodedPassword");

        PasswordResetToken prt = new PasswordResetToken(1L, token, user,
                Instant.now().plus(Duration.ofMinutes(10)), false);

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(prt));
        when(passwordEncoder.matches("newPassword", "oldEncodedPassword")).thenReturn(false);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        var result = passwordResetService.resetPassword(token, "newPassword");

        assertThat(result).isEqualTo(PasswordResetService.ResetPasswordResult.SUCCESS);
        assertThat(user.getPassword()).isEqualTo("encodedNewPassword");
        assertThat(prt.isUsed()).isTrue();
        verify(userRepository).save(user);
        verify(tokenRepository).save(prt);
    }

    @Test
    void whenTokenIsExpired_returnsInvalidToken() {
        String token = "expired-token";
        PasswordResetToken prt = new PasswordResetToken(1L, token, user,
                Instant.now().minus(Duration.ofMinutes(1)), false);
        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(prt));

        var result = passwordResetService.resetPassword(token, "newPassword");

        assertThat(result).isEqualTo(PasswordResetService.ResetPasswordResult.INVALID_TOKEN);
        verify(userRepository, never()).save(any());
    }

    @Test
    void whenTokenDoesNotExist_returnsInvalidToken() {
        when(tokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        var result = passwordResetService.resetPassword("invalid-token", "newPassword");

        assertThat(result).isEqualTo(PasswordResetService.ResetPasswordResult.INVALID_TOKEN);
    }

    @Test
    void whenPasswordTooShort_returnsInvalidLength() {
        String token = "valid-token";
        PasswordResetToken prt = new PasswordResetToken(1L, token, user,
                Instant.now().plus(Duration.ofMinutes(5)), false);
        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(prt));

        var result = passwordResetService.resetPassword(token, "123");

        assertThat(result).isEqualTo(PasswordResetService.ResetPasswordResult.INVALID_LENGTH);
    }

    @Test
    void whenPasswordSameAsOld_returnsSameAsOld() {
        String token = "valid-token";
        PasswordResetToken prt = new PasswordResetToken(1L, token, user,
                Instant.now().plus(Duration.ofMinutes(5)), false);
        user.setPassword("encodedOldPassword");
        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(prt));
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);

        var result = passwordResetService.resetPassword(token, "oldPassword");

        assertThat(result).isEqualTo(PasswordResetService.ResetPasswordResult.SAME_AS_OLD);
    }

    @Test
    void whenTokenAlreadyUsed_returnsInvalidToken() {
        String token = "used-token";
        PasswordResetToken prt = new PasswordResetToken(1L, token, user,
                Instant.now().plus(Duration.ofMinutes(5)), true);
        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(prt));

        var result = passwordResetService.resetPassword(token, "newPassword");

        assertThat(result).isEqualTo(PasswordResetService.ResetPasswordResult.INVALID_TOKEN);
    }

    @Test
    void whenTokenIsValid_passwordIsEncodedAndSaved() {
        String rawPassword = "newpassword123";
        String encodedPassword = "encoded_" + rawPassword;

        User user = new User();
        user.setPassword("oldPassword");
        PasswordResetToken token = new PasswordResetToken(1L, "valid-token", user,
                Instant.now().plus(Duration.ofMinutes(30)), false);

        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(passwordEncoder.matches(rawPassword, "oldPassword")).thenReturn(false);

        PasswordResetService.ResetPasswordResult result =
                passwordResetService.resetPassword("valid-token", rawPassword);

        assertThat(result).isEqualTo(PasswordResetService.ResetPasswordResult.SUCCESS);
        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(argThat(savedUser ->
                savedUser.getPassword().equals(encodedPassword)));
    }
}