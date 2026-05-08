package iteration1.api;

import api.models.admin.CreateUserRequest;
import api.models.admin.CreateUserResponse;
import api.models.customer.GetUserResponse;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import baseTests.BaseTest;
import common.annotations.UserApiSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;

public class DeleteUserTest extends BaseTest {
  @Test
  public void adminCanDeleteUserTest() {
    CreateUserRequest createUserRequest = AdminSteps.createUser();
    GetUserResponse getUserResponse = new UserSteps(createUserRequest).getUserResponse();

    new CrudRequester(
        RequestSpecs.adminSpec(), Endpoint.ADMIN_DELETE_USER, ResponseSpecs.requestReturnsOK())
        .delete(getUserResponse.getId())
        .assertThat()
        .body(equalTo("User with ID " + getUserResponse.getId() + " deleted successfully."));

    List<CreateUserResponse> users = AdminSteps.getAllUsers().stream()
        .filter(user -> user.getId() == (getUserResponse.getId()))
        .toList();
    softly.assertThat(users).hasSize(0);
  }

  @Test
  public void adminCannotDeleteNotExistsUserTest() {
    Long maxUserID = AdminSteps.getAllUsers().stream()
        .map(CreateUserResponse::getId)
        .max(Long::compare).orElse(0L);
    Long notExistsUserID = maxUserID + 100;
    new CrudRequester(
        RequestSpecs.adminSpec(), Endpoint.ADMIN_DELETE_USER, ResponseSpecs.requestReturnsNotFound("Error: User with ID " + notExistsUserID + " not found."))
        .delete(notExistsUserID);
  }

  @Test
  @UserApiSession
  public void userCannotDeleteUserTest() {
    CreateUserRequest createUserRequest = AdminSteps.createUser();
    GetUserResponse getUserResponse = new UserSteps(createUserRequest).getUserResponse();

    new CrudRequester(
        RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
        Endpoint.ADMIN_DELETE_USER, ResponseSpecs.requestReturnsForbidden("Forbidden"))
        .delete(getUserResponse.getId());

    List<CreateUserResponse> users = AdminSteps.getAllUsers().stream()
        .filter(user -> user.getId() == (getUserResponse.getId()))
        .toList();
    softly.assertThat(users).hasSize(1);
    AdminSteps.deleteUserById(getUserResponse.getId());
  }

  @Test
  public void unauthorizedAdminCanNotDeleteUserTest() {
    CreateUserRequest createUserRequest = AdminSteps.createUser();
    GetUserResponse getUserResponse = new UserSteps(createUserRequest).getUserResponse();

    new CrudRequester(
        RequestSpecs.unauthSpec(),
        Endpoint.ADMIN_DELETE_USER, ResponseSpecs.requestReturnsUnauthorized())
        .delete(getUserResponse.getId());

    List<CreateUserResponse> users = AdminSteps.getAllUsers().stream()
        .filter(user -> user.getId() == (getUserResponse.getId()))
        .toList();
    softly.assertThat(users).hasSize(1);
    AdminSteps.deleteUserById(getUserResponse.getId());
  }
}
