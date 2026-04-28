package iteration2;

import generators.RandomData;
import models.*;
import models.admin.CreateUserRequest;
import models.admin.CreateUserResponse;
import models.customer.ChangeNameRequest;
import models.customer.ChangeNameResponse;
import models.customer.GetUserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.admin.AdminCreateUserRequester;
import requests.customer.ChangeNameRequester;
import requests.customer.GetUserRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class ChangeUserNameTest extends BaseTest {
    /*
    User can change name to name contains two words
    User can change name to name contains two words with length 1
    User can change name to the same name
    User can change name to name contains two words with russian letters
     */
    @ParameterizedTest
    @ValueSource(strings = {"Alsu Sharaf", "A S", "Алсу Шараф"})
    public void userCanChangeNameTest(String name) {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                .name(name)
                .build();
        ChangeNameResponse changeNameResponse = new ChangeNameRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(changeNameRequest).extract().body().as(ChangeNameResponse.class);

        softly.assertThat(changeNameRequest.getName()).isEqualTo(changeNameResponse.getCustomer().getName());
        softly.assertThat(userRequest.getUsername()).isEqualTo(changeNameResponse.getCustomer().getUsername());
        softly.assertThat(Messages.PROFILE_UPDATED_SUCCESSFULLY.getMessage()).isEqualTo(changeNameResponse.getMessage());

        GetUserResponse getUserResponse = new GetUserRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(null).extract().body().as(GetUserResponse.class);

        softly.assertThat(changeNameRequest.getName()).isEqualTo(getUserResponse.getName());
        softly.assertThat(userRequest.getUsername()).isEqualTo(getUserResponse.getUsername());
    }

    @Test
    public void userCanChangeNameToTheSameNameTest() {
        String newName = "ALSU shafar";
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                .name(newName)
                .build();
        ChangeNameResponse changeNameResponseFirst = new ChangeNameRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(changeNameRequest).extract().body().as(ChangeNameResponse.class);
        softly.assertThat(changeNameRequest.getName()).isEqualTo(changeNameResponseFirst.getCustomer().getName());
        softly.assertThat(userRequest.getUsername()).isEqualTo(changeNameResponseFirst.getCustomer().getUsername());
        softly.assertThat(Messages.PROFILE_UPDATED_SUCCESSFULLY.getMessage()).isEqualTo(changeNameResponseFirst.getMessage());

        ChangeNameResponse changeNameResponseSecond = new ChangeNameRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(changeNameRequest).extract().body().as(ChangeNameResponse.class);
        softly.assertThat(changeNameResponseFirst).isEqualTo(changeNameResponseSecond);

        GetUserResponse getUserResponse = new GetUserRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(null).extract().body().as(GetUserResponse.class);

        softly.assertThat(changeNameRequest.getName()).isEqualTo(getUserResponse.getName());
        softly.assertThat(userRequest.getUsername()).isEqualTo(getUserResponse.getUsername());
    }

    /*
    User cannot change name to empty value
    User cannot change name to value contains only spaces
    User cannot change name to value contains number
    User cannot change name to value contains special symbol
    User cannot change name to value contains only one word
    User cannot change name to value separated by two spaces
    User cannot change name to value separated by _
     */
    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "Alsu1 test2", "Alsu!@#$%%^&*() test_+/,<>?}{[]';/.!", "Alsutest", "Alsu  test", "Alsu_test"})
    public void userCannotChangeNameToInvalidValueTest(String name) {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest).extract().body().as(CreateUserResponse.class);

        GetUserResponse getUserResponseBefore = new GetUserRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(null).extract().body().as(GetUserResponse.class);

        ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                .name(name)
                .build();
        new ChangeNameRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest(Messages.PROFILE_UPDATE_ERROR.getMessage()))
                .post(changeNameRequest);

        GetUserResponse getUserResponseAfter = new GetUserRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(null).extract().body().as(GetUserResponse.class);
        softly.assertThat(getUserResponseBefore).isEqualTo(getUserResponseAfter);
    }

    @Test
    public void userCannotChangeNameToNullTest() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest).extract().body().as(CreateUserResponse.class);

        GetUserResponse getUserResponseBefore = new GetUserRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(null).extract().body().as(GetUserResponse.class);

        ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                .name(null)
                .build();
        new ChangeNameRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest(Messages.PROFILE_UPDATE_ERROR.getMessage()))
                .post(changeNameRequest);

        GetUserResponse getUserResponseAfter = new GetUserRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(null).extract().body().as(GetUserResponse.class);
        softly.assertThat(getUserResponseBefore).isEqualTo(getUserResponseAfter);
    }
}
