package io.avaje.aws.client.cognito;

import io.avaje.http.client.AuthTokenProvider;
import io.avaje.http.client.HttpClientRequest;
import io.avaje.http.client.HttpClientResponse;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class CognitoAuthTokenProviderTest {

  @Test
  void obtainToken() {
    final String url = "https://<something>.amazoncognito.com/oauth2/token";
    final String clientId = "<something>";
    final String clientSecret = "<something>";

    AuthTokenProvider authTokenProvider = CognitoAuthTokenProvider.builder()
      .url(url)
      .clientId(clientId)
      .clientSecret(clientSecret)
      .scope("default/default")
      .build();

    HttpResponse<String> httpResponse = mock(HttpResponse.class);
    when(httpResponse.statusCode()).thenReturn(200);
    when(httpResponse.body()).thenReturn("{\"access_token\":\"1234\",\"expires_in\":3600}");

    HttpClientResponse clientResponse = mock(HttpClientResponse.class);
    when(clientResponse.asString()).thenReturn(httpResponse);

    HttpClientRequest httpClientRequest = mock(HttpClientRequest.class);
    when(httpClientRequest.url(anyString())).thenReturn(httpClientRequest);

    when(httpClientRequest.header(anyString(), anyString())).thenReturn(httpClientRequest);
    when(httpClientRequest.formParam(anyString(), anyString())).thenReturn(httpClientRequest);
    when(httpClientRequest.POST()).thenReturn(clientResponse);

    // act
    authTokenProvider.obtainToken(httpClientRequest);

    // verify the Authorization header has been obtained and set
    verify(httpClientRequest).header("Authorization", "Basic PHNvbWV0aGluZz46PHNvbWV0aGluZz4=");
  }
}
