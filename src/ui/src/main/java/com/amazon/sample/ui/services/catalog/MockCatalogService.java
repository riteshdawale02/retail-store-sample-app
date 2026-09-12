
package com.amazon.sample.ui.services.catalog;

import com.amazon.sample.ui.services.catalog.model.Product;
import com.amazon.sample.ui.services.catalog.model.ProductPage;
import com.amazon.sample.ui.services.catalog.model.ProductTag;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Data;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

public class MockCatalogService implements CatalogService {

  private Map<String, Product> products;
  private Map<String, ProductTag> tags;

  public MockCatalogService() {
    tags = loadTagsFromJson();
    products = loadProductsFromJson();
  }

  private Map<String, ProductTag> loadTagsFromJson() {
    Map<String, ProductTag> tagMap = new HashMap<>();

    try {
      // Load the JSON file from classpath
      InputStream inputStream = getClass()
        .getResourceAsStream("/data/tags.json");
      if (inputStream == null) {
        throw new RuntimeException("Could not find tags.json in classpath");
      }

      // Read the JSON content
      ObjectMapper mapper = new ObjectMapper();
      List<TagData> tagList = mapper.readValue(
        inputStream,
        mapper
          .getTypeFactory()
          .constructCollectionType(List.class, TagData.class)
      );

      // Convert to ProductTag objects and populate the map
      for (TagData tag : tagList) {
        tagMap.put(
          tag.getName(),
          new ProductTag(tag.getName(), tag.getDisplayName())
        );
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to load tags from JSON file", e);
    }

    return tagMap;
  }

  private Map<String, Product> loadProductsFromJson() {
    Map<String, Product> productMap = new HashMap<>();

    try {
      // Load the JSON file from classpath
      InputStream inputStream = getClass()
        .getResourceAsStream("/data/products.json");
      if (inputStream == null) {
        throw new RuntimeException("Could not find products.json in classpath");
      }

      // Read the JSON content
      ObjectMapper mapper = new ObjectMapper();
      List<ProductData> productList = mapper.readValue(
        inputStream,
        mapper
          .getTypeFactory()
          .constructCollectionType(List.class, ProductData.class)
      );

      // Convert to Product objects and populate the map
      for (ProductData productData : productList) {
        // Convert tags from strings to ProductTag objects
        List<ProductTag> productTags = productData
          .getTags()
          .stream()
          .map(tagName -> tags.get(tagName))
          .filter(Objects::nonNull)
          .collect(Collectors.toList());

        Product product = new Product(
          productData.getId(),
          productData.getName(),
          productData.getDescription(),
          productData.getPrice(),
          productTags
        );

        productMap.put(product.getId(), product);
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to load products from JSON file", e);
    }

    return productMap;
  }

  @Data
  private static class TagData {

    private String name;
    private String displayName;
  }

  @Data
  private static class ProductData {

    private String id;
    private String name;
    private String description;
    private int price;
    private List<String> tags;
  }

  @Override
  public Mono<ProductPage> getProducts(
    String tag,
    String order,
    int page,
    int size
  ) {
    System.out.println("Tag is " + tag);
    List<Product> productList =
      this.products.values()
        .stream()
        .sorted(Comparator.comparing(Product::getName))
        .filter(product -> tag.isBlank() || product.hasTag(tag))
        .collect(Collectors.toList());

    int end = page * size;

    if (end > productList.size()) {
      end = productList.size();
    }

    return Mono.just(
      new ProductPage(
        page,
        size,
        productList.size(),
        productList.subList((page - 1) * size, end)
      )
    );
  }

  @Override
  public Mono<Product> getProduct(String productId) {
    return Mono.just(this.products.get(productId));
  }

  @Override
  public Flux<ProductTag> getTags() {
    return Flux.fromIterable(this.tags.values());
  }
  @Override public Mono<Product> createProduct( String token, String name, String description, int price ) { // Mock mode has no real auth backend and no admin concept - it exists // only for local UI-only development when no catalog endpoint is
    String id = UUID.randomUUID().toString();
    Product product = new Product(id, name, description, price, List.of());
    this.products.put(id, product); return Mono.just(product);
  }

  @Override public Mono<Void> deleteProduct(String token, String productId) {
    this.products.remove(productId); return Mono.empty(); 
  }
  
}
