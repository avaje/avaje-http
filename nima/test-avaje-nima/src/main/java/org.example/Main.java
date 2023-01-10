package org.example;

import io.avaje.inject.BeanScope;
import io.avaje.inject.InjectModule;
import io.avaje.jsonb.Jsonb;
import io.avaje.nima.Nima;

@InjectModule(requires = Jsonb.class)
public class Main {

  public static void main(String[] args) {

    BeanScope beanScope = BeanScope.builder()
      .build();//.beans(Jsonb.builder().build()).build();

    Nima nima = new Nima();
    nima.configure(beanScope);
    nima.start(8082);
  }
}
