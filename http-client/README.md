[![Build](https://github.com/avaje/avaje-http/actions/workflows/build.yml/badge.svg)](https://github.com/avaje/avaje-http-client/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.avaje/avaje-http-client.svg?label=Maven%20Central)](https://mvnrepository.com/artifact/io.avaje/avaje-http-client)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/avaje/avaje-http-client/blob/master/LICENSE)

# [Avaje-HTTP-Client](https://avaje.io/http-client/)

A lightweight wrapper to the [JDK 11+ Java Http Client](http://openjdk.java.net/groups/net/httpclient/intro.html)

- Requires Java 11+
- Adds a fluid API for building URLs and payloads
- Adds JSON marshalling/unmarshalling of request/response using avaje-jsonb, Jackson, Moshi, or Gson 
- Gzip encoding/decoding
- Logging of request/response logging
- Interception of request/response
- Built in support for authorization via Basic Auth and Bearer Tokens
- Provides async and sync API

### Dependency

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-http-client</artifactId>
  <version>${avaje.client.version}</version>
</dependency>
```

### Create HttpClient

Create a HttpClient with a baseUrl, Jackson or Gson based JSON
 body adapter, logger.

```java
HttpClient client = HttpClient.builder()
  .baseUrl(baseUrl)
  .bodyAdapter(new JsonbBodyAdapter())
  //.bodyAdapter(new JacksonBodyAdapter(new ObjectMapper()))
  //.bodyAdapter(new GsonBodyAdapter(new Gson()))
  .build();

HttpResponse<String> hres = client.request()
  .path("hello")
  .GET()
  .asString();
```

## Requests

From HttpClient:
 - Create a request
 - Build the url via path(), matrixParam(), queryParam()
 - Optionally set headers(), cookies() etc
 - Optionally specify a request body (JSON, form, or any JDK BodyPublisher)
 - Http verbs - GET(), POST(), PUT(), PATCH(), DELETE(), HEAD(), TRACE()

 - Sync processing response body as:
   - a bean, list of beans, stream of beans, String, Void or any JDK Response.BodyHandler

 - Async processing of the request using CompletableFuture
   - a bean, list of beans, stream of beans, String, Void or any JDK Response.BodyHandler

## JDK HttpClient

- Introduction to JDK HttpClient at
[JDK HttpClient Introduction](http://openjdk.java.net/groups/net/httpclient/intro.html)

- Javadoc for JDK
  [HttpClient](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpClient.html)

#### Example GET as String
```java
HttpClient client = HttpClient.builder()
  .baseUrl(baseUrl)
  .build();

HttpResponse<String> hres = client.request()
  .path("hello")
  .GET()
  .asString();
```

#### Example GET as JSON marshalling into a java class/dto
```java
HttpResponse<CustomerDto> customer = client.request()
  .path("customers").path(42)
  .GET()
  .as(CustomerDto.class);

// just get the bean without HttpResponse
CustomerDto customer = client.request()
  .path("customers").path(42)
  .GET()
  .bean(CustomerDto.class);

// get a List
HttpResponse<List<CustomerDto>> customers = client.request()
  .path("customers")
  .queryParam("active", "true")
  .GET()
  .asList(CustomerDto.class);


// get a Stream - `application/x-json-stream`
HttpResponse<List<CustomerDto>> customers = client.request()
  .path("customers/stream")
  .GET()
  .asStream(CustomerDto.class);

```

#### Example Async GET as String
- All async requests use CompletableFuture&lt;T&gt;
- throwable is a CompletionException
- In the example below hres is of type HttpResponse&lt;String&gt;

```java
client.request()
   .path("hello")
   .GET()
   .async().asString()  // CompletableFuture<HttpResponse<String>>
   .whenComplete((hres, throwable) -> {

     if (throwable != null) {
       // CompletionException
       ...
     } else {
       // HttpResponse<String>
       int statusCode = hres.statusCode();
       String body = hres.body();
       ...
     }
   });
```

## Overview of responses

Overview of response types for sync calls.

<table style="width:100%;">
<tr><td><b>sync processing</b></td><td>&nbsp;</td></tr>
<tr><td>asVoid</td><td>HttpResponse&lt;Void&gt;</td></tr>
<tr><td>asString</td><td>HttpResponse&lt;String&gt;</td></tr>
<tr><td>as&lt;E&gt</td><td>HttpResponse&lt;E&gt;</td></tr>
<tr><td>asList&lt;E&gt</td><td>HttpResponse&lt;List&lt;E&gt;&gt;</td></tr>
<tr><td>asStream&lt;E&gt</td><td>HttpResponse&lt;Stream&lt;E&gt;&gt;</td></tr>
<tr><td>bean&lt;E&gt</td><td>E</td></tr>
<tr><td>list&lt;E&gt</td><td>List&lt;E&gt;</td></tr>
<tr><td>stream&lt;E&gt</td><td>Stream&lt;E&gt;</td></tr>
<tr><td>handler(HttpResponse.BodyHandler&lt;E&gt;)</td><td>HttpResponse&lt;E&gt;</td></tr>
<tr><td>&nbsp;</td><td>&nbsp;</td></tr>
<tr><td><b>async processing</b></td><td>&nbsp;</td></tr>
<tr><td>asVoid</td><td>CompletableFuture&lt;HttpResponse&lt;Void&gt;&gt;</td></tr>
<tr><td>asString</td><td>CompletableFuture&lt;HttpResponse&lt;String&gt;&gt;</td></tr>
<tr><td>as&lt;E&gt</td><td>CompletableFuture&lt;HttpResponse&lt;E&gt;&gt;</td></tr>
<tr><td>asList&lt;E&gt</td><td>CompletableFuture&lt;HttpResponse&lt;List&lt;E&gt;&gt;&gt;</td></tr>
<tr><td>asStream&lt;E&gt</td><td>CompletableFuture&lt;HttpResponse&lt;Stream&lt;E&gt;&gt;&gt;</td></tr>
<tr><td>bean&lt;E&gt</td><td>CompletableFuture&lt;E&gt;</td></tr>
<tr><td>list&lt;E&gt</td><td>CompletableFuture&lt;List&lt;E&gt;&gt;</td></tr>
<tr><td>stream&lt;E&gt</td><td>CompletableFuture&lt;Stream&lt;E&gt;&gt;</td></tr>
<tr><td>handler(HttpResponse.BodyHandler&lt;E&gt)</td><td>CompletableFuture&lt;HttpResponse&lt;E&gt;&gt;</td></tr>
</table>

### HttpResponse BodyHandlers

JDK HttpClient provides a number of [BodyHandlers](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpResponse.BodyHandler.html)
including reactive Flow-based subscribers. With the `handler()` method we can use any of these or our own [`HttpResponse.BodyHandler`](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpResponse.BodyHandler.html)
implementation.

Refer to [HttpResponse.BodyHandlers](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpResponse.BodyHandlers.html)

<table style="width:100%;">
<tr><td>discarding()</td><td>Discards the response body</td></tr>
<tr><td>ofByteArray()</td><td>byte[]</td></tr>
<tr><td>ofString()</td><td>String, additional charset option</td></tr>
<tr><td>ofLines()</td><td>Stream&lt;String&gt;</td></tr>
<tr><td>ofInputStream()</td><td>InputStream</td></tr>

<tr><td>ofFile(Path file)</td><td>Path with various options</td></tr>
<tr><td>ofByteArrayConsumer(...)</td><td>&nbsp;</td></tr>
<tr><td>fromSubscriber(...)</td><td>various options</td></tr>
<tr><td>fromLineSubscriber(...)</td><td>various options</td></tr>
</table>

## Overview of Request body

When sending body content we can use:
- Object which is written as JSON content by default
- byte[], String, Path (file), InputStream
- formParams() for url encoded form (`application/x-www-form-urlencoded`)
- Any [HttpRequest.BodyPublisher](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpRequest.BodyPublishers.html)

## Examples

#### GET as String
```java
HttpResponse<String> hres = client.request()
  .path("hello")
  .GET()
  .asString();
```

#### Async GET as String
 - All async requests use JDK httpClient.sendAsync(...) returning CompletableFuture&lt;T&gt;
 - throwable is a CompletionException
 - In the example below hres is of type HttpResponse&lt;String&gt;

```java
client.request()
   .path("hello")
   .GET()
   .async().asString()
   .whenComplete((hres, throwable) -> {

     if (throwable != null) {
       // CompletionException
       ...
     } else {
       // HttpResponse<String>
       int statusCode = hres.statusCode();
       String body = hres.body();
       ...
     }
   });
```

#### GET as json to single bean
```java
HttpResponse<Customer> customer = client.request()
  .path("customers").path(42)
  .GET()
  .as(Customer.class);

Customer customer = client.request()
  .path("customers").path(42)
  .GET()
  .bean(Customer.class);
```

#### GET as json to a list of beans
```java
HttpResponse<List<Customer>> list = client.request()
  .path("customers")
  .GET()
  .asList(Customer.class);

List<Customer> list = client.request()
  .path("customers")
  .GET()
  .list(Customer.class);
```

#### GET as `application/x-json-stream` as a stream of beans
```java
HttpResponse<Stream<Customer>> stream = client.request()
  .path("customers/all")
  .GET()
  .asStream(Customer.class);

Stream<Customer> stream = client.request()
  .path("customers/all")
  .GET()
  .stream(Customer.class);
```


#### POST a bean as json request body
```java
Hello bean = new Hello(42, "rob", "other");

HttpResponse<Void> res = client.request()
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
HttpResponse<String> res = client.request()
  .path("customers")
  .path("42")
  .path("contacts")
  .GET()
  .asString();

// is the same as ...

HttpResponse<String> res = client.request()
  .path("customers/42/contacts")
  .GET()
  .asString();
```

#### MatrixParam
```java
HttpResponse<String> httpRes = client.request()
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
List<Product> beans = client.request()
  .path("products")
  .queryParam("sortBy", "name")
  .queryParam("maxCount", "100")
  .GET()
  .list(Product.class);
```

#### FormParam
```java
HttpResponse<Void> res = client.request()
  .path("register/user")
  .formParam("name", "Bazz")
  .formParam("email", "user@foo.com")
  .formParam("url", "http://foo.com")
  .formParam("startDate", "2020-12-03")
  .POST()
  .asDiscarding();

assertThat(res.statusCode()).isEqualTo(201);
```

## Retry
To add Retry funtionality, use `.retryHandler(yourhandler)` on the builder to provide your retry handler. The `RetryHandler` interface provides two methods, one for status exceptions (e.g. you get a 4xx/5xx from the server) and another for exceptions thrown by the underlying client (e.g. server times out or client couldn't send request). Here is example implementation of `RetryHandler`.

```
public final class ExampleRetry implements RetryHandler {
  private static final int MAX_RETRIES = 2;
  @Override
  public boolean isRetry(int retryCount, HttpResponse<?> response) {

    final var code = response.statusCode();

    if (retryCount >= MAX_RETRIES || code <= 400) {

      return false;
    }

    return true;
  }

  @Override
  public boolean isExceptionRetry(int retryCount, HttpException response) {
    //unwrap the exception
    final var cause = response.getCause();
    if (retryCount >= MAX_RETRIES) {
      return false;
    }
    if (cause instanceof ConnectException) {
      return true;
    }

    return false;
  }
```

## Async processing

All async requests use JDK httpClient.sendAsync(...) returning CompletableFuture. Commonly the
`whenComplete()` callback is used to process the async responses.

The `bean()`, `list()` and `stream()` responses throw a `HttpException` if the status code >= 300
(noting that by default redirects are followed apart for HTTPS to HTTP).

<table style="width:100%;">
<tr><td><b>async processing</b></td><td>&nbsp;</td></tr>
<tr><td>asVoid</td><td>CompletableFuture&lt;HttpResponse&lt;Void&gt;&gt;</td></tr>
<tr><td>asString</td><td>CompletableFuture&lt;HttpResponse&lt;String&gt;&gt;</td></tr>
<tr><td>bean&lt;E&gt</td><td>CompletableFuture&lt;E&gt;</td></tr>
<tr><td>list&lt;E&gt</td><td>CompletableFuture&lt;List&lt;E&gt;&gt;</td></tr>
<tr><td>stream&lt;E&gt</td><td>CompletableFuture&lt;Stream&lt;E&gt;&gt;</td></tr>
<tr><td>handler(HttpResponse.BodyHandler&lt;E&gt)</td><td>CompletableFuture&lt;HttpResponse&lt;E&gt;&gt;</td></tr>
</table>

### .async().asDiscarding() - HttpResponse&lt;Void&gt;

```java

client.request()
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
client.request()
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
client.request()
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

### .async().handler(...) - Any `Response.BodyHandler` implementation

The example below is a line subscriber processing response content line by line.

```java
CompletableFuture<HttpResponse<Void>> future = client.request()
   .path("hello/lineStream")
   .GET().async()
   .handler(HttpResponse.BodyHandlers.fromLineSubscriber(new Flow.Subscriber<>() {

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

## HttpCall

If we are creating an API and want the client code to *choose* to execute
the request asynchronously or synchronously then we can use `call()`.

The client can then choose to `execute()` the request synchronously or
choose `async()` to execute the request asynchronously.

```java
HttpCall<List<Customer>> call =
  client.request()
    .path("customers")
    .GET()
    .call().list(Customer.class);

// Either execute synchronously
List<Customer> customers =  call.execute();

// Or execute asynchronously
call.async()
  .whenComplete((customers, throwable) -> {
    ...
  });
```

## BasicAuthIntercept - Authorization Basic / Basic Auth

We can use `BasicAuthIntercept` to intercept all requests by adding an `Authorization: Basic ...`
header ("Basic Auth").

##### Example

```java
HttpClient client =
   HttpClient.builder()
     .baseUrl(baseUrl)
     ...
     .requestIntercept(new BasicAuthIntercept("myUsername", "myPassword"))  <!-- HERE
     .build();
```


## AuthTokenProvider - Authorization Bearer token

For authorization using `Bearer` tokens that are obtained and expire, implement `AuthTokenProvider`
and register that when building the HttpClient.

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

### 2. Register with HttpClient

```java
    HttpClient client = HttpClient.builder()
      .baseUrl("https://foo")
      ...
      .authTokenProvider(new MyAuthTokenProvider()) <!-- HERE
      .build();
```

### 3. Token obtained and set automatically

All requests using the HttpClient will automatically get
an `Authorization` header with `Bearer` token added. The token will be
obtained for the initial request and then renewed when the token has expired.
