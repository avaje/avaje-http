package org.example.webserver;

import io.avaje.http.api.*;
import io.javalin.http.Context;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

/**
 * Hello resource manager.
 * <p>
 * Simple API for Hello resources.
 */
//@Hidden
@Valid
@Controller
@Path("/hello")
class HelloController {

  private int retryCounter;

  @Produces(MediaType.TEXT_PLAIN)
  @Get("message")
  String getPlainMessage() {
    return "hello world";
  }

  @Get("retry")
  String retry() {
    retryCounter++;
    if (retryCounter == 3) {
      retryCounter = 0;
      return "All good at 3rd attempt";
    } else {
      throw new IllegalStateException("Barf");
    }
  }

  @Get
  String basicAuth(@Header String authorization) {
    final String[] split = authorization.split(" ");
    if (split[0].equals("Basic")) {
      return "decoded: " + new String(Base64.getDecoder().decode(split[1]), UTF_8);
    }
    return "NotExpected: " + authorization;
  }

  @Get("stream")
  void stream(Context context) {
    // simulate x-json-stream response
    context.header("content-type", "application/x-json-stream");
    String content =
      "{\"id\":1, \"name\":\"one\"}\n" +
        "{\"id\":2, \"name\":\"two\"}\n" +
        "{\"id\":3, \"name\":\"three\"}\n" +
        "{\"id\":4, \"name\":\"four\"}\n";
    context.result(content);
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
  @Get("/{id}/{date}")
  HelloDto hello(int id, LocalDate date, String otherParam) {
    return new HelloDto(id, date.toString(), otherParam);
  }

  /**
   * Find Hellos by name.
   *
   * @param name       The name to search for
   * @param otherParam My option parameter
   * @return The Hellos that we found.
   */
  @Get("/findbyname/{name}")
  List<HelloDto> findByName(String name, String otherParam) {
    return new ArrayList<>();
  }

  /**
   * Simple example post with JSON body response.
   */
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
  @Post("/savebean/{foo}")
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

  @Get
  List<HelloDto> getAll() {
    return findAll();
  }

  //  @Hidden
  @Delete("{id}")
  void deleteById(int id) {
    System.out.println("deleting " + id);
  }

  @Produces("text/plain")
  @Get("/withMatrix/{year};author;country;zone/{other}")
  String getWithMatrixParam(int year, String author, String country, String zone, String other, String extra) {
    return "yr:" + year + " au:" + author + " co:" + country + " zone:" + zone + " other:" + other + " extra:" + extra;
  }

  private List<HelloDto> findAll() {
    List<HelloDto> list = new ArrayList<>();
    list.add(new HelloDto(12, "Jim", "asd"));
    list.add(new HelloDto(13, "Spock", "456456"));
    return list;
  }
}
