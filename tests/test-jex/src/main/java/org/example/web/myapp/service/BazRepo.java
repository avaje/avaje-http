package org.example.web.myapp.service;

import java.util.ArrayList;
import java.util.List;

import org.example.web.myapp.Baz;
import org.example.web.myapp.Repository;

import jakarta.inject.Singleton;

@Singleton
public class BazRepo implements Repository<Baz, Long> {

  @Override
  public Baz findById(Long id) {
    Baz baz = new Baz();
    baz.id = id;
    baz.name = "Baz" + id;
    //baz.startDate = LocalDate.of(2020, 1, 1);
    return baz;
  }

  @Override
  public List<Baz> findAll() {
    return new ArrayList<>();
  }

  @Override
  public Long save(Baz bean) {
    return 42L;
  }
}
