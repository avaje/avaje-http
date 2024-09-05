package io.avaje.http.lambda;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.avaje.http.lambda.Routing.HttpMethod;
import io.avaje.http.lambda.events.ALBRequestEvent;
import io.avaje.http.lambda.json.JacksonService;
import io.avaje.http.lambda.json.JsonbService;

class SigmaTest {

  private Sigma sigma;
  private ALBRequestEvent albExample;

  @BeforeEach
  void setUpBeforeClass() throws JsonMappingException, JsonProcessingException {

    this.sigma = Sigma.create().jsonService(new JacksonService());
    String exampleEvent =
        """
                {
          "requestContext": {
              "elb": {
                  "targetGroupArn": "arn:aws:elasticloadbalancing:us-east-1:123456789012:targetgroup/lambda-279XGJDqGZ5rsrHC2Fjr/49e9d65c45c6791a"
              }
          },
          "httpMethod": "GET",
          "path": "/lambda/1234",
          "queryStringParameters": {
              "query": "1234ABCD"
          },
          "headers": {
              "accept": "text/html",
              "accept-encoding": "gzip",
              "accept-language": "en-US",
              "connection": "keep-alive",
              "host": "lambda-alb-123578498.us-east-1.elb.amazonaws.com",
              "upgrade-insecure-requests": "1",
              "x-amzn-trace-id": "Root=1-5c536348-3d683b8b04734faae651f476",
              "x-forwarded-for": "72.12.164.125",
              "x-forwarded-port": "80",
              "x-forwarded-proto": "http",
              "x-imforwards": "20"
          },
          "body": "",
          "isBase64Encoded": false
      }
                """;
    this.albExample = new ObjectMapper().readValue(exampleEvent, ALBRequestEvent.class);
  }

  @Test
  void test() {
    sigma.routing(
        r ->
            r.get(
                "/lambda/{pathParam}",
                ctx -> {
                  ALBRequestEvent event = ctx.awsRequest();
                  ctx.pathParam("pathParam");
                  ctx.queryParam("query");

                  ctx.text("hello world");
                }));

    sigma.createAWSHandler().handle(albExample, null);
  }
}
