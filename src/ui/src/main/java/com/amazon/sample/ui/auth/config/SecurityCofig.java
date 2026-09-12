package com.amazon.sample.ui.config;

import com.amazon.sample.ui.auth.jwt.JwtLoginSuccessHandler;
import com.amazon.sample.ui.auth.jwt.JwtLogoutSuccessHandler;
import com.amazon.sample.ui.auth.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * PHASE 2 UPDATE: form login now issues a JWT cookie on success
 * (JwtLoginSuccessHandler) instead of relying only on the default
 * session. Logout now also clears that cookie (JwtLogoutSuccessHandler).
 *
 * Still phase-1-scoped on authorization: anyExchange().permitAll() stays
 * until step 4 (RBAC), because cart/checkout/catalog still don't read
 * the JWT at all yet - that's step 3.
 */
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;
    private final JwtLoginSuccessHandler jwtLoginSuccessHandler;
    private final JwtLogoutSuccessHandler jwtLogoutSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService() {
        return username -> Mono.fromCallable(() ->
                        userRepository.findByUsername(username)
                                .map(this::toUserDetails)
                                .orElseThrow(() -> new UsernameNotFoundException("No user: " + username))
                )
                .subscribeOn(Schedulers.boundedElastic());
    }

    private UserDetails toUserDetails(com.amazon.sample.ui.auth.entities.UserEntity entity) {
        return User.withUsername(entity.getUsername())
                .password(entity.getPasswordHash())
                .authorities(AuthorityUtils.createAuthorityList("ROLE_" + entity.getRole()))
                .build();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .authenticationSuccessHandler(jwtLoginSuccessHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(jwtLogoutSuccessHandler)
                )
                // TODO before final submission: re-enable CSRF with a token in the
                // login/signup forms.
                .csrf(csrf -> csrf.disable())
                .build();
    }
}