package org.example.myapp.service;

import org.example.myapp.web.Baz;
import org.example.myapp.web.Repository;

import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

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
