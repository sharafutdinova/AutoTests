package iteration1.api;

import api.dao.AccountDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.models.accounts.CreateAccountResponse;
import api.models.admin.CreateUserRequest;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.requests.steps.DataBaseSteps;
import baseTests.BaseTest;
import common.annotations.UserApiSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import api.requests.skeleton.Endpoint;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

public class CreateAccountTest extends BaseTest {
    @Test
    @UserApiSession
    public void userCanCreateAccountTest() {
        CreateUserRequest user = SessionStorage.getUser();
        CreateAccountResponse createAccountResponse = new ValidatedCrudRequester<CreateAccountResponse>
                (RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                        Endpoint.ACCOUNTS,
                        ResponseSpecs.entityWasCreated())
                .post();

        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());

        DaoAndModelAssertions.assertThat(createAccountResponse, accountDao).match();
    }
}