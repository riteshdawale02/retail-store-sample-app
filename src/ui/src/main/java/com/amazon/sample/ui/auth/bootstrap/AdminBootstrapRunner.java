package com.amazon.sample.ui.auth.bootstrap;

import com.amazon.sample.ui.auth.entities.UserEntity;
import com.amazon.sample.ui.auth.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Runs once at startup, on the main thread, before the app starts
 * accepting requests - this is NOT part of the reactive request path,
 * so plain blocking JpaRepository calls here are fine (no
 * boundedElastic bridging needed, unlike AuthService).
 *
 * Idempotent: safe to leave the env vars set across restarts. If the
 * user already exists, it only promotes the role if needed and never
 * touches the password of an existing account (so it won't silently
 * reset a password you changed later).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapRunner implements ApplicationRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AdminBootstrapProperties properties;

  @Override
  public void run(ApplicationArguments args) {
    if (
      !StringUtils.hasText(properties.getUsername()) ||
      !StringUtils.hasText(properties.getPassword())
    ) {
      log.info(
        "RETAIL_ADMIN_USERNAME / RETAIL_ADMIN_PASSWORD not set - skipping admin bootstrap"
      );
      return;
    }

    userRepository.findByUsername(properties.getUsername())
      .ifPresentOrElse(
        existing -> promoteIfNeeded(existing),
        this::createAdmin
      );
  }

  private void promoteIfNeeded(UserEntity existing) {
    if (!"ADMIN".equals(existing.getRole())) {
      existing.setRole("ADMIN");
      userRepository.save(existing);
      log.info("Promoted existing user '{}' to ADMIN", existing.getUsername());
    } else {
      log.info("Admin user '{}' already present", existing.getUsername());
    }
  }

  private void createAdmin() {
    UserEntity admin = new UserEntity();
    admin.setUsername(properties.getUsername());
    admin.setEmail(properties.getEmail());
    admin.setPasswordHash(passwordEncoder.encode(properties.getPassword()));
    admin.setRole("ADMIN");
    userRepository.save(admin);
    log.info("Created admin user '{}'", properties.getUsername());
  }
}
