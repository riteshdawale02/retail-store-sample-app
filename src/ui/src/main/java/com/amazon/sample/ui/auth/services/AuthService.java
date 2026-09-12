package com.amazon.sample.ui.auth.services;

import com.amazon.sample.ui.auth.entities.UserEntity;
import com.amazon.sample.ui.auth.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * UI is a reactive (WebFlux) application, but Spring Data JPA is blocking.
 * We bridge the two here by running the JPA work on the boundedElastic
 * scheduler, which is the standard pattern for occasional blocking calls
 * (like login/signup) inside an otherwise reactive app.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<UserEntity> signup(String username, String email, String rawPassword) {
        return Mono.fromCallable(() -> {
                    if (userRepository.existsByUsername(username)) {
                        throw new IllegalArgumentException("Username already taken");
                    }
                    if (userRepository.existsByEmail(email)) {
                        throw new IllegalArgumentException("Email already registered");
                    }

                    UserEntity user = new UserEntity();
                    user.setUsername(username);
                    user.setEmail(email);
                    user.setPasswordHash(passwordEncoder.encode(rawPassword));

                    return userRepository.save(user);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
}