package io.dinject.webroutegen.openapi;

import io.dinject.webroutegen.MethodReader;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class MethodDocBuilderTest {

  private final MethodDocBuilder builder = new MethodDocBuilder(mock(MethodReader.class), null);

  @Test
  public void testNormalisePath() {

    assertThat(builder.normalisePath(":foo")).isEqualTo("{foo}");
    assertThat(builder.normalisePath("/:foo")).isEqualTo("/{foo}");
    assertThat(builder.normalisePath("/:foo/")).isEqualTo("/{foo}/");
    assertThat(builder.normalisePath("/:foo/bar")).isEqualTo("/{foo}/bar");
    assertThat(builder.normalisePath("/:foo/:bar")).isEqualTo("/{foo}/{bar}");
    assertThat(builder.normalisePath("/:foo/:bar/")).isEqualTo("/{foo}/{bar}/");
    assertThat(builder.normalisePath("/:foo/:bar/bazz")).isEqualTo("/{foo}/{bar}/bazz");
    assertThat(builder.normalisePath("/:foo/:bar/:bazz")).isEqualTo("/{foo}/{bar}/{bazz}");

    assertThat(builder.normalisePath("{foo}")).isEqualTo("{foo}");
    assertThat(builder.normalisePath("/{foo}")).isEqualTo("/{foo}");
    assertThat(builder.normalisePath("/{foo}/")).isEqualTo("/{foo}/");
    assertThat(builder.normalisePath("/{foo}/bar")).isEqualTo("/{foo}/bar");
    assertThat(builder.normalisePath("/{foo}/{bar}")).isEqualTo("/{foo}/{bar}");
    assertThat(builder.normalisePath("/{foo}/{bar}/")).isEqualTo("/{foo}/{bar}/");
    assertThat(builder.normalisePath("/{foo}/{bar}/bazz")).isEqualTo("/{foo}/{bar}/bazz");
    assertThat(builder.normalisePath("/{foo}/{bar}/{bazz}")).isEqualTo("/{foo}/{bar}/{bazz}");

  }
}
