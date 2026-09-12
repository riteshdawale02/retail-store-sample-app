package com.amazon.sample.ui.auth.jwt;

import java.net.URI;
import java.time.Duration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JwtLogoutSuccessHandler implements ServerLogoutSuccessHandler {

    @Override
    public Mono<Void> onLogoutSuccess(
            WebFilterExchange webFilterExchange,
            org.springframework.security.core.Authentication authentication) {

        var exchange = webFilterExchange.getExchange();

        ResponseCookie expired = ResponseCookie.from(JwtLoginSuccessHandler.COOKIE_NAME, "")
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ZERO)
                .build();

        exchange.getResponse().addCookie(expired);
        exchange.getResponse().setStatusCode(HttpStatus.FOUND);
        exchange.getResponse().getHeaders().setLocation(URI.create("/login?logout"));

        return exchange.getResponse().setComplete();
    }
}