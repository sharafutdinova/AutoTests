package api.requests.steps;

import static org.hamcrest.Matchers.equalTo;

import api.generators.RandomData;
import api.models.UserRole;
import api.models.admin.CreateUserRequest;
import api.models.admin.CreateUserResponse;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import java.util.List;

public class AdminSteps {
  public static CreateUserRequest createUser() {
    CreateUserRequest userRequest =
        CreateUserRequest.builder()
            .username(RandomData.getUsername())
            .password(RandomData.getPassword())
            .role(UserRole.USER.toString())
            .build();

    new CrudRequester(
            RequestSpecs.adminSpec(), Endpoint.ADMIN_USER, ResponseSpecs.entityWasCreated())
        .post(userRequest);
    return userRequest;
  }

  public static void deleteUserById(long id) {
    new CrudRequester(
            RequestSpecs.adminSpec(), Endpoint.ADMIN_DELETE_USER, ResponseSpecs.requestReturnsOK())
        .delete(id)
        .assertThat()
        .body(equalTo("User with ID " + id + " deleted successfully."));
  }

  public static void deleteUserByCreateUserRequest(CreateUserRequest createUserRequest) {
    long id = new UserSteps(createUserRequest).getUserResponse().getId();
    deleteUserById(id);
  }

  public static List<CreateUserResponse> getAllUsers() {
    return new ValidatedCrudRequester<CreateUserResponse>(
            RequestSpecs.adminSpec(), Endpoint.ADMIN_USERS, ResponseSpecs.requestReturnsOK())
        .getAll(CreateUserResponse[].class);
  }
}
