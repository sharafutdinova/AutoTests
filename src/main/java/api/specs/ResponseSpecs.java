package api.specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;

import java.util.List;

public class ResponseSpecs {
  private ResponseSpecs() {
  }

  private static ResponseSpecBuilder defaultResponseBuilder() {
    return new ResponseSpecBuilder();
  }

  public static ResponseSpecification entityWasCreated() {
    return defaultResponseBuilder().expectStatusCode(HttpStatus.SC_CREATED).build();
  }

  public static ResponseSpecification requestReturnsOK() {
    return defaultResponseBuilder().expectStatusCode(HttpStatus.SC_OK).build();
  }

  public static ResponseSpecification requestReturnsBadRequest(
      String errorKey, List<String> errorValues) {
    return defaultResponseBuilder()
        .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
        .expectBody(errorKey, Matchers.containsInAnyOrder(errorValues.toArray()))
        .build();
  }

  public static ResponseSpecification requestReturnsBadRequest(String errorValue) {
    return defaultResponseBuilder()
        .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
        .expectBody(Matchers.equalTo(errorValue))
        .build();
  }

  public static ResponseSpecification requestReturnsForbidden(String errorValue) {
    return defaultResponseBuilder()
        .expectStatusCode(HttpStatus.SC_FORBIDDEN)
        .expectBody(Matchers.containsString(errorValue))
        .build();
  }

  public static ResponseSpecification requestReturnsForbidden() {
    return defaultResponseBuilder()
        .expectStatusCode(HttpStatus.SC_FORBIDDEN)
        .build();
  }

  public static ResponseSpecification requestReturnsUnauthorized() {
    return defaultResponseBuilder()
        .expectStatusCode(HttpStatus.SC_UNAUTHORIZED)
        .build();
  }
  public static ResponseSpecification requestReturnsNotFound(String errorValue) {
    return defaultResponseBuilder()
        .expectStatusCode(HttpStatus.SC_NOT_FOUND)
        .expectBody(Matchers.equalTo(errorValue))
        .build();
  }
}
