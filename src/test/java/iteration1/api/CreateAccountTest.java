package iteration1.api;

import api.models.accounts.CreateAccountResponse;
import api.models.admin.CreateUserRequest;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import baseTests.BaseTest;
import common.annotations.UserApiSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CreateAccountTest extends BaseTest {
  @Test
  @UserApiSession
  public void userCanCreateAccountTest() {
    CreateUserRequest user = SessionStorage.getUser();
    CreateAccountResponse createAccountResponse =
        new ValidatedCrudRequester<CreateAccountResponse>(
            RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
            Endpoint.ACCOUNTS,
            ResponseSpecs.entityWasCreated())
            .post();

    List<CreateAccountResponse> accounts = SessionStorage.getSteps().getCustomerAccounts();
    softly.assertThat(accounts.contains(createAccountResponse)).isTrue();
  }

  @Test
  @UserApiSession
  public void unauthorizedUserCanNotCreateAccountTest() {
    new CrudRequester(
        RequestSpecs.unauthSpec(),
        Endpoint.ACCOUNTS,
        ResponseSpecs.requestReturnsUnauthorized())
        .post();

    List<CreateAccountResponse> accounts = SessionStorage.getSteps().getCustomerAccounts();
    softly.assertThat(accounts).isEmpty();
  }

  @Test
  public void adminCanNotCreateAccountTest() {
    new CrudRequester(
        RequestSpecs.adminSpec(),
        Endpoint.ACCOUNTS,
        ResponseSpecs.requestReturnsForbidden("Forbidden"))
        .post();
  }
}
