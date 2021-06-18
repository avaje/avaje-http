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
  <version>1.6</version>
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
HttpResponse<String> hres = clientContext.request()
  .path("hello")
  .GET()
  .asString();
```

GET as json to single bean
```java
Customer customer = clientContext.request()
  .path("customers").path(42)
  .GET()
  .bean(Customer.class);
```

GET as json to a list of beans
```java
List<Customer> list = clientContext.request()
  .path("customers")
  .GET()
  .list(Customer.class);
```

GET as `application/x-json-stream` as a stream of beans
```java
Stream<Customer> stream = clientContext.request()
  .path("customers/all")
  .GET()
  .stream(Customer.class);
```


POST a bean as json request body
```java
HelloDto bean = new HelloDto(12, "rob", "other");

HttpResponse<Void> res = clientContext.request()
  .path("hello/savebean")
  .body(bean)
  .POST()
  .asDiscarding();

assertThat(res.statusCode()).isEqualTo(201);
```


Path
```java
HttpResponse<String> res = clientContext.request()
  .path("customers")
  .path("42")
  .path("contacts")
  .GET()
  .asString();

// is the same as ...

HttpResponse<String> res = clientContext.request()
  .path("customers/42/contacts")
  .GET()
  .asString();
```

MatrixParam
```java
HttpResponse<String> httpRes = clientContext.request()
  .path("books")
  .matrixParam("author", "rob")
  .matrixParam("country", "nz")
  .path("foo")
  .matrixParam("extra", "banana")
  .GET().asString();
```

QueryParam
```java
List<Product> beans = clientContext.request()
  .path("products")
  .queryParam("sortBy", "name")
  .queryParam("maxCount", "100")
  .GET().list(Product.class);
```

FormParam
```java
HttpResponse<Void> res = clientContext.request()
  .path("register/user")
  .formParam("name", "Bazz")
  .formParam("email", "user@foo.com")
  .formParam("url", "http://foo.com")
  .formParam("startDate", "2020-12-03")
  .POST()
  .asDiscarding();

assertThat(res.statusCode()).isEqualTo(201);
```

## Currently, NO support for POSTing multipart-form

## Async processing

### .async().asDiscarding() - HttpResponse<Void>

```java

clientContext.request()
   .path("hello/world")
   .GET()
   .async().asDiscarding()
   .whenComplete((hres, throwable) -> {

     if (throwable != null) {
       ...
     } else {
       int statusCode = hres.statusCode();
       ...
     }
   });

```

###  .async().asString() - HttpResponse<String>

```java
clientContext.request()
   .path("hello/world")
   .GET()
   .async().asString()
   .whenComplete((hres, throwable) -> {

     if (throwable != null) {
       ...
     } else {
       int statusCode = hres.statusCode();
       String body = hres.body();
       ...
     }
   });
```

### .async().bean(HelloDto.class)

```java
clientContext.request()
   ...
   .POST().async()
   .bean(HelloDto.class)
   .whenComplete((helloDto, throwable) -> {

     if (throwable != null) {
       HttpException httpException = (HttpException) throwable.getCause();
       int statusCode = httpException.getStatusCode();

       // maybe convert json error response body to a bean (using Jackson/Gson)
       MyErrorBean errorResponse = httpException.bean(MyErrorBean.class);
       ..

     } else {
       // use helloDto
       ...
     }
   });

```

### .async().withHandler(...) - Any response body handler

The example below is a line subscriber processing response content line by line.

```java
CompletableFuture<HttpResponse<Void>> future = clientContext.request()
   .path("hello/lineStream")
   .GET().async()
   .withHandler(HttpResponse.BodyHandlers.fromLineSubscriber(new Flow.Subscriber<>() {

     @Override
     public void onSubscribe(Flow.Subscription subscription) {
       subscription.request(Long.MAX_VALUE);
     }
     @Override
     public void onNext(String item) {
       ...
     }
     @Override
     public void onError(Throwable throwable) {
       ...
     }
     @Override
     public void onComplete() {
       ...
     }
   }))
   .whenComplete((hres, throwable) -> {
     int statusCode = hres.statusCode();
     ...
   });

```


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
