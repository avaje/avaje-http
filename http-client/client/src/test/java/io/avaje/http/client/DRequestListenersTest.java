package io.avaje.http.client;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DRequestListenersTest {

  private final StringBuilder buffer = new StringBuilder();

  @Test
  void response() {

    DRequestListeners listeners = new DRequestListeners(listeners());

    listeners.response(Mockito.mock(RequestListener.Event.class));

    assertThat(buffer.toString()).isEqualTo("one|two|");
  }

  private List<RequestListener> listeners() {
    return Arrays.asList(event -> buffer.append("one|"), event -> buffer.append("two|"));
  }

}
