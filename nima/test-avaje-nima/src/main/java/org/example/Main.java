package org.example;

import io.avaje.inject.BeanScope;
import io.avaje.nima.Nima;

//@InjectModule(requires = Jsonb.class)
public class Main {

  public static void main(String[] args) {

    BeanScope beanScope = BeanScope.builder()
      .build();

    Nima nima = new Nima();
    nima.configure(beanScope);
    nima.start(8082);
  }
}
