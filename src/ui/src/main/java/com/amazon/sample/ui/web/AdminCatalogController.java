package com.amazon.sample.ui.web;

import com.amazon.sample.ui.auth.jwt.JwtLoginSuccessHandler;
import com.amazon.sample.ui.services.catalog.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

/**
 * Reachable only if SecurityConfig's /admin/** rule already let the
 * request through (i.e. the logged-in user has ROLE_ADMIN via the
 * session). That session check is separate from the JWT read below -
 * the JWT is what Catalog itself verifies, since Catalog knows nothing
 * about UI's session/DB, only about tokens signed with the shared secret.
 */
@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminCatalogController {

  private final CatalogService catalogService;

  private String extractToken(ServerHttpRequest request) {
    var cookie = request
      .getCookies()
      .getFirst(JwtLoginSuccessHandler.COOKIE_NAME);
    return cookie != null ? cookie.getValue() : "";
  }

  @GetMapping("")
  public String list(Model model) {
    model.addAttribute(
      "catalog",
      catalogService.getProducts("", "", 1, 100)
    );
    return "admin-products";
  }

  @PostMapping("")
  public Mono<String> create(
      @RequestParam String name,
      @RequestParam String description,
      @RequestParam int price,
      ServerHttpRequest request) {
    String token = extractToken(request);
    return catalogService
      .createProduct(token, name, description, price)
      .thenReturn("redirect:/admin/products");
  }

  @PostMapping("/{id}/delete")
  public Mono<String> delete(
      @PathVariable String id,
      ServerHttpRequest request) {
    String token = extractToken(request);
    return catalogService
      .deleteProduct(token, id)
      .thenReturn("redirect:/admin/products");
  }
}
