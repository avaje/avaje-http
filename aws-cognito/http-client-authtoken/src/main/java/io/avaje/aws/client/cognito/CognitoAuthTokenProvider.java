package io.avaje.aws.client.cognito;

import io.avaje.http.client.AuthTokenProvider;

/**
 * AuthTokenProvider for AWS Cognito providing Bearer access tokens.
 *
 * <pre>{@code
 *
 *     AuthTokenProvider authTokenProvider = CognitoAuthTokenProvider.builder()
 *       .url("https://foo.amazoncognito.com/oauth2/token")
 *       .clientId("<something>")
 *       .clientSecret("<something>")
 *       .scope("default/default")
 *       .build();
 *
 *     // specify the authTokenProvider on the HttpClient ...
 *
 *     HttpClient client = HttpClient.builder()
 *       .authTokenProvider(authTokenProvider)
 *       .baseUrl(myApplicationUrl)
 *       .build();
 *
 * }</pre>
 */
public interface CognitoAuthTokenProvider extends AuthTokenProvider {

  /**
   * Return a builder for the CognitoAuthTokenProvider.
   */
  static Builder builder() {
    return new AmzCognitoAuthTokenProvider();
  }

  /**
   * The builder for the AWS Cognito AuthTokenProvider.
   */
  interface Builder {

    /**
     * Set the url used to obtain access tokens.
     */
    Builder url(String url);

    /**
     * Set the clientId.
     */
    Builder clientId(String clientId);

    /**
     * Set the clientSecret.
     */
    Builder clientSecret(String clientSecret);

    /**
     * Set the scope.
     */
    Builder scope(String scope);

    /**
     * Build and return the AuthTokenProvider.
     */
    AuthTokenProvider build();
  }
}
