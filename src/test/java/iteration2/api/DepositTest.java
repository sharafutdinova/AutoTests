package iteration2.api;

import api.generators.RandomData;
import api.models.Messages;
import api.models.TransactionTypes;
import api.models.accounts.CreateAccountResponse;
import api.models.accounts.DepositRequest;
import api.models.accounts.DepositResponse;
import api.models.accounts.GetAccountTransactionsResponse;
import api.models.comparison.TransactionsComparing;
import baseTests.BaseTest;
import common.annotations.UserApiSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;


public class DepositTest extends BaseTest {
    /*
    User can deposit 5000
    User can deposit 4999.99
    User can deposit 0.01
     */
    @ParameterizedTest
    @ValueSource(doubles = {5000, 4999.99, 0.01})
    @UserApiSession
    public void userCanDepositTest(Double amount) {
        CreateAccountResponse createAccountResponse = SessionStorage.getSteps().createAccount();

        DepositRequest depositRequest = DepositRequest.builder().id(createAccountResponse.getId()).balance(amount).build();
        DepositResponse depositResponse = new ValidatedCrudRequester<DepositResponse>
                (RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK()).post(depositRequest);

        softly.assertThat(TransactionsComparing.validateDepositTransaction(depositRequest, depositResponse)).isTrue();
        GetAccountTransactionsResponse getAccountTransactions = SessionStorage.getSteps()
                .getAccountTransactions(createAccountResponse.getId());
        softly.assertThat(depositResponse.getTransactions()).isEqualTo(getAccountTransactions.getTransactions());
        softly.assertThat(getAccountTransactions.getTransactions().size()).isEqualTo(1);
        softly.assertThat(getAccountTransactions.getTransactions().getFirst().getAmount()).isEqualTo(amount);
        softly.assertThat(getAccountTransactions.getTransactions().getFirst().getType())
                .isEqualTo(TransactionTypes.TRANSACTION_TYPE_FOR_DEPOSIT.getDescription());

        CreateAccountResponse account = SessionStorage.getSteps().getCustomerAccount(createAccountResponse.getId());
        softly.assertThat(account.getBalance()).isEqualTo(amount);
        softly.assertThat(account.getTransactions().size()).isEqualTo(1);
    }

    /*
    User cannot deposit 0
    User cannot deposit negative value
    User cannot deposit value more than 5000
     */
    public static Stream<Arguments> negativeCasesForDepositTest() {
        return Stream.of(
                Arguments.of(0.00, "Deposit amount must be at least 0.01"),
                Arguments.of(-0.01, "Deposit amount must be at least 0.01"),
                Arguments.of(5000.01, "Deposit amount cannot exceed 5000"));
    }

    @ParameterizedTest
    @MethodSource("negativeCasesForDepositTest")
    @UserApiSession
    public void userCanNotDepositTest(Double amount, String errorMessage) {
        CreateAccountResponse createAccountResponse = SessionStorage.getSteps().createAccount();

        DepositRequest depositRequest = DepositRequest.builder().id(createAccountResponse.getId()).balance(amount).build();
        new CrudRequester(RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsBadRequest(errorMessage)).post(depositRequest);

        GetAccountTransactionsResponse getAccountTransactions = SessionStorage.getSteps()
                .getAccountTransactions(createAccountResponse.getId());
        softly.assertThat(getAccountTransactions.getTransactions().size()).isZero();

        CreateAccountResponse account = SessionStorage.getSteps().getCustomerAccount(createAccountResponse.getId());
        softly.assertThat(account.getBalance()).isZero();
        softly.assertThat(account.getTransactions().size()).isZero();
    }

    @Test
    @UserApiSession
    public void userCanNotDepositToNotExistsAccountTest() {
        int notExistsAccountId = 10000;
        double amount = RandomData.getDepositAmount();
        DepositRequest depositRequest = DepositRequest.builder().id(notExistsAccountId).balance(amount).build();
        new CrudRequester(RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsForbidden(Messages.FORBIDDEN_ERROR.getMessage())).post(depositRequest);
        List<CreateAccountResponse> getAccountsResponse = SessionStorage.getSteps().getCustomerAccounts();
        softly.assertThat(getAccountsResponse.size()).isZero();
    }

    @Test
    @UserApiSession
    public void userCanNotDepositToAnotherUserAccountTest() {
        SessionStorage.addUser(AdminSteps.createUser());
        int anotherUserId = 1;
        CreateAccountResponse createAccountResponse = SessionStorage.getSteps(anotherUserId).createAccount();

        double amount = RandomData.getDepositAmount();
        DepositRequest depositRequest = DepositRequest.builder().id(createAccountResponse.getId()).balance(amount).build();
        new CrudRequester(RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsForbidden(Messages.FORBIDDEN_ERROR.getMessage())).post(depositRequest);

        GetAccountTransactionsResponse getAccountTransactions = SessionStorage.getSteps(anotherUserId)
                .getAccountTransactions(createAccountResponse.getId());
        softly.assertThat(getAccountTransactions.getTransactions().size()).isZero();
        CreateAccountResponse account = SessionStorage.getSteps(anotherUserId).getCustomerAccount(createAccountResponse.getId());
        softly.assertThat(account.getBalance()).isZero();
        softly.assertThat(account.getTransactions().size()).isZero();
    }
}
