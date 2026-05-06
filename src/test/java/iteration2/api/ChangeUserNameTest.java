package iteration2.api;

import api.dao.UserDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.generators.RandomData;
import api.models.Messages;
import api.models.admin.CreateUserRequest;
import api.models.customer.UpdateProfileRequest;
import api.models.customer.UpdateProfileResponse;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.requests.steps.DataBaseSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import baseTests.BaseTest;
import common.annotations.UserApiSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ChangeUserNameTest extends BaseTest {
  /*
  User can change name to name contains two words
  User can change name to name contains two words with length 1
  User can change name to name contains two words with russian letters
   */
  @ParameterizedTest
  @ValueSource(strings = {"Alsu Sharaf", "A S", "Алсу Шараф"})
  @UserApiSession
  public void userCanChangeNameTest(String name) {
    CreateUserRequest user = SessionStorage.getUser();
    UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder().name(name).build();
    UpdateProfileResponse updateProfileResponse =
        new ValidatedCrudRequester<UpdateProfileResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.UPDATE_CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
            .update(updateProfileRequest);

    UserDao userDao = DataBaseSteps.getUserByUsername(user.getUsername());
    DaoAndModelAssertions.assertThat(updateProfileResponse.getCustomer(), userDao).match();
    softly
        .assertThat(updateProfileResponse.getMessage())
        .isEqualTo(Messages.PROFILE_UPDATED_SUCCESSFULLY.getMessage());
  }

  @Test
  @UserApiSession
  public void userCanChangeNameToTheSameNameTest() {
    CreateUserRequest user = SessionStorage.getUser();
    String newName = RandomData.getName();
    UpdateProfileRequest updateProfileRequest =
        UpdateProfileRequest.builder().name(newName).build();
    UpdateProfileResponse updateProfileFirst =
        new ValidatedCrudRequester<UpdateProfileResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.UPDATE_CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
            .update(updateProfileRequest);

    UserDao userDao = DataBaseSteps.getUserByUsername(user.getUsername());
    DaoAndModelAssertions.assertThat(updateProfileFirst.getCustomer(), userDao).match();

    UpdateProfileResponse updateProfileSecond =
        new ValidatedCrudRequester<UpdateProfileResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.UPDATE_CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
            .update(updateProfileRequest);

    userDao = DataBaseSteps.getUserByUsername(user.getUsername());
    DaoAndModelAssertions.assertThat(updateProfileSecond.getCustomer(), userDao).match();
    softly
        .assertThat(updateProfileSecond.getMessage())
        .isEqualTo(Messages.PROFILE_UPDATED_SUCCESSFULLY.getMessage());
    softly.assertThat(updateProfileSecond).isEqualTo(updateProfileFirst);
  }

  @Test
  @UserApiSession
  public void userCanChangeNameSeveralTimesTest() {
    CreateUserRequest user = SessionStorage.getUser();
    String firstName = RandomData.getName();
    UpdateProfileRequest updateProfileRequestFirst =
        UpdateProfileRequest.builder().name(firstName).build();
    UpdateProfileResponse updateProfileResponseFirst =
        new ValidatedCrudRequester<UpdateProfileResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.UPDATE_CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
            .update(updateProfileRequestFirst);

    UserDao userDaoFirst = DataBaseSteps.getUserByUsername(user.getUsername());
    DaoAndModelAssertions.assertThat(updateProfileResponseFirst.getCustomer(), userDaoFirst)
        .match();

    String secondName = RandomData.getName();
    UpdateProfileRequest updateProfileRequestSecond =
        UpdateProfileRequest.builder().name(secondName).build();
    UpdateProfileResponse updateProfileResponseSecond =
        new ValidatedCrudRequester<UpdateProfileResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.UPDATE_CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
            .update(updateProfileRequestSecond);

    UserDao userDaoSecond = DataBaseSteps.getUserByUsername(user.getUsername());
    DaoAndModelAssertions.assertThat(updateProfileResponseSecond.getCustomer(), userDaoSecond)
        .match();
    softly
        .assertThat(updateProfileResponseSecond.getMessage())
        .isEqualTo(Messages.PROFILE_UPDATED_SUCCESSFULLY.getMessage());
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
  @ValueSource(
      strings = {
        "",
        "   ",
        "Alsu1 test2",
        "Alsu!@#$%%^&*() test_+/,<>?}{[]';/.!",
        "Alsutest",
        "Alsu  test",
        "Alsu_test"
      })
  @UserApiSession
  public void userCannotChangeNameToInvalidValueTest(String name) {
    CreateUserRequest user = SessionStorage.getUser();
    UserDao userDaoBefore = DataBaseSteps.getUserByUsername(user.getUsername());

    UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder().name(name).build();
    new CrudRequester(
            RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
            Endpoint.UPDATE_CUSTOMER_PROFILE,
            ResponseSpecs.requestReturnsBadRequest(Messages.PROFILE_UPDATE_ERROR.getMessage()))
        .update(updateProfileRequest);

    UserDao userDaoAfter = DataBaseSteps.getUserByUsername(user.getUsername());
    softly.assertThat(userDaoAfter).isEqualTo(userDaoBefore);
  }

  @Test
  @UserApiSession
  public void userCannotChangeNameToNullTest() {
    CreateUserRequest user = SessionStorage.getUser();
    UserDao userDaoBefore = DataBaseSteps.getUserByUsername(user.getUsername());

    UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder().name(null).build();
    new CrudRequester(
            RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
            Endpoint.UPDATE_CUSTOMER_PROFILE,
            ResponseSpecs.requestReturnsBadRequest(Messages.PROFILE_UPDATE_ERROR.getMessage()))
        .update(updateProfileRequest);

    UserDao userDaoAfter = DataBaseSteps.getUserByUsername(user.getUsername());
    softly.assertThat(userDaoAfter).isEqualTo(userDaoBefore);
  }
}
