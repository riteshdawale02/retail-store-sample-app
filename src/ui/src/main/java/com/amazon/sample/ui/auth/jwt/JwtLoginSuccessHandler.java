package com.amazon.sample.ui.auth.jwt;

import java.net.URI;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements ServerAuthenticationSuccessHandler {

    public static final String COOKIE_NAME = "ACCESS_TOKEN";

    private final JwtService jwtService;

    @Override
    public Mono<Void> onAuthenticationSuccess(
            WebFilterExchange webFilterExchange,
            org.springframework.security.core.Authentication authentication) {

        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replaceFirst("^ROLE_", ""))
                .orElse("CUSTOMER");

        String token = jwtService.generateToken(username, role);

        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofHours(1))
                // secure(true) requires HTTPS - enable once TLS/ingress is in front
                // of this locally too, otherwise the browser silently drops the
                // cookie over plain HTTP and login will look broken.
                .secure(false)
                .sameSite("Lax")
                .build();

        var exchange = webFilterExchange.getExchange();
        exchange.getResponse().addCookie(cookie);
        exchange.getResponse().setStatusCode(HttpStatus.FOUND);
        exchange.getResponse().getHeaders().setLocation(URI.create("/"));

        return exchange.getResponse().setComplete();
    }
}