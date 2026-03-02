package ar.uba.fi.ingsoft1.product_example.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class VerificationService {
    private final VerificationTokenRepository tokenRepo;
    private final UserRepository userRepository;
    private final RestTemplate rest = new RestTemplate();

    @Value("${app.verification.base-url}") String baseUrl;
    @Value("${app.verification.backend-url:http://localhost:21103/api}") String backendUrl;
    @Value("${app.verification.expiration}") Duration expiration;
    @Value("${app.n8n.webhook.url:}") String n8nWebhookUrl;

    public VerificationService(VerificationTokenRepository tokenRepo, UserRepository userRepository) {
        this.tokenRepo = tokenRepo;
        this.userRepository = userRepository;
    }

    public void sendVerificationEmail(User user) {
        String token = UUID.randomUUID().toString();
        tokenRepo.save(new VerificationToken(null, token, user, Instant.now().plus(expiration), false));

        if (n8nWebhookUrl != null && !n8nWebhookUrl.isBlank()) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("event", "user_signup");
            payload.put("email", user.getEmail());
            payload.put("username", user.getUsername());
            payload.put("token", token);
            String verificationUrl = backendUrl + "/users/verify?token=" + token;
            payload.put("verificationUrl", verificationUrl);
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                rest.postForEntity(n8nWebhookUrl, new HttpEntity<>(payload, headers), Void.class);
            } catch (Exception e) {
                throw new RuntimeException("Error enviando notificación a N8N: " + e.getMessage(), e);
            }
        }
    }

    public boolean verify(String token) {
        return tokenRepo.findByToken(token).filter(t -> !t.isUsed() && Instant.now().isBefore(t.getExpiresAt())).map(t -> {
            t.setUsed(true);
            tokenRepo.save(t);
            User u = t.getUser();
            u.setEmailVerified(true);
            userRepository.save(u);
            return true;
        }).orElse(false);
    }
}
