# avaje-http-client


### Dependency

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-http-client</artifactId>
  <version>1.0</version>
</dependency>
```

### Create HttpClientContext

Create a HttpClientContext with a baseUrl, Jackson or Gson based JSON
 body adapter, logger.

```java
  public HttpClientContext client() {
    return HttpClientContext.newBuilder()
      .withBaseUrl(baseUrl)
      .withResponseListener(new RequestLogger())
      .withBodyAdapter(new JacksonBodyAdapter(new ObjectMapper()))
//      .withBodyAdapter(new GsonBodyAdapter(new Gson()))
      .build();
  }

```

### Requests

From HttpClientContext:
 - Create a request
 - Build the url via path(), matrixParam(), queryParam()
 - Optionally set headers(), cookies() etc
 - Optionally specify a request body (JSON, form, or raw BodyPublisher)
 - Http verbs - get(), post(), put(), delete()
 - Optionally return response body as a bean, list of beans, or raw

## Examples

GET as String
```java
    final HttpResponse<String> hres = clientContext.request()
      .path("hello")
      .get().asString();

```

GET as json to single bean
```java
final HelloDto bean = clientContext.request()
  .path("hello/there")
  .get().bean(HelloDto.class);
```

POST a bean as json request body
```java
HelloDto bean = new HelloDto(12, "rob", "other");

final HttpResponse<Void> res = clientContext.request()
  .path("hello/savebean")
  .body(bean).post()
  .asDiscarding();

assertThat(res.statusCode()).isEqualTo(201);

```

GET as json to list of beans
```java
final List<HelloDto> beans = clientContext.request()
  .path("hello")
  .get().list(HelloDto.class);
```

Path
```java
final HttpResponse<String> res = clientContext.request()
  .path("hello")
  .path("withMatrix")
  .path("2011")
  .get().asString();

// is the same as ...

final HttpResponse<String> res = clientContext.request()
  .path("hello/withMatrix/2011")
  .get().asString();
```

MatrixParam
```java
final HttpResponse<String> httpRes = clientContext.request()
  .path("hello")
  .matrixParam("author", "rob")
  .matrixParam("country", "nz")
  .path("foo")
  .matrixParam("extra", "banana")
  .get().asString();
```

QueryParam
```java
final List<HelloDto> beans = clientContext.request()
  .path("hello")
  .queryParam("sortBy", "name")
  .queryParam("maxCount", "100")
  .get().list(HelloDto.class);
```

FormParam
```java
final HttpResponse<Void> res = clientContext.request()
  .path("hello/saveform")
  .formParam("name", "Bazz")
  .formParam("email", "user@foo.com")
  .formParam("url", "http://foo.com")
  .formParam("startDate", "2020-12-03")
  .post()
  .asDiscarding();

assertThat(res.statusCode()).isEqualTo(201);
```

## Currently NO support for POSTing multipart-form
