package org.example.web.myapp;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import org.example.web.AppRoles;
import org.example.web.myapp.other.Foo;
import org.example.web.myapp.service.MyService;

import io.avaje.http.api.BeanParam;
import io.avaje.http.api.Controller;
import io.avaje.http.api.Default;
import io.avaje.http.api.Delete;
import io.avaje.http.api.Form;
import io.avaje.http.api.Get;
import io.avaje.http.api.MediaType;
import io.avaje.http.api.Path;
import io.avaje.http.api.Post;
import io.avaje.http.api.Produces;
import io.avaje.http.api.QueryParam;
import io.avaje.http.api.Valid;
import io.avaje.jex.Context;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.inject.Inject;

/**
 * Hello resource manager.
 * <p>
 * Simple API for Hello resources.
 */
//@Hidden
@Valid
@Controller
@Path("/hello")
class WebController {

  private final MyService myService;

  @Inject
  WebController(MyService myService) {
    this.myService = myService;
  }

  @Produces(MediaType.TEXT_PLAIN)
  @Get("message")
  String getPlainMessage() {
    return "hello world";
  }

  /**
   * Return the Hello DTO.
   *
   * @param id         The hello Id.
   * @param date       The name of the hello
   * @param otherParam Optional other parameter
   * @return The Hello DTO given the id and name.
   * @deprecated Please migrate away
   */
  @Deprecated
  @Roles({AppRoles.ADMIN, AppRoles.BASIC_USER})
  @Get("/:id/:date")
  HelloDto hello(int id, LocalDate date, String otherParam) {
    return new HelloDto(id, date.toString(), otherParam);
  }

  /**
   * Find Hellos by name.
   *
   * @param name    The name to search for
   * @param myParam My option parameter
   * @return The Hellos that we found.
   */
  @Roles(AppRoles.ADMIN)
  @Get("/findbyname/{name}")
  List<HelloDto> findByName(String name, @QueryParam("my-param") @Default("one") String myParam) {
    return new ArrayList<>();
  }

  /**
   * Simple example post with JSON body response.
   */
  @Produces(MediaType.APPLICATION_JSON_PATCH_JSON)
  @Post
  HelloDto post(HelloDto dto) {
    dto.name = "posted";
    return dto;
  }

  /**
   * Save the hello using json body.
   *
   * @param foo The hello doo id
   * @param dto The hello body as json
   */
//  @Roles({ADMIN})
  @Post("/savebean/:foo")
  void saveBean(String foo, HelloDto dto, Context context) {
    // save hello data ...
    System.out.println("save " + foo + " dto:" + dto);
    requireNonNull(foo);
    requireNonNull(dto);
    requireNonNull(context);
  }

  /**
   * Create the new Hello using a form.
   */
  @Post("saveform")
  @Form
  void saveForm(HelloForm helloForm) {
    System.out.println("saving " + helloForm);
  }

  @Form @Post("mySave")
  void saveForm324(@Default("junk") String name, String email, String url) {
    System.out.println("name " + name + " email:" + email + " url:" + url);
  }


  @Post("saveform2")
  @Form
  void saveForm2(String name, String email, String url) {
    System.out.println("name " + name + " email:" + email + " url:" + url);
  }

  @Post("saveform3")
  @Form
  HelloDto saveForm3(HelloForm helloForm) {
    return new HelloDto(52, helloForm.name, helloForm.email);
  }

  @Produces("text/plain")
  @Get("withValidBean")
  String getGetBeanForm(@BeanParam GetBeanForm bean) {
    return "ok name:" + bean.getName();
  }

  @Hidden
  @Get
  List<HelloDto> getAll() {
    return myService.findAll();
  }

  @Get("/async")
  CompletableFuture<List<HelloDto>> getAllAsync() {
    return CompletableFuture.supplyAsync(() -> {
      // Simulate a delay as if an actual IO operation is being executed.
      // This also helps ensure that we aren't just getting lucky with timings.
      try {
        Thread.sleep(10L);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }

      return myService.findAll();
    }, Executors.newSingleThreadExecutor()); // Example of how to use a custom executor.
  }

  //  @Hidden
  @Delete(":id")
  void deleteById(int id) {
    System.out.println("deleting " + id);
  }

  @Produces("text/plain")
  @Get("/withMatrix/:year;author;country/:other")
  String getWithMatrixParam(int year, String author, String country, String other, String extra) {
    return "yr:" + year + " au:" + author + " co:" + country + " other:" + other + " extra:" + extra;
  }

  @Produces("text/plain")
  @Get("slash/{name}/<nam0>/other/<nam1>")
  String slashAccepting(String name, String nam0, String nam1) {
    return "got name:" + name + " splat0:" + nam0 + " splat1:" + nam1;
  }

  @Produces(value = "text/plain")
  @Get("controlStatusCode")
  String controlStatusCode(Context ctx) {
    ctx.status(201);
    return "controlStatusCode";
  }

  @Produces(value = "text/plain")
  @Get("takesNestedEnum")
  String takesNestedEnum(Foo.NestedEnum myEnum) {
    return "takesNestedEnum-" + myEnum;
  }
}
