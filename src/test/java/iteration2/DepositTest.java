package iteration2;

import generators.RandomData;
import models.Messages;
import models.Transaction;
import models.TransactionTypes;
import models.UserRole;
import models.accounts.*;
import models.admin.CreateUserRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import requests.accounts.CreateAccountRequester;
import requests.accounts.DepositRequester;
import requests.accounts.GetAccountTransactionsRequester;
import requests.admin.AdminCreateUserRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Comparator;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DepositTest extends BaseTest {
    /*
    User can deposit 5000
    User can deposit 4999.99
    User can deposit 0.01
     */
    @ParameterizedTest
    @ValueSource(doubles = {5000, 4999.99, 0.01})
    public void userCanDepositTest(Double amount) {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);
        CreateAccountResponse createAccountResponse = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null).extract().body().as(CreateAccountResponse.class);

        DepositRequest depositRequest = DepositRequest.builder().id(createAccountResponse.getId()).balance(amount).build();
        DepositResponse depositResponse = new DepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(depositRequest).extract().body().as(DepositResponse.class);
        softly.assertThat(depositRequest.getBalance()).isEqualTo(depositResponse.getBalance());
        Transaction lastTransaction = depositResponse.getTransactions().stream().max(Comparator.comparing(Transaction::getId)).get();
        softly.assertThat(TransactionTypes.TRANSACTION_TYPE_FOR_DEPOSIT.getDescription()).isEqualTo(lastTransaction.getType());
        softly.assertThat(depositRequest.getBalance()).isEqualTo(lastTransaction.getAmount());

        GetAccountTransactionsRequest getAccountTransactionsRequest = GetAccountTransactionsRequest.builder().accountId(createAccountResponse.getId()).build();
        GetAccountTransactionsResponse getAccountTransactionsResponse = new GetAccountTransactionsRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(getAccountTransactionsRequest).extract().as(GetAccountTransactionsResponse.class);
        softly.assertThat(depositResponse.getTransactions()).isEqualTo(getAccountTransactionsResponse.getTransactions());
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
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);
        CreateAccountResponse createAccountResponse = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null).extract().body().as(CreateAccountResponse.class);

        DepositRequest depositRequest = DepositRequest.builder().id(createAccountResponse.getId()).balance(amount).build();
        new DepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest(errorMessage)).post(depositRequest);

        GetAccountTransactionsRequest getAccountTransactionsRequest = GetAccountTransactionsRequest.builder().accountId(createAccountResponse.getId()).build();
        GetAccountTransactionsResponse getAccountTransactionsResponse = new GetAccountTransactionsRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(getAccountTransactionsRequest).extract().as(GetAccountTransactionsResponse.class);
        softly.assertThat(0).isEqualTo(getAccountTransactionsResponse.getTransactions().size());
    }

    @Test
    public void userCanNotDepositToNotExistsAccountTest() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);
        int accountId = 1000;
        double amount = 550;
        DepositRequest depositRequest = DepositRequest.builder().id(accountId).balance(amount).build();
        new DepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsForbidden(Messages.FORBIDDEN_ERROR.getMessage())).post(depositRequest);
    }

    @Test
    public void userCanNotDepositToAnotherUserAccountTest() {
        CreateUserRequest baseUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(baseUserRequest);

        CreateUserRequest anotherUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(anotherUserRequest);
        CreateAccountResponse createAccountResponse = new CreateAccountRequester(RequestSpecs.authAsUser(anotherUserRequest.getUsername(), anotherUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null).extract().body().as(CreateAccountResponse.class);

        double amount = 550;
        DepositRequest depositRequest = DepositRequest.builder().id(createAccountResponse.getId()).balance(amount).build();
        new DepositRequester(RequestSpecs.authAsUser(baseUserRequest.getUsername(), baseUserRequest.getPassword()),
                ResponseSpecs.requestReturnsForbidden(Messages.FORBIDDEN_ERROR.getMessage())).post(depositRequest);
    }
}
