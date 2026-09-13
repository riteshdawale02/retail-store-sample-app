/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: MIT-0
 */

package com.amazon.sample.ui.web.util;

import com.amazon.sample.ui.services.carts.CartsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * NOTE: populateAuth() below follows the exact same pattern as the
 * existing populateCart() - it subscribes to a reactive pipeline and
 * sets the model attribute in the callback, without the ModelAttribute
 * method itself waiting for it to finish. That's how the existing cart
 * population already works here, so this is consistent with it, not a
 * new risk. If you ever notice the login/logout links flicker or show
 * stale state on first paint, this shared timing characteristic
 * (present in both) is why - both could be converted to return
 * Mono<Void> together later if that becomes an actual problem, but
 * doing so now would touch the already-working cart badge, which is
 * out of scope for this change.
 */
@Slf4j
@ControllerAdvice(annotations = RequiresCommonAttributes.class)
public class CommonAttributesControllerAdvice {

  private final CartsService cartsService;

  @Value("${retail.ui.disable-demo-warnings}")
  private boolean disableDemoWarnings;

  public CommonAttributesControllerAdvice(CartsService cartsService) {
    this.cartsService = cartsService;
  }

  @ModelAttribute("common")
  public void populateCommon(ServerHttpRequest request, Model model) {
    model.addAttribute("disableDemoWarnings", disableDemoWarnings);
    populateCart(request, model);
    populateAuth(model);
  }

  private void populateCart(ServerHttpRequest request, Model model) {
    String sessionId = SessionIDUtil.getSessionId(request);

    cartsService
      .getCart(sessionId)
      .doOnNext(cart -> model.addAttribute("cart", cart))
      .subscribe();
  }

  private void populateAuth(Model model) {
    // Sensible defaults for the anonymous case, so templates never see
    // an unset/null variable even before the reactive lookup below
    // completes.
    model.addAttribute("isAuthenticated", false);
    model.addAttribute("isAdmin", false);
    model.addAttribute("username", "");

    ReactiveSecurityContextHolder.getContext()
      .map(ctx -> ctx.getAuthentication())
      .filter(auth -> auth != null && auth.isAuthenticated()
        && !"anonymousUser".equals(auth.getPrincipal()))
      .doOnNext(auth -> {
        model.addAttribute("isAuthenticated", true);
        model.addAttribute("username", auth.getName());
        boolean isAdmin = auth.getAuthorities().stream()
          .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        model.addAttribute("isAdmin", isAdmin);
      })
      .subscribe();
  }
}
