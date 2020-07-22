package org.example;

import io.dinject.controller.Controller;
import io.dinject.controller.Delete;
import io.dinject.controller.Form;
import io.dinject.controller.Get;
import io.dinject.controller.Header;
import io.dinject.controller.Path;
import io.dinject.controller.Post;
import io.dinject.controller.Put;
import io.helidon.common.http.FormParams;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import org.example.api.Foo;
import org.example.api.FooBody;

import java.util.ArrayList;
import java.util.List;

@Controller
@Path("/foo")
public class FooController {

  //@Produces("text/plain")
  @Get("hello")
  public String hello() {
    return "Hello from Foo";
  }

  @Get
  public List<Foo> getSome(String was) {
    List<Foo> foos = new ArrayList<>();
    foos.add(new Foo("Rob-" + was, 5));
    foos.add(new Foo("Fi", 4));
    return foos;
  }

  @Get("{name}")
  public Foo getOne(String name) {
    Foo foo = new Foo();
    foo.name = name;
    foo.age = 42;
    return foo;
  }

  @Post
  public Foo postIt(ServerRequest request, FooBody payload, @Header String userAgent) {
    Foo foo = new Foo();
    foo.name = payload.name + "=" + payload.getMessage() + " agent:" + userAgent;
    foo.age = payload.age;
    return foo;
  }

  @Put
  public Foo putIt(FooBody payload) {
    Foo foo = new Foo();
    foo.name = payload.name + "=" + payload.getMessage() + " - Put";
    foo.age = payload.age;
    return foo;
  }

  @Delete("{id}")
  public Foo deleteIt(long id, FooBody body) {
    Foo foo = new Foo();
    foo.name = body.name + "=" + body.getMessage() + " - Delete " + id;
    foo.age = body.age;
    return foo;
  }

  @Form
  @Post("/form")
  public Foo formTest(MyForm myForm, String lang, FormParams params) {

    Foo foo = new Foo();
    foo.name = myForm.name + " - " + myForm.id + " lang:" + lang + " params:" + params.toMap();
    foo.age = 56;
    return foo;
  }

  @Form
  @Post("/form2")
  public void form2Test(ServerRequest req, ServerResponse res) {
    req.content().as(FormParams.class)
      .thenAccept(formParams -> {
        MyForm myForm = new MyForm();
        myForm.id = formParams.first("id").orElse(null);
        myForm.name = formParams.first("name").orElse(null);
        res.send(formParams.toMap().toString());
      });
  }

  public static class MyForm {
    public String id;
    public String name;

    @Override
    public String toString() {
      return "MyForm{" +
        "id='" + id + '\'' +
        ", name='" + name + '\'' +
        '}';
    }
  }
}
