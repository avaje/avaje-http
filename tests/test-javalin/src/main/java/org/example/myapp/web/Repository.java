package org.example.myapp.web;

import java.util.List;

public interface Repository<T,I> {

  T findById(I id);

  List<T> findAll();

  I save(T bean);
}

