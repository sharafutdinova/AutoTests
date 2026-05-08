package iteration1.api;

import api.models.admin.CreateUserRequest;
import api.models.comparison.ModelAssertions;
import api.models.customer.GetUserResponse;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import baseTests.BaseTest;
import common.annotations.UserApiSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;

public class CustomerProfileTest extends BaseTest {
  @Test
  @UserApiSession
  public void userCanGetCustomerProfileTest() {
    CreateUserRequest user = SessionStorage.getUser();
    GetUserResponse getUserResponse = new ValidatedCrudRequester<GetUserResponse>(
        RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
        Endpoint.GET_CUSTOMER_PROFILE,
        ResponseSpecs.requestReturnsOK())
        .get();

    ModelAssertions.assertThatModels(getUserResponse, user).match();
    softly.assertThat(getUserResponse.getName()).isNull();
    softly.assertThat(getUserResponse.getAccounts()).isEmpty();
  }

  @Test
  public void adminCannotGetCustomerProfileTest() {
    new CrudRequester(
        RequestSpecs.adminSpec(),
        Endpoint.GET_CUSTOMER_PROFILE,
        ResponseSpecs.requestReturnsForbidden())
        .get();
  }

  @Test
  public void unauthorizedUserCannotGetCustomerProfileTest() {
    new CrudRequester(
        RequestSpecs.unauthSpec(),
        Endpoint.GET_CUSTOMER_PROFILE,
        ResponseSpecs.requestReturnsUnauthorized())
        .get();
  }
}
