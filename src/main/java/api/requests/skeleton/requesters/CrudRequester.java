package api.requests.skeleton.requesters;

import api.models.BaseModel;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.HttpRequest;
import api.requests.skeleton.interfaces.CrudEndpointInterface;
import api.requests.skeleton.interfaces.GetAllEndpointInterface;
import common.helpers.StepLogger;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class CrudRequester extends HttpRequest
    implements CrudEndpointInterface, GetAllEndpointInterface {
  public CrudRequester(
      RequestSpecification requestSpecification,
      Endpoint endpoint,
      ResponseSpecification responseSpecification) {
    super(requestSpecification, endpoint, responseSpecification);
  }

  @Override
  public ValidatableResponse post(BaseModel model) {
    return StepLogger.log("POST request to " + endpoint.getUrl(), () -> given()
        .spec(requestSpecification)
        .body(model)
        .post(endpoint.getUrl())
        .then()
        .assertThat()
        .spec(responseSpecification));
  }

  public ValidatableResponse post() {
    return StepLogger.log("POST request to " + endpoint.getUrl(), () -> given()
        .spec(requestSpecification)
        .post(endpoint.getUrl())
        .then()
        .assertThat()
        .spec(responseSpecification));
  }

  @Override
  public ValidatableResponse get() {
    return StepLogger.log("GET request to " + endpoint.getUrl(), () -> given()
        .spec(requestSpecification)
        .get(endpoint.getUrl())
        .then()
        .assertThat()
        .spec(responseSpecification));
  }

  public ValidatableResponse getWithPathParam(Map<String, ?> baseModel) {
    return StepLogger.log("GET request to " + endpoint.getUrl(), () -> given()
        .spec(requestSpecification)
        .pathParams(baseModel)
        .get(endpoint.getUrl())
        .then()
        .assertThat()
        .spec(responseSpecification));
  }

  @Override
  public ValidatableResponse update(BaseModel model) {
    return StepLogger.log("PUT request to " + endpoint.getUrl(), () -> given()
        .spec(requestSpecification)
        .body(model)
        .put(endpoint.getUrl())
        .then()
        .assertThat()
        .spec(responseSpecification));
  }

  @Override
  public ValidatableResponse delete(long id) {
    return StepLogger.log("DELETE request to " + endpoint.getUrl() + " with id = " + id, () -> given()
        .spec(requestSpecification)
        .pathParams("id", id)
        .delete(endpoint.getUrl())
        .then()
        .assertThat()
        .spec(responseSpecification));
  }

  @Override
  public ValidatableResponse getAll(Class<?> clazz) {
    return StepLogger.log("GET request to " + endpoint.getUrl(), () -> given()
        .spec(requestSpecification)
        .get(endpoint.getUrl())
        .then()
        .assertThat()
        .spec(responseSpecification));
  }
}
