/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: MIT-0
 */

package com.amazon.sample.ui.services.catalog;

import com.amazon.sample.ui.services.catalog.model.Product;
import com.amazon.sample.ui.services.catalog.model.ProductPage;
import com.amazon.sample.ui.services.catalog.model.ProductTag;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CatalogService {
  Mono<ProductPage> getProducts(String tag, String order, int page, int size);

  Mono<Product> getProduct(String productId);

  Flux<ProductTag> getTags();

  /**
   * Admin-only. token is the caller's raw JWT (no "Bearer " prefix),
   * attached per-call as an Authorization header - see KiotaCatalogService
   * for why this is safe to do without touching the shared client.
   */
  Mono<Product> createProduct(
    String token,
    String name,
    String description,
    int price
  );

  /** Admin-only, same token convention as createProduct. */
  Mono<Void> deleteProduct(String token, String productId);
}
