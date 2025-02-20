package org.example.jstache;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.jstach.jstache.JStache;
import io.jstach.jstache.JStacheConfig;
import io.jstach.jstache.JStacheLambda;
import io.jstach.jstache.JStacheType;

@Controller("/jstache")
public class JstacheController {

  @Get("/hello")
  public HelloWorldZeroDependency hello() {
    Person rick = new Person("Rick", LocalDate.now().minusYears(70));
    Person morty = new Person("Morty", LocalDate.now().minusYears(14));
    Person beth = new Person("Beth", LocalDate.now().minusYears(35));
    Person jerry = new Person("Jerry", LocalDate.now().minusYears(35));
    return new HelloWorldZeroDependency("Hello alien", List.of(rick, morty, beth, jerry));
  }

  @Get("/helloRuntime")
  public HelloWorld helloRuntime() {
    Person rick = new Person("Rick", LocalDate.now().minusYears(70));
    Person morty = new Person("Morty", LocalDate.now().minusYears(14));
    Person beth = new Person("Beth", LocalDate.now().minusYears(35));
    Person jerry = new Person("Jerry", LocalDate.now().minusYears(35));
    return new HelloWorld("Hello alien", List.of(rick, morty, beth, jerry));
  }

  /*
   * Annotate the root model with an inline mustache template
   */
  @JStacheConfig(type = JStacheType.STACHE)
  @JStache(
      template =
          """
          {{#people}}
          {{message}} {{name}}! You are {{#ageInfo}}{{age}}{{/ageInfo}} years old!
          {{#-last}}
          That is all for now!
          {{/-last}}
          {{/people}}
          """)
  public record HelloWorldZeroDependency(String message, List<Person> people) implements AgeLambdaSupport {}

  public record Person(String name, LocalDate birthday) {}

  public record AgeInfo(long age, String date) {}

  public interface AgeLambdaSupport {
    @JStacheLambda
    default AgeInfo ageInfo(Person person) {
      long age = ChronoUnit.YEARS.between(person.birthday(), LocalDate.now());
      String date = person.birthday().format(DateTimeFormatter.ISO_DATE);
      return new AgeInfo(age, date);
    }
  }

  @JStache(
      template =
          """
          {{#people}}
          {{message}} {{name}}! You are {{#ageInfo}}{{age}}{{/ageInfo}} years old!
          {{#-last}}
          That is all for now!
          {{/-last}}
          {{/people}}
          """)
  public record HelloWorld(String message, List<Person> people) implements AgeLambdaSupport {}

}
