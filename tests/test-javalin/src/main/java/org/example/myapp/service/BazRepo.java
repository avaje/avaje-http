package org.example.myapp.service;

import org.example.myapp.web.Baz;
import org.example.myapp.web.Repository;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class BazRepo implements Repository<Baz,Long> {

  @Override
  public Baz findById(Long id) {
    return new Baz();
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
