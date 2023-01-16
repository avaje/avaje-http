package org.example.dinject;

import io.avaje.http.client.BodyAdapter;
import io.avaje.http.client.HttpClientContext;
import io.avaje.http.client.JsonbBodyAdapter;
import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigureWithDITest {

  @Test
  void configureWith() {
    try (BeanScope beanScope = BeanScope.builder().build()) {

      assertThat(beanScope.contains("io.avaje.jsonb.Jsonb")).isTrue();

      HttpClientContext.Builder builder = HttpClientContext.builder();
      HttpClientContext.Builder.State state = builder.state();
      assertThat(state.baseUrl()).isNull();
      assertThat(state.bodyAdapter()).isNull();
      assertThat(state.client()).isNull();
      assertThat(state.requestLogging()).isTrue();
      assertThat(state.requestTimeout()).isEqualByComparingTo(Duration.ofSeconds(20));
      assertThat(state.retryHandler()).isNull();

      builder.configureWith(beanScope);
      BodyAdapter bodyAdapter = state.bodyAdapter();
      assertThat(bodyAdapter).isInstanceOf(JsonbBodyAdapter.class);
    }
  }
}
