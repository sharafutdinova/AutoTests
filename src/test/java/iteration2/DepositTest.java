package iteration2;

import generators.RandomData;
import models.Account;
import models.Messages;
import models.TransactionTypes;
import models.accounts.*;
import models.admin.CreateUserRequest;
import models.comparison.TransactionsComparing;
import models.customer.GetAccountsResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import requests.steps.UserSteps;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;


public class DepositTest extends BaseTest {
    /*
    User can deposit 5000
    User can deposit 4999.99
    User can deposit 0.01
     */
    @ParameterizedTest
    @ValueSource(doubles = {5000, 4999.99, 0.01})
    public void userCanDepositTest(Double amount) {
        CreateAccountResponse createAccountResponse = UserSteps.createAccount(userRequest);

        DepositRequest depositRequest = DepositRequest.builder().id(createAccountResponse.getId()).balance(amount).build();
        DepositResponse depositResponse = new ValidatedCrudRequester<DepositResponse>(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK()).post(depositRequest);

        softly.assertThat(TransactionsComparing.validateDepositTransaction(depositRequest, depositResponse)).isTrue();
        GetAccountTransactionsResponse getAccountTransactionsResponse = UserSteps.getAccountTransactions(userRequest, createAccountResponse.getId());
        softly.assertThat(depositResponse.getTransactions()).isEqualTo(getAccountTransactionsResponse.getTransactions());
        softly.assertThat(getAccountTransactionsResponse.getTransactions().size()).isEqualTo(1);
        softly.assertThat(getAccountTransactionsResponse.getTransactions().getFirst().getAmount()).isEqualTo(amount);
        softly.assertThat(getAccountTransactionsResponse.getTransactions().getFirst().getType()).isEqualTo(TransactionTypes.TRANSACTION_TYPE_FOR_DEPOSIT.getDescription());

        Account account = UserSteps.getCustomerAccount(userRequest, createAccountResponse.getId());
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
    public void userCanNotDepositTest(Double amount, String errorMessage) {
        CreateAccountResponse createAccountResponse = UserSteps.createAccount(userRequest);

        DepositRequest depositRequest = DepositRequest.builder().id(createAccountResponse.getId()).balance(amount).build();
        new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsBadRequest(errorMessage)).post(depositRequest);

        GetAccountTransactionsResponse getAccountTransactionsResponse = UserSteps.getAccountTransactions(userRequest, createAccountResponse.getId());
        softly.assertThat(getAccountTransactionsResponse.getTransactions().size()).isZero();

        Account account = UserSteps.getCustomerAccount(userRequest, createAccountResponse.getId());
        softly.assertThat(account.getBalance()).isZero();
        softly.assertThat(account.getTransactions().size()).isZero();
    }

    @Test
    public void userCanNotDepositToNotExistsAccountTest() {
        int notExistsAccountId = 10000;
        double amount = RandomData.getDepositAmount();
        DepositRequest depositRequest = DepositRequest.builder().id(notExistsAccountId).balance(amount).build();
        new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsForbidden(Messages.FORBIDDEN_ERROR.getMessage())).post(depositRequest);
        GetAccountsResponse getAccountsResponse = UserSteps.getCustomerAccounts(userRequest);
        softly.assertThat(getAccountsResponse.getAccounts().size()).isZero();
    }

    @Test
    public void userCanNotDepositToAnotherUserAccountTest() {
        CreateUserRequest anotherUserRequest = AdminSteps.createUser();
        CreateAccountResponse createAccountResponse = UserSteps.createAccount(anotherUserRequest);

        double amount = RandomData.getDepositAmount();
        DepositRequest depositRequest = DepositRequest.builder().id(createAccountResponse.getId()).balance(amount).build();
        new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsForbidden(Messages.FORBIDDEN_ERROR.getMessage())).post(depositRequest);

        GetAccountTransactionsResponse getUserAccountTransactionsResponse = UserSteps.getAccountTransactions(anotherUserRequest, createAccountResponse.getId());
        softly.assertThat(getUserAccountTransactionsResponse.getTransactions().size()).isZero();
        Account account = UserSteps.getCustomerAccount(anotherUserRequest, createAccountResponse.getId());
        softly.assertThat(account.getBalance()).isZero();
        softly.assertThat(account.getTransactions().size()).isZero();
        AdminSteps.deleteUserByCreateUserRequest(anotherUserRequest);
    }
}
