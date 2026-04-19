package requests.steps;

import generators.RandomData;
import models.UserRole;
import models.admin.CreateUserRequest;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static org.hamcrest.Matchers.equalTo;

public class AdminSteps {
    public static CreateUserRequest createUser() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.entityWasCreated())
                .post(userRequest);
        return userRequest;
    }

    public static void deleteUserById(long id) {
        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_DELETE_USER,
                ResponseSpecs.requestReturnsOK())
                .delete(id)
                .assertThat()
                .body(equalTo("User with ID " + id + " deleted successfully."));
    }

    public static void deleteUserByCreateUserRequest(CreateUserRequest createUserRequest) {
        long id = UserSteps.getUserResponse(createUserRequest).getId();
        deleteUserById(id);
    }
}
