package ar.uba.fi.ingsoft1.product_example.user;

import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static ar.uba.fi.ingsoft1.product_example.user.UserConstants.MIN_PASSWORD_LENGHT;

@Service
class PasswordResetService {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    enum ResetPasswordResult {
        SUCCESS,
        INVALID_TOKEN,
        INVALID_LENGTH,
        SAME_AS_OLD
    }

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate rest = new RestTemplate();

    @Value("${app.reset.expiration}")
    Duration expiration;
    @Value("${app.reset.frontend-url:http://localhost:21100}")
    String frontendUrl;
    @Value("${app.n8n.webhook.reset-password.url:}")
    String resetWebhookUrl;

    PasswordResetService(PasswordResetTokenRepository tokenRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean requestResetForEmail(String email) {
        var maybeUser = userRepository.findByEmail(email);
        if (maybeUser.isEmpty()) {
            return false;
        }
        var user = maybeUser.get();
        String token = UUID.randomUUID().toString();
        tokenRepository.save(new PasswordResetToken(null, token, user, Instant.now().plus(expiration), false));

        if (resetWebhookUrl != null && !resetWebhookUrl.isBlank()) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("event", "forgot_password");
            payload.put("email", user.getEmail());
            payload.put("username", user.getUsername());
            payload.put("token", token);
            String resetUrl = frontendUrl + "/reset?token=" + token;
            payload.put("resetUrl", resetUrl);
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                rest.postForEntity(resetWebhookUrl, new HttpEntity<>(payload, headers), Void.class);
            } catch (Exception e) {
                log.warn("Error enviando webhook de recuperación de contraseña a {}", resetWebhookUrl, e);
            }
        }
        return true;
    }

    public ResetPasswordResult resetPassword(String token, String newPassword) {
        var maybe = tokenRepository.findByToken(token);
        if (maybe.isEmpty())
            return ResetPasswordResult.INVALID_TOKEN;
        var prt = maybe.get();
        if (prt.isUsed())
            return ResetPasswordResult.INVALID_TOKEN;
        if (prt.getExpiresAt().isBefore(Instant.now()))
            return ResetPasswordResult.INVALID_TOKEN;

        var user = prt.getUser();

        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGHT) {
            return ResetPasswordResult.INVALID_LENGTH;
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            return ResetPasswordResult.SAME_AS_OLD;
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        prt.setUsed(true);
        tokenRepository.save(prt);
        return ResetPasswordResult.SUCCESS;
    }
}
