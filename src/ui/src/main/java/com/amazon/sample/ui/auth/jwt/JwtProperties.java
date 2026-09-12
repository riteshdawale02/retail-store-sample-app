package com.amazon.sample.ui.auth.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * NOTE ON THE SECRET NAME: this reads RETAIL_JWT_SECRET (no "ui" in the
 * name) deliberately. In step 3, Catalog/Cart/Checkout/Orders will each
 * read the SAME env var name (adapted to their own config convention) so
 * every service verifies tokens with the same shared HMAC secret. Keep
 * this name consistent everywhere - do not rename it per-service.
 */
@Component
@ConfigurationProperties(prefix = "retail.jwt")
@Getter
@Setter
public class JwtProperties {

    /** Must be at least 256 bits (32 chars) for HS256. Never commit a real value. */
    private String secret;

    private long expirationMinutes = 60;
}