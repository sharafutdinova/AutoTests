package iteration2;

import generators.RandomData;
import models.comparison.UserNameComparing;
import models.customer.UpdateProfileRequest;
import models.customer.UpdateProfileResponse;
import models.customer.GetUserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import requests.steps.UserSteps;
import models.Messages;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class ChangeUserNameTest extends BaseTest {
    /*
    User can change name to name contains two words
    User can change name to name contains two words with length 1
    User can change name to name contains two words with russian letters
     */
    @ParameterizedTest
    @ValueSource(strings = {"Alsu Sharaf", "A S", "Алсу Шараф"})
    public void userCanChangeNameTest(String name) {
        UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder()
                .name(name)
                .build();
        UpdateProfileResponse updateProfileResponse = new ValidatedCrudRequester<UpdateProfileResponse>
                (RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                        Endpoint.UPDATE_CUSTOMER_PROFILE,
                        ResponseSpecs.requestReturnsOK())
                .update(updateProfileRequest);

        GetUserResponse getUserResponse = UserSteps.getUserResponse(userRequest);
        softly.assertThat(UserNameComparing.validateUpdateProfileResponse(updateProfileRequest, updateProfileResponse)).isTrue();
        softly.assertThat(getUserResponse.getName()).isEqualTo(updateProfileRequest.getName());
        softly.assertThat(getUserResponse.getUsername()).isEqualTo(userRequest.getUsername());
    }

    @Test
    public void userCanChangeNameToTheSameNameTest() {
        String newName = RandomData.getName();
        UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder()
                .name(newName)
                .build();
        UpdateProfileResponse updateProfileResponseFirst = new ValidatedCrudRequester<UpdateProfileResponse>
                (RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                        Endpoint.UPDATE_CUSTOMER_PROFILE,
                        ResponseSpecs.requestReturnsOK())
                .update(updateProfileRequest);

        softly.assertThat(UserNameComparing.validateUpdateProfileResponse(updateProfileRequest, updateProfileResponseFirst)).isTrue();

        UpdateProfileResponse updateProfileResponseSecond = new ValidatedCrudRequester<UpdateProfileResponse>
                (RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                        Endpoint.UPDATE_CUSTOMER_PROFILE,
                        ResponseSpecs.requestReturnsOK())
                .update(updateProfileRequest);

        softly.assertThat(updateProfileResponseSecond).isEqualTo(updateProfileResponseFirst);
        GetUserResponse getUserResponse = UserSteps.getUserResponse(userRequest);
        softly.assertThat(getUserResponse.getName()).isEqualTo(updateProfileRequest.getName());
        softly.assertThat(getUserResponse.getUsername()).isEqualTo(userRequest.getUsername());
    }

    @Test
    public void userCanChangeNameSeveralTimesTest() {
        String firstName = RandomData.getName();
        UpdateProfileRequest updateProfileRequestFirst = UpdateProfileRequest.builder()
                .name(firstName)
                .build();
        UpdateProfileResponse updateProfileResponseFirst = new ValidatedCrudRequester<UpdateProfileResponse>
                (RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                        Endpoint.UPDATE_CUSTOMER_PROFILE,
                        ResponseSpecs.requestReturnsOK())
                .update(updateProfileRequestFirst);

        softly.assertThat(UserNameComparing.validateUpdateProfileResponse(updateProfileRequestFirst, updateProfileResponseFirst)).isTrue();

        String secondName = RandomData.getName();
        UpdateProfileRequest updateProfileRequestSecond = UpdateProfileRequest.builder()
                .name(secondName)
                .build();
        UpdateProfileResponse updateProfileResponseSecond = new ValidatedCrudRequester<UpdateProfileResponse>
                (RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                        Endpoint.UPDATE_CUSTOMER_PROFILE,
                        ResponseSpecs.requestReturnsOK())
                .update(updateProfileRequestSecond);

        softly.assertThat(UserNameComparing.validateUpdateProfileResponse(updateProfileRequestSecond, updateProfileResponseSecond)).isTrue();
        GetUserResponse getUserResponse = UserSteps.getUserResponse(userRequest);
        softly.assertThat(getUserResponse.getName()).isEqualTo(secondName);
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
        GetUserResponse getUserResponseBefore = UserSteps.getUserResponse(userRequest);

        UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder()
                .name(name)
                .build();
        new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.UPDATE_CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsBadRequest(Messages.PROFILE_UPDATE_ERROR.getMessage()))
                .update(updateProfileRequest);

        GetUserResponse getUserResponseAfter = UserSteps.getUserResponse(userRequest);
        softly.assertThat(getUserResponseAfter).isEqualTo(getUserResponseBefore);
    }

    @Test
    public void userCannotChangeNameToNullTest() {
        GetUserResponse getUserResponseBefore = UserSteps.getUserResponse(userRequest);

        UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder()
                .name(null)
                .build();
        new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.UPDATE_CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsBadRequest(Messages.PROFILE_UPDATE_ERROR.getMessage()))
                .update(updateProfileRequest);

        GetUserResponse getUserResponseAfter = UserSteps.getUserResponse(userRequest);
        softly.assertThat(getUserResponseAfter).isEqualTo(getUserResponseBefore);
    }
}
