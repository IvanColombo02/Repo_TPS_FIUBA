package ar.uba.fi.ingsoft1.product_example.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@NoArgsConstructor
@Data
@Entity
@AllArgsConstructor
public class VerificationToken {
    @Id @GeneratedValue private Long id;
    @Column(unique = true, nullable = false) private String token;
    @ManyToOne(optional = false) private User user;
    @Column(nullable = false) private Instant expiresAt;
    @Column(nullable = false) private boolean used = false;


    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public User getUser() {
        return user;
    }
}
