package iteration1.api;

import api.models.accounts.CreateAccountResponse;
import api.models.admin.CreateUserRequest;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import baseTests.BaseTest;
import common.annotations.UserApiSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import api.requests.skeleton.Endpoint;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;

public class CreateAccountTest extends BaseTest {
    @Test
    @UserApiSession
    public void userCanCreateAccountTest() {
        CreateUserRequest user = SessionStorage.getUser();
        CreateAccountResponse createAccountResponse = new ValidatedCrudRequester<CreateAccountResponse>(RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post();
        List<CreateAccountResponse> accounts = SessionStorage.getSteps().getCustomerAccounts();
        softly.assertThat(accounts.contains(createAccountResponse)).isTrue();
    }
}