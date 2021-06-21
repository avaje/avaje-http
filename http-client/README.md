# avaje-http-client

A light weight wrapper to the JDK 11+ Java Http Client

- Adds a fluid API for request constructing URL and payload
- Adds JSON marshalling/unmarshalling of request and response using Jackson or Gson
- Gzip encoding/decoding
- Logging of request/response logging
- Interception of request/response
- Built in support for authorization via Basic Auth and Bearer Token




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

## Requests

From HttpClientContext:
 - Create a request
 - Build the url via path(), matrixParam(), queryParam()
 - Optionally set headers(), cookies() etc
 - Optionally specify a request body (JSON, form, or any JDK BodyPublisher)
 - Http verbs - GET(), POST(), PUT(), PATCH(), DELETE(), HEAD(), TRACE()

 - Sync processing response body as:
   - a bean, list of beans, stream of beans, String, Void or any JDK Response.BodyHandler

 - Async processing of the request using CompletableFuture
   - a bean, list of beans, stream of beans, String, Void or any JDK Response.BodyHandler



## Limitations:
- NO support for POSTing multipart-form currently


#### Example GET as String
```java
HttpResponse<String> hres = clientContext.request()
  .path("hello")
  .GET()
  .asString();
```


## Overview of responses

Overview of response types for sync calls.

<table style="width:100%;">
<tr><td><b>sync processing</b></td><td>&nbsp;</td></tr>
<tr><td>asVoid</td><td>HttpResponse&lt;Void&gt;</td></tr>
<tr><td>asString</td><td>HttpResponse&lt;String&gt;</td></tr>
<tr><td>bean&lt;E&gt</td><td>E</td></tr>
<tr><td>list&lt;E&gt</td><td>List&lt;E&gt;</td></tr>
<tr><td>stream&lt;E&gt</td><td>Stream&lt;E&gt;</td></tr>
<tr><td>withHandler(HttpResponse.BodyHandler&lt;E&gt;)</td><td>E</td></tr>
<tr><td>&nbsp;</td><td>&nbsp;</td></tr>
<tr><td><b>async processing</b></td><td>&nbsp;</td></tr>
<tr><td>asVoid</td><td>CompletableFuture&lt;Void&gt;</td></tr>
<tr><td>asString</td><td>CompletableFuture&lt;String&gt;</td></tr>
<tr><td>bean&lt;E&gt</td><td>CompletableFuture&lt;E&gt;</td></tr>
<tr><td>list&lt;E&gt</td><td>CompletableFuture&lt;List&lt;E&gt;&gt;</td></tr>
<tr><td>stream&lt;E&gt</td><td>CompletableFuture&lt;Stream&lt;E&gt;&gt;</td></tr>
<tr><td>withHandler(HttpResponse.BodyHandler&lt;E&gt)</td><td>CompletableFuture&lt;E&gt;</td></tr>
</table>

### JDK BodyHandlers

JDK HttpClient provides a number of BodyHandlers including reactive Flow based subscribers.
With the `withHandler()` methods we can use any of these or our own `HttpResponse.BodyHandler`
implementation.

Reference https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpResponse.BodyHandlers.html

<table style="width:100%;">
<tr><td>discarding()</td><td>Discards the response body</td></tr>
<tr><td>ofByteArray()</td><td>byte[]</td></tr>
<tr><td>ofString()</td><td>String, additional charset option</td></tr>
<tr><td>ofLines()</td><td>Stream&lt;String&gt;</td></tr>
<tr><td>ofInputStream()</td><td>InputStream</td></tr>

<tr><td>ofFile(Path file)</td><td>Path with various options</td></tr>
<tr><td>ofByteArrayConsumer(...)</td><td>&nbsp;</td></tr>
<tr><td>fromSubscriber</td><td>various options</td></tr>
<tr><td>fromLineSubscriber</td><td>various options</td></tr>
</table>

## Examples

#### GET as String
```java
HttpResponse<String> hres = clientContext.request()
  .path("hello")
  .GET()
  .asString();
```

#### Async GET as String
 - All async requests use JDK httpClient.sendAsync(...) returning CompletableFuture&lt;T&gt;
 - throwable is a CompletionException
 - In the example below hres is of type HttpResponse&lt;String&gt;

```java
clientContext.request()
   .path("hello")
   .GET()
   .async().asString()
   .whenComplete((hres, throwable) -> {

     if (throwable != null) {
       // CompletionException
       ...
     } else {
       // HttpResponse&lt;String&gt;
       int statusCode = hres.statusCode();
       String body = hres.body();
       ...
     }
   });
```

#### GET as json to single bean
```java
Customer customer = clientContext.request()
  .path("customers").path(42)
  .GET()
  .bean(Customer.class);
```

#### GET as json to a list of beans
```java
List<Customer> list = clientContext.request()
  .path("customers")
  .GET()
  .list(Customer.class);
```

#### GET as `application/x-json-stream` as a stream of beans
```java
Stream<Customer> stream = clientContext.request()
  .path("customers/all")
  .GET()
  .stream(Customer.class);
```


#### POST a bean as json request body
```java
Hello bean = new Hello(42, "rob", "other");

HttpResponse<Void> res = clientContext.request()
  .path("hello")
  .body(bean)
  .POST()
  .asDiscarding();

assertThat(res.statusCode()).isEqualTo(201);
```


#### Path

Multiple calls to `path()` append with a `/`. This is make it easier to build a path
programmatically and also build paths that include `matrixParam()`

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

#### MatrixParam
```java
HttpResponse<String> httpRes = clientContext.request()
  .path("books")
  .matrixParam("author", "rob")
  .matrixParam("country", "nz")
  .path("foo")
  .matrixParam("extra", "banana")
  .GET()
  .asString();
```

#### QueryParam
```java
List<Product> beans = clientContext.request()
  .path("products")
  .queryParam("sortBy", "name")
  .queryParam("maxCount", "100")
  .GET()
  .list(Product.class);
```

#### FormParam
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



## Async processing

All async requests use JDK httpClient.sendAsync(...) returning CompletableFuture&lt;T&gt;

<table style="width:100%;">
<tr><td>asVoid</td><td>CompletableFuture&lt;Void&gt;</td></tr>
<tr><td>asString</td><td>CompletableFuture&lt;String&gt;</td></tr>
<tr><td>bean&lt;E&gt</td><td>CompletableFuture&lt;E&gt;</td></tr>
<tr><td>list&lt;E&gt</td><td>CompletableFuture&lt;List&lt;E&gt;&gt;</td></tr>
<tr><td>stream&lt;E&gt</td><td>CompletableFuture&lt;Stream&lt;E&gt;&gt;</td></tr>
<tr><td>withHandler(HttpResponse.BodyHandler&lt;E&gt)</td><td>CompletableFuture&lt;E&gt</td></tr>
</table>

### .async().asDiscarding() - HttpResponse&lt;Void&gt;

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

###  .async().asString() - HttpResponse&lt;String&gt;

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

### .async().withHandler(...) - Any `Response.BodyHandler` implementation

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
       // process the line of response content
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

## BasicAuthIntercept - Authorization Basic / Basic Auth

We can use `BasicAuthIntercept` to intercept all requests adding a `Authorization: Basic ...`
header ("Basic Auth").

##### Example

```java
HttpClientContext clientContext =
   HttpClientContext.newBuilder()
     .withBaseUrl(baseUrl)
     ...
     .withRequestIntercept(new BasicAuthIntercept("myUsername", "myPassword"))  <!-- HERE
     .build();
```


## AuthTokenProvider - Authorization Bearer token

For Authorization using `Bearer` tokens that are obtained and expire, implement `AuthTokenProvider`
and register that when building the HttpClientContext.

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
      ...
      .withAuthTokenProvider(new MyAuthTokenProvider()) <!-- HERE
      .build();
```

### 3. Token obtained and set automatically

All requests using the HttpClientContext will automatically get
an `Authorization` header with `Bearer` token added. The token will be
obtained for initial request and then renewed when the token has expired.
