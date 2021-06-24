# avaje-http-client

A lightweight wrapper to the [JDK 11+ Java Http Client](http://openjdk.java.net/groups/net/httpclient/intro.html)

- Use Java 11.0.8 or higher (some SSL related bugs prior to 11.0.8 with JDK HttpClient)
- Adds a fluid API for building URL and payload
- Adds JSON marshalling/unmarshalling of request and response using Jackson or Gson
- Gzip encoding/decoding
- Logging of request/response logging
- Interception of request/response
- Built in support for authorization via Basic Auth and Bearer Token
- Provides async and sync API


### Dependency

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-http-client</artifactId>
  <version>1.9</version>
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
- Retry (when specified) does not apply to `async` response processing`


## JDK HttpClient

- Introduction to JDK HttpClient at
[JDK HttpClient Introduction](http://openjdk.java.net/groups/net/httpclient/intro.html)

- Javadoc for JDK
  [HttpClient](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpClient.html)


#### Example GET as String
```java
HttpResponse<String> hres = clientContext.request()
  .path("hello")
  .GET()
  .asString();
```
#### Example Async GET as String
- All async requests use CompletableFuture&lt;T&gt;
- throwable is a CompletionException
- In the example below hres is of type HttpResponse&lt;String&gt;

```java
clientContext.request()
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
<tr><td>bean&lt;E&gt</td><td>E</td></tr>
<tr><td>list&lt;E&gt</td><td>List&lt;E&gt;</td></tr>
<tr><td>stream&lt;E&gt</td><td>Stream&lt;E&gt;</td></tr>
<tr><td>withHandler(HttpResponse.BodyHandler&lt;E&gt;)</td><td>HttpResponse&lt;E&gt;</td></tr>
<tr><td>&nbsp;</td><td>&nbsp;</td></tr>
<tr><td><b>async processing</b></td><td>&nbsp;</td></tr>
<tr><td>asVoid</td><td>CompletableFuture&lt;HttpResponse&lt;Void&gt;&gt;</td></tr>
<tr><td>asString</td><td>CompletableFuture&lt;HttpResponse&lt;String&gt;&gt;</td></tr>
<tr><td>bean&lt;E&gt</td><td>CompletableFuture&lt;E&gt;</td></tr>
<tr><td>list&lt;E&gt</td><td>CompletableFuture&lt;List&lt;E&gt;&gt;</td></tr>
<tr><td>stream&lt;E&gt</td><td>CompletableFuture&lt;Stream&lt;E&gt;&gt;</td></tr>
<tr><td>withHandler(HttpResponse.BodyHandler&lt;E&gt)</td><td>CompletableFuture&lt;HttpResponse&lt;E&gt;&gt;</td></tr>
</table>

### HttpResponse BodyHandlers

JDK HttpClient provides a number of [BodyHandlers](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpResponse.BodyHandler.html)
including reactive Flow based subscribers. With the `withHandler()` method we can use any of these or our own [`HttpResponse.BodyHandler`](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpResponse.BodyHandler.html)
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
       // HttpResponse<String>
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

All async requests use JDK httpClient.sendAsync(...) returning CompletableFuture. Commonly the
`whenComplete()` callback will be used to process the async responses.

The `bean()`, `list()` and `stream()` responses throw a `HttpException` if the status code >= 300
(noting that by default redirects are followed apart for HTTPS to HTTP).

<table style="width:100%;">
<tr><td><b>async processing</b></td><td>&nbsp;</td></tr>
<tr><td>asVoid</td><td>CompletableFuture&lt;HttpResponse&lt;Void&gt;&gt;</td></tr>
<tr><td>asString</td><td>CompletableFuture&lt;HttpResponse&lt;String&gt;&gt;</td></tr>
<tr><td>bean&lt;E&gt</td><td>CompletableFuture&lt;E&gt;</td></tr>
<tr><td>list&lt;E&gt</td><td>CompletableFuture&lt;List&lt;E&gt;&gt;</td></tr>
<tr><td>stream&lt;E&gt</td><td>CompletableFuture&lt;Stream&lt;E&gt;&gt;</td></tr>
<tr><td>withHandler(HttpResponse.BodyHandler&lt;E&gt)</td><td>CompletableFuture&lt;HttpResponse&lt;E&gt;&gt;</td></tr>
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

## HttpCall

If we are creating an API and want the client code to *choose* to execute
the request asynchronously or synchronously then we can use `call()`.

The client can then choose to `execute()` the request synchronously or
choose `async()` to execute the request asynchronously.

```java
HttpCall<List<Customer>> call =
  clientContext.request()
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


# 10K requests - Loom vs Async

The following is a very quick and rough comparison of running 10,000 requests
using `Async` vs `Loom`.

The intention is to test the thought that in a "future Loom world" the
desire to use `async()` execution with HttpClient reduces.

TLDR: Caveat, caveat, more caveats ... initial testing shows Loom to be just a
touch faster (~10%) than async.

To run my tests I use [Jex](https://github.com/avaje/avaje-jex) as the server
(Jetty based) and have it running using Loom. For whatever testing you do
you will need a server that can handle a very large number of concurrent requests.

The Loom blocking request (make 10K of these)

```java
HttpResponse<String> hres =  httpClient.request()
  .path("s200")
  .GET()
  .asString();
```
The equivalent async request (make 10K of these joining the CompletableFuture's).

```java
CompletableFuture<HttpResponse<String>> future = httpClient.request()
  .path("s200")
  .GET()
  .async()
  .asString()
  .whenComplete((hres, throwable) -> {
    ...
  });
```


### 10K requests using Async and reactive streams

Use `.async()` to execute the requests which internally is using JDK
HttpClient's reactive streams. The `whenComplete()` callback is invoked
when the response is ready. Collect all the resulting CompletableFuture
and wait for them all to complete.

Outline:

```java

// Collect all the CompletableFuture's
List<CompletableFuture<HttpResponse<String>>> futures = new ArrayList<>();

for (int i = 0; i < 10_000; i++) {
  futures.add(httpClient.request().path("s200")
    .GET()
    .async().asString()
    .whenComplete((hres, throwable) -> {
        // confirm 200 response etc
        ...
    }));
}

// wait for all requests to complete via join() ...
CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

```

### 10K requests using Loom

With Loom Java 17 EA Release we can use `Executors.newVirtualThreadExecutor()`
to return an ExecutorService that uses Loom Virtual Threads. These are backed
by "Carrier threads" (via ForkedJoinPool).

Outline:

```java

// use Loom's Executors.newVirtualThreadExecutor()

try (ExecutorService executorService = Executors.newVirtualThreadExecutor()) {
    for (int i = 0; i < 10_000; i++) {
        executorService.submit(this::task);
    }
}

```
```java
private void task() {
  HttpResponse<String> hres =
    httpClient.request().path("s200")
     .GET()
     .asString();

  // confirm 200 response etc
  ...
}

```

Caveat: Proper performance benchmarks are really hard and take a lot of
effort.

Running some "rough/approx performance comparison tests" using `Loom`
build `17 EA 2021-09-14 / (build 17-loom+7-342)` vs `Async` for my environment
and 10K request scenarios has loom execution around 10% faster than async.

It looks like Loom and Async run in pretty much the same time although it
currently looks that Loom is just a touch faster (perhaps due to how it does
park/unpark). More investigation required.


Date: 2021-06
Build: `17 EA 2021-09-14 / (build 17-loom+7-342)`.

```
openjdk version "17-loom" 2021-09-14
OpenJDK Runtime Environment (build 17-loom+7-342)
OpenJDK 64-Bit Server VM (build 17-loom+7-342, mixed mode, sharing)
```
