package org.example;


import io.avaje.http.api.*;

import javax.validation.Valid;
import java.net.URL;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

@Client
@Path("moo")
public interface Simple {

  // UUID goo, Boolean option,
  @Get("{uid}/{date}")
  HttpResponse<String> byIdg(long uid, LocalDate date, @Header URL access, @QueryParam("my-dat") LocalDate dt);

  @Get("users/{user}/repos")
  List<Repo> listRepos(String user, String other);

  @Post
  Id save(Repo bean);

  @Post("/register")
  @Form
  void register(MyForm myForm);

  @Post("other")
  @Form
  void registerOther(String myName, String email);

  class Id {
    public long id;
  }

  @Valid
  class MyForm {

    String name;
    String email;
    boolean active;
    int notPublic;

    @Header
    public String bafHead;

    @QueryParam("my-parm")
    String mparam;

    public String getMparam() {
      return mparam;
    }

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
