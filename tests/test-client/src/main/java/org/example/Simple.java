package org.example;


import io.avaje.http.api.*;

import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Client
public interface Simple {

  // UUID goo, Boolean option,
  @Get("{uid}")
  HttpResponse<String> byId(long uid, LocalTime forT, @QueryParam("my-dat") LocalDate dt);

  @Get("users/{user}/repos")
  List<Repo> listRepos(String user, String other);

  @Post
  void save(Repo bean);

  @Post
  @Form
  void register(MyForm myForm);

  @Post("other")
  @Form
  void registerOther(String myName, String email);

  class MyForm {

    String name;
    String email;
    boolean active;
    int notPublic;

    public String getName() {
      return name;
    }

    public boolean isActive4() {
      return active;
    }

    public boolean isActive() {
      return active;
    }

    public String email() {
      return email;
    }
    public void setName(String name) {
      this.name = name;
    }

    public void setEmail(String email) {
      this.email = email;
    }


  }
}
