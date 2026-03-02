package ar.uba.fi.ingsoft1.product_example.config.security;

import java.security.Principal;

public record JwtUserDetails(
                String username,
                String role) implements Principal {

        @Override
        public String getName() {
                return username;
        }
}