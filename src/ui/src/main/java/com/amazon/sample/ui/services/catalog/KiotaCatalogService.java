/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: MIT-0
 */

package com.amazon.sample.ui.services.catalog;

import com.amazon.sample.ui.client.catalog.CatalogClient;
import com.amazon.sample.ui.services.catalog.model.CatalogMapper;
import com.amazon.sample.ui.services.catalog.model.Product;
import com.amazon.sample.ui.services.catalog.model.ProductPage;
import com.amazon.sample.ui.services.catalog.model.ProductTag;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class KiotaCatalogService implements CatalogService {

  private CatalogClient catalogClient;
  private CatalogMapper mapper;

  public KiotaCatalogService(
    CatalogClient catalogClient,
    CatalogMapper mapper
  ) {
    this.catalogClient = catalogClient;
    this.mapper = mapper;
  }

  @Override
  public Mono<ProductPage> getProducts(
    String tag,
    String order,
    int page,
    int size
  ) {
    var response = Mono.just(
      this.catalogClient.catalog()
        .size()
        .get(getRequestConfiguration -> {
          getRequestConfiguration.queryParameters.tags = tag;
        })
    );

    return Flux.fromIterable(
      this.catalogClient.catalog()
        .products()
        .get(getRequestConfiguration -> {
          getRequestConfiguration.queryParameters.order = order;
          getRequestConfiguration.queryParameters.page = page;
          getRequestConfiguration.queryParameters.size = size;
          getRequestConfiguration.queryParameters.tags = tag;
        })
    )
      .map(mapper::product)
      .collectList()
      .zipWith(response, (p, r) -> new ProductPage(page, size, r.getSize(), p));
  }

  @Override
  public Mono<Product> getProduct(String productId) {
    return Mono.just(
      this.catalogClient.catalog().products().byId(productId).get()
    ).map(mapper::product);
  }

  @Override
  public Flux<ProductTag> getTags() {
    return Flux.fromIterable(this.catalogClient.catalog().tags().get()).map(
      mapper::tag
    );
  }

  /**
   * NOTE ON WHY THIS IS SAFE: catalogClient is a shared singleton bean
   * used by every request/every user (see StoreServices.java), so it
   * would be wrong to store a token as global state on it. Instead the
   * token is attached to just THIS ONE outbound call via the request
   * configuration's headers - the same mechanism already used above for
   * query parameters. No two users' requests ever share state here.
   */
  @Override
  public Mono<Product> createProduct(
    String token,
    String name,
    String description,
    int price
  ) {
    var request =
      new com.amazon.sample.ui.client.catalog.models.model.Product();
    request.setName(name);
    request.setDescription(description);
    request.setPrice(price);

    return Mono.just(
      this.catalogClient.catalog()
        .products()
        .post(request, requestConfiguration -> {
          requestConfiguration.headers.add(
            "Authorization",
            "Bearer " + token
          );
        })
    ).map(mapper::product);
  }

  @Override
  public Mono<Void> deleteProduct(String token, String productId) {
    return Mono.fromRunnable(() ->
      this.catalogClient.catalog()
        .products()
        .byId(productId)
        .delete(requestConfiguration -> {
          requestConfiguration.headers.add(
            "Authorization",
            "Bearer " + token
          );
        })
    );
  }
}
