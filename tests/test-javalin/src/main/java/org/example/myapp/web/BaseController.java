package org.example.myapp.web;

import io.avaje.http.api.Get;
import io.avaje.http.api.Post;

import java.util.List;

abstract class BaseController<T, I> {

  protected final Repository<T, I> repository;

  BaseController(Repository<T, I> repository) {
    this.repository = repository;
  }

  @Get(":id")
  T getById(I id) {
    return repository.findById(id);
  }

  @Get
  List<T> findAll() {
    return repository.findAll();
  }

  @Post
  I save(T bean) {
    return repository.save(bean);
  }

}
