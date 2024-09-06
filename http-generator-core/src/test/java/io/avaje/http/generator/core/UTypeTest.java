package io.avaje.http.generator.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class UTypeTest {

  @Test
  void isJavaLangPackage() {
    assertTrue(UType.isJavaLangPackage("java.lang.F"));
    assertTrue(UType.isJavaLangPackage("java.lang.Foo"));
  }

  @Test
  void isJavaLangPackage_expect_false() {
    assertFalse(UType.isJavaLangPackage("java.lang.other.Foo"));
    assertFalse(UType.isJavaLangPackage("not.lang.Foo"));
  }

  @Test
  void parseNestedEnum() {
    UType uType = UType.parse("my.pack.Foo.NestedEnum");
    assertThat(uType.mainType()).isEqualTo("my.pack.Foo.NestedEnum");
    assertThat(uType.shortType()).isEqualTo("Foo.NestedEnum");
    assertThat(uType.shortTypeNested()).isEqualTo("NestedEnum");
  }
}
