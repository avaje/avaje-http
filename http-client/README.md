# avaje-http-client

A light weight wrapper to the JDK 11+ Java Http Client

- Adds a fluid API for request constructing URL and payload
- Adds JSON marshalling/unmarshalling of request and response using Jackson or Gson
- Adds request/response logging



### Dependency

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-http-client</artifactId>
  <version>1.5</version>
</dependency>
```

### Create HttpClientContext

Create a HttpClientContext with a baseUrl, Jackson or Gson based JSON
 body adapter, logger.

```java
  public HttpClientContext client() {
    return HttpClientContext.newBuilder()
      .withBaseUrl(baseUrl)
      .withRequestListener(new RequestLogger())
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
 - Http verbs - GET(), POST(), PUT(), PATCH(), DELETE(), HEAD(), TRACE()
 - Optionally return response body as a bean, list of beans, or raw

## Examples

GET as String
```java
final HttpResponse<String> hres = clientContext.request()
  .path("hello")
  .GET()
  .asString();
```

GET as json to single bean
```java
final HelloDto bean = clientContext.request()
  .path("hello/there")
  .GET()
  .bean(HelloDto.class);
```

POST a bean as json request body
```java
HelloDto bean = new HelloDto(12, "rob", "other");

final HttpResponse<Void> res = clientContext.request()
  .path("hello/savebean")
  .body(bean)
  .POST()
  .asDiscarding();

assertThat(res.statusCode()).isEqualTo(201);
```

GET as json to list of beans
```java
final List<HelloDto> beans = clientContext.request()
  .path("hello")
  .GET()
  .list(HelloDto.class);
```

Path
```java
final HttpResponse<String> res = clientContext.request()
  .path("hello")
  .path("withMatrix")
  .path("2011")
  .GET()
  .asString();

// is the same as ...

final HttpResponse<String> res = clientContext.request()
  .path("hello/withMatrix/2011")
  .GET()
  .asString();
```

MatrixParam
```java
final HttpResponse<String> httpRes = clientContext.request()
  .path("hello")
  .matrixParam("author", "rob")
  .matrixParam("country", "nz")
  .path("foo")
  .matrixParam("extra", "banana")
  .GET().asString();
```

QueryParam
```java
final List<HelloDto> beans = clientContext.request()
  .path("hello")
  .queryParam("sortBy", "name")
  .queryParam("maxCount", "100")
  .GET().list(HelloDto.class);
```

FormParam
```java
final HttpResponse<Void> res = clientContext.request()
  .path("hello/saveform")
  .formParam("name", "Bazz")
  .formParam("email", "user@foo.com")
  .formParam("url", "http://foo.com")
  .formParam("startDate", "2020-12-03")
  .POST()
  .asDiscarding();

assertThat(res.statusCode()).isEqualTo(201);
```

## Currently, NO support for POSTing multipart-form

## Auth token

Built in support for obtaining and setting an Authorization token.

### 1. Implement AuthTokenProvider

```java

  class MyAuthTokenProvider implements AuthTokenProvider {

    @Override
    public AuthToken obtainToken(HttpClientRequest tokenRequest) {
      AuthTokenResponse res = tokenRequest
        .url("https://foo/v2/token")
        .header("content-type", "application/json")
        .body(authRequestAsJson())
        .POST()
        .bean(AuthTokenResponse.class);

      Instant validUntil = Instant.now().plusSeconds(res.expires_in).minusSeconds(60);

      return AuthToken.of(res.access_token, validUntil);
    }
  }
```

### 2. Register with HttpClientContext

```java
    HttpClientContext ctx = HttpClientContext.newBuilder()
      .withBaseUrl("https://foo")
      .withBodyAdapter(new JacksonBodyAdapter(objectMapper))
      .withRequestListener(new RequestLogger())
      .withAuthTokenProvider(new MyAuthTokenProvider()) <!-- HERE
      .build();
```

### 3. Token obtained and set automatically

Now all requests using the HttpClientContext will automatically get
an `Authorization` header with `Bearer` token added. The token will be
obtained when necessary.
