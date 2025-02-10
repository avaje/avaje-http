package org.example.web.myapp;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import io.avaje.jex.http.Context;
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
  WebHelloDto hello(int id, LocalDate date, String otherParam) {
    return new WebHelloDto(id, date.toString(), otherParam);
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
  List<WebHelloDto> findByName(String name, @QueryParam("my-param") @Default("one") String myParam) {
    return new ArrayList<>();
  }

  /**
   * Simple example post with JSON body response.
   */
  @Produces(MediaType.APPLICATION_JSON_PATCH_JSON)
  @Post
  WebHelloDto post(WebHelloDto dto) {
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
  void saveBean(String foo, WebHelloDto dto, Context context) {
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
  WebHelloDto saveForm3(HelloForm helloForm) {
    return new WebHelloDto(52, helloForm.name, helloForm.email);
  }

  @Produces("text/plain")
  @Get("withValidBean")
  String getGetBeanForm(@BeanParam GetBeanForm bean) {
    return "ok name:" + bean.getName();
  }

  @Hidden
  @Get
  List<WebHelloDto> getAll() {
    return myService.findAll();
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

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @Produces(value = "text/plain")
  @Get("takesOptional")
  String takesOptional(Optional<Long> myOptional) {
    return "takesOptional-" + myOptional;
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @Produces(value = "text/plain")
  @Get("takesOptionalEnum")
  String takesOptionalEnum(@QueryParam("myOptional") Optional<Foo.NestedEnum> myOptional) {
    return "takesOptionalEnum-" + myOptional;
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @Produces(value = "text/plain")
  @Get("takesOptionalString")
  String takesOptionalString(@QueryParam Optional<String> myOptional) {
    return "takesOptionalString-" + myOptional;
  }
}
