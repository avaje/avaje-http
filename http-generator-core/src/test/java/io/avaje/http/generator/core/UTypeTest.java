package io.avaje.http.generator.core;

import org.junit.jupiter.api.Test;

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
}
