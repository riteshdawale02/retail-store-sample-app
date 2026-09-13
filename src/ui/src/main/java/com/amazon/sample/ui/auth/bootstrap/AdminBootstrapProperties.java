package com.amazon.sample.ui.auth.bootstrap;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Optional. Only takes effect if both username and password are set
 * (via RETAIL_ADMIN_USERNAME / RETAIL_ADMIN_PASSWORD env vars). Left
 * blank by default so nothing happens in an environment that hasn't
 * explicitly opted in.
 */
@Component
@ConfigurationProperties(prefix = "retail.admin")
@Getter
@Setter
public class AdminBootstrapProperties {

  private String username = "";
  private String password = "";
  private String email = "admin@example.com";
}
