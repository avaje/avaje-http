package io.avaje.http.generator.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BeanParamReaderTest {

  @Test
  void toJavaIdentifier_replacesUnsupportedCharacters() {
    assertThat(BeanParamReader.toJavaIdentifier("customer-info"))
      .isEqualTo("customer_info");
  }

  @Test
  void toJavaIdentifier_preservesValidIdentifier() {
    assertThat(BeanParamReader.toJavaIdentifier("invoiceCustomer"))
      .isEqualTo("invoiceCustomer");
  }

  @Test
  void toJavaIdentifier_handlesInvalidFirstCharacter() {
    assertThat(BeanParamReader.toJavaIdentifier("123customer"))
      .isEqualTo("_23customer");
  }
}
