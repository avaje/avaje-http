package org.example;

import jakarta.inject.Singleton;
import java.util.List;

/**
 * Simple generic service for testing request-scoped controllers with generic DI dependencies.
 */
@Singleton
public class GenericService<T> {

  public List<T> findAll() {
    return List.of();
  }
}
