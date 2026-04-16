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
import requests.accounts.CreateAccountRequester;
import requests.accounts.DepositRequester;
import requests.accounts.GetAccountTransactionsRequester;
import requests.accounts.TransferRequester;
import requests.admin.AdminCreateUserRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Comparator;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransferTest extends BaseTest {
    /*
    User can transfer 10000 to self account
    User can transfer 9999.99 to self account
    User can transfer 0.01 to self account
     */
    public static Stream<Arguments> positiveCasesForTransferTest() {
        return Stream.of(
                Arguments.of(10000.00),
                Arguments.of(9999.99),
                Arguments.of(0.01));
    }

    @ParameterizedTest
    @MethodSource("positiveCasesForTransferTest")
    public void userCanTransferTest(double amount) {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);
        CreateAccountResponse createSenderAccountResponse = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null).extract().body().as(CreateAccountResponse.class);
        int maxAmountForDeposit = 5000;
        DepositRequest depositRequest = DepositRequest.builder().id(createSenderAccountResponse.getId()).balance(maxAmountForDeposit).build();
        new DepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(depositRequest).extract().body().as(DepositResponse.class);
        new DepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(depositRequest).extract().body().as(DepositResponse.class);
        CreateAccountResponse createReceiverAccountResponse = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null).extract().body().as(CreateAccountResponse.class);

        TransferRequest transferRequest = TransferRequest.builder().amount(amount)
                .senderAccountId(createSenderAccountResponse.getId())
                .receiverAccountId(createReceiverAccountResponse.getId()).build();
        TransferResponse transferResponse = new TransferRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(transferRequest).extract().body().as(TransferResponse.class);
        softly.assertThat(transferRequest.getSenderAccountId()).isEqualTo(transferResponse.getSenderAccountId());
        softly.assertThat(transferRequest.getReceiverAccountId()).isEqualTo(transferResponse.getReceiverAccountId());
        softly.assertThat(transferRequest.getAmount()).isEqualTo(transferResponse.getAmount());
        softly.assertThat(Messages.TRANSFER_SUCCESSFUL.getMessage()).isEqualTo(transferResponse.getMessage());

        GetAccountTransactionsRequest getReceiverAccountTransactionsRequest = GetAccountTransactionsRequest.builder().accountId(createReceiverAccountResponse.getId()).build();
        GetAccountTransactionsResponse getReceiverTransactions = new GetAccountTransactionsRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(getReceiverAccountTransactionsRequest).extract().as(GetAccountTransactionsResponse.class);
        Transaction lastReceiverTransaction = getReceiverTransactions.getTransactions().stream().max(Comparator.comparing(Transaction::getId)).get();
        softly.assertThat(transferRequest.getAmount()).isEqualTo(lastReceiverTransaction.getAmount());
        softly.assertThat(TransactionTypes.TRANSACTION_TYPE_FOR_TRANSFER_IN.getDescription()).isEqualTo(lastReceiverTransaction.getType());

        GetAccountTransactionsRequest getSenderAccountTransactionsRequest = GetAccountTransactionsRequest.builder().accountId(createSenderAccountResponse.getId()).build();
        GetAccountTransactionsResponse getSenderTransactions = new GetAccountTransactionsRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(getSenderAccountTransactionsRequest).extract().as(GetAccountTransactionsResponse.class);
        Transaction lastSenderTransaction = getSenderTransactions.getTransactions().stream().max(Comparator.comparing(Transaction::getId)).get();
        softly.assertThat(transferRequest.getAmount()).isEqualTo(lastSenderTransaction.getAmount());
        softly.assertThat(TransactionTypes.TRANSACTION_TYPE_FOR_TRANSFER_OUT.getDescription()).isEqualTo(lastSenderTransaction.getType());
    }

    @Test
    public void userCanTransferValueEqualToBalanceToAnotherUserAccountTest() {
        CreateUserRequest baseUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();
        CreateUserRequest anotherUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(baseUserRequest);
        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(anotherUserRequest);
        CreateAccountResponse createSenderAccountResponse = new CreateAccountRequester(RequestSpecs.authAsUser(baseUserRequest.getUsername(), baseUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null).extract().body().as(CreateAccountResponse.class);
        CreateAccountResponse createReceiverAccountResponse = new CreateAccountRequester(RequestSpecs.authAsUser(anotherUserRequest.getUsername(), anotherUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null).extract().body().as(CreateAccountResponse.class);
        int amountForDeposit = 5000;
        DepositRequest depositRequest = DepositRequest.builder().id(createSenderAccountResponse.getId()).balance(amountForDeposit).build();
        new DepositRequester(RequestSpecs.authAsUser(baseUserRequest.getUsername(), baseUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(depositRequest).extract().body().as(DepositResponse.class);
        TransferRequest transferRequest = TransferRequest.builder().amount(amountForDeposit)
                .senderAccountId(createSenderAccountResponse.getId())
                .receiverAccountId(createReceiverAccountResponse.getId()).build();
        TransferResponse transferResponse = new TransferRequester(RequestSpecs.authAsUser(baseUserRequest.getUsername(), baseUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(transferRequest).extract().body().as(TransferResponse.class);
        softly.assertThat(transferRequest.getSenderAccountId()).isEqualTo(transferResponse.getSenderAccountId());
        softly.assertThat(transferRequest.getReceiverAccountId()).isEqualTo(transferResponse.getReceiverAccountId());
        softly.assertThat(transferRequest.getAmount()).isEqualTo(transferResponse.getAmount());
        softly.assertThat(Messages.TRANSFER_SUCCESSFUL.getMessage()).isEqualTo(transferResponse.getMessage());

        GetAccountTransactionsRequest getReceiverAccountTransactionsRequest = GetAccountTransactionsRequest.builder().accountId(createReceiverAccountResponse.getId()).build();
        GetAccountTransactionsResponse getReceiverTransactions = new GetAccountTransactionsRequester(RequestSpecs.authAsUser(anotherUserRequest.getUsername(), anotherUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(getReceiverAccountTransactionsRequest).extract().as(GetAccountTransactionsResponse.class);
        Transaction lastReceiverTransaction = getReceiverTransactions.getTransactions().stream().max(Comparator.comparing(Transaction::getId)).get();
        softly.assertThat(transferRequest.getAmount()).isEqualTo(lastReceiverTransaction.getAmount());
        softly.assertThat(TransactionTypes.TRANSACTION_TYPE_FOR_TRANSFER_IN.getDescription()).isEqualTo(lastReceiverTransaction.getType());

        GetAccountTransactionsRequest getSenderAccountTransactionsRequest = GetAccountTransactionsRequest.builder().accountId(createSenderAccountResponse.getId()).build();
        GetAccountTransactionsResponse getSenderTransactions = new GetAccountTransactionsRequester(RequestSpecs.authAsUser(baseUserRequest.getUsername(), baseUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(getSenderAccountTransactionsRequest).extract().as(GetAccountTransactionsResponse.class);
        Transaction lastSenderTransaction = getSenderTransactions.getTransactions().stream().max(Comparator.comparing(Transaction::getId)).get();
        softly.assertThat(transferRequest.getAmount()).isEqualTo(lastSenderTransaction.getAmount());
        softly.assertThat(TransactionTypes.TRANSACTION_TYPE_FOR_TRANSFER_OUT.getDescription()).isEqualTo(lastSenderTransaction.getType());
    }

    /*
        User cannot transfer value greater than 10000
        User cannot transfer 0
        User cannot transfer negative value
    */
    public static Stream<Arguments> negativeCasesForTransferTest() {
        return Stream.of(
                Arguments.of(10000.01, "Transfer amount cannot exceed 10000"),
                Arguments.of(0.00, "Transfer amount must be at least 0.01"),
                Arguments.of(-0.01, "Transfer amount must be at least 0.01"));
    }

    @ParameterizedTest
    @MethodSource("negativeCasesForTransferTest")
    public void userCanNotTransferInvalidAmountTest(double amount, String errorMessage) {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);
        CreateAccountResponse createSenderAccountResponse = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null).extract().body().as(CreateAccountResponse.class);
        int maxAmountForDeposit = 5000;
        DepositRequest depositRequest = DepositRequest.builder().id(createSenderAccountResponse.getId()).balance(maxAmountForDeposit).build();
        new DepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(depositRequest).extract().body().as(DepositResponse.class);
        new DepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(depositRequest).extract().body().as(DepositResponse.class);
        CreateAccountResponse createReceiverAccountResponse = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null).extract().body().as(CreateAccountResponse.class);
        GetAccountTransactionsRequest getSenderAccountTransactionsRequest = GetAccountTransactionsRequest.builder().accountId(createSenderAccountResponse.getId()).build();
        GetAccountTransactionsResponse getSenderAccountTransactionsResponseBefore = new GetAccountTransactionsRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(getSenderAccountTransactionsRequest).extract().as(GetAccountTransactionsResponse.class);

        TransferRequest transferRequest = TransferRequest.builder().amount(amount)
                .senderAccountId(createSenderAccountResponse.getId())
                .receiverAccountId(createReceiverAccountResponse.getId()).build();
        new TransferRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest(errorMessage)).post(transferRequest);

        GetAccountTransactionsResponse getSenderAccountTransactionsResponseAfter = new GetAccountTransactionsRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(getSenderAccountTransactionsRequest).extract().as(GetAccountTransactionsResponse.class);
        softly.assertThat(getSenderAccountTransactionsResponseBefore).isEqualTo(getSenderAccountTransactionsResponseAfter);

        GetAccountTransactionsRequest getReceiverAccountTransactionsRequest = GetAccountTransactionsRequest.builder().accountId(createReceiverAccountResponse.getId()).build();
        GetAccountTransactionsResponse getReceiverAccountTransactionsResponse = new GetAccountTransactionsRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(getReceiverAccountTransactionsRequest).extract().as(GetAccountTransactionsResponse.class);
        softly.assertThat(0).isEqualTo(getReceiverAccountTransactionsResponse.getTransactions().size());
    }

    @Test
    public void userCanNotTransferValueGreaterThanBalanceTest() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);
        CreateAccountResponse createSenderAccountResponse = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null).extract().body().as(CreateAccountResponse.class);
        double amountForDeposit = 550.00;
        DepositRequest depositRequest = DepositRequest.builder().id(createSenderAccountResponse.getId()).balance(amountForDeposit).build();
        new DepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(depositRequest).extract().body().as(DepositResponse.class);
        CreateAccountResponse createReceiverAccountResponse = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null).extract().body().as(CreateAccountResponse.class);
        GetAccountTransactionsRequest getSenderAccountTransactionsRequest = GetAccountTransactionsRequest.builder().accountId(createSenderAccountResponse.getId()).build();
        GetAccountTransactionsResponse getSenderAccountTransactionsResponseBefore = new GetAccountTransactionsRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(getSenderAccountTransactionsRequest).extract().as(GetAccountTransactionsResponse.class);

        TransferRequest transferRequest = TransferRequest.builder().amount(amountForDeposit + 1)
                .senderAccountId(createSenderAccountResponse.getId())
                .receiverAccountId(createReceiverAccountResponse.getId()).build();
        new TransferRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest(Messages.TRANSFER_INVALID.getMessage())).post(transferRequest);

        GetAccountTransactionsResponse getSenderAccountTransactionsResponseAfter = new GetAccountTransactionsRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(getSenderAccountTransactionsRequest).extract().as(GetAccountTransactionsResponse.class);
        softly.assertThat(getSenderAccountTransactionsResponseBefore).isEqualTo(getSenderAccountTransactionsResponseAfter);

        GetAccountTransactionsRequest getReceiverAccountTransactionsRequest = GetAccountTransactionsRequest.builder().accountId(createReceiverAccountResponse.getId()).build();
        GetAccountTransactionsResponse getReceiverAccountTransactionsResponse = new GetAccountTransactionsRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(getReceiverAccountTransactionsRequest).extract().as(GetAccountTransactionsResponse.class);
        softly.assertThat(0).isEqualTo(getReceiverAccountTransactionsResponse.getTransactions().size());
    }

    @Test
    public void userCanNotTransferToTheSameAccountTest() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);
        CreateAccountResponse createSenderAccountResponse = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null).extract().body().as(CreateAccountResponse.class);
        int maxAmountForDeposit = 5000;
        DepositRequest depositRequest = DepositRequest.builder().id(createSenderAccountResponse.getId()).balance(maxAmountForDeposit).build();
        new DepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(depositRequest).extract().body().as(DepositResponse.class);
        GetAccountTransactionsRequest getSenderAccountTransactionsRequest = GetAccountTransactionsRequest.builder().accountId(createSenderAccountResponse.getId()).build();
        GetAccountTransactionsResponse getSenderAccountTransactionsResponseBefore = new GetAccountTransactionsRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(getSenderAccountTransactionsRequest).extract().as(GetAccountTransactionsResponse.class);

        TransferRequest transferRequest = TransferRequest.builder().amount(maxAmountForDeposit)
                .senderAccountId(createSenderAccountResponse.getId())
                .receiverAccountId(createSenderAccountResponse.getId()).build();
        new TransferRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest(Messages.TRANSFER_INVALID.getMessage())).post(transferRequest);

        GetAccountTransactionsResponse getSenderAccountTransactionsResponseAfter = new GetAccountTransactionsRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(getSenderAccountTransactionsRequest).extract().as(GetAccountTransactionsResponse.class);
        softly.assertThat(getSenderAccountTransactionsResponseBefore).isEqualTo(getSenderAccountTransactionsResponseAfter);
    }

    @Test
    public void userCanNotTransferFromNotExistsAccountTest() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);
        CreateAccountResponse createSenderAccountResponse = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null).extract().body().as(CreateAccountResponse.class);
        int amountForDeposit = 5000;
        DepositRequest depositRequest = DepositRequest.builder().id(createSenderAccountResponse.getId()).balance(amountForDeposit).build();
        new DepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(depositRequest).extract().body().as(DepositResponse.class);
        GetAccountTransactionsRequest getSenderAccountTransactionsRequest = GetAccountTransactionsRequest.builder().accountId(createSenderAccountResponse.getId()).build();
        GetAccountTransactionsResponse getSenderAccountTransactionsResponseBefore = new GetAccountTransactionsRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(getSenderAccountTransactionsRequest).extract().as(GetAccountTransactionsResponse.class);
        int notExistsAccountId = 500;
        TransferRequest transferRequest = TransferRequest.builder().amount(amountForDeposit)
                .senderAccountId(createSenderAccountResponse.getId() + notExistsAccountId)
                .receiverAccountId(createSenderAccountResponse.getId()).build();
        new TransferRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsForbidden(Messages.FORBIDDEN_ERROR.getMessage())).post(transferRequest);

        GetAccountTransactionsResponse getSenderAccountTransactionsResponseAfter = new GetAccountTransactionsRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(getSenderAccountTransactionsRequest).extract().as(GetAccountTransactionsResponse.class);
        softly.assertThat(getSenderAccountTransactionsResponseBefore).isEqualTo(getSenderAccountTransactionsResponseAfter);
    }

    @Test
    public void userCanNotTransferToNotExistsAccountTest() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);
        CreateAccountResponse createSenderAccountResponse = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null).extract().body().as(CreateAccountResponse.class);
        int amountForDeposit = 5000;
        DepositRequest depositRequest = DepositRequest.builder().id(createSenderAccountResponse.getId()).balance(amountForDeposit).build();
        new DepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(depositRequest).extract().body().as(DepositResponse.class);
        GetAccountTransactionsRequest getSenderAccountTransactionsRequest = GetAccountTransactionsRequest.builder().accountId(createSenderAccountResponse.getId()).build();
        GetAccountTransactionsResponse getSenderAccountTransactionsResponseBefore = new GetAccountTransactionsRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(getSenderAccountTransactionsRequest).extract().as(GetAccountTransactionsResponse.class);
        int notExistsAccountId = 500;
        TransferRequest transferRequest = TransferRequest.builder().amount(amountForDeposit)
                .senderAccountId(createSenderAccountResponse.getId())
                .receiverAccountId(createSenderAccountResponse.getId() + notExistsAccountId).build();
        new TransferRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest(Messages.TRANSFER_INVALID.getMessage())).post(transferRequest);

        GetAccountTransactionsResponse getSenderAccountTransactionsResponseAfter = new GetAccountTransactionsRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(getSenderAccountTransactionsRequest).extract().as(GetAccountTransactionsResponse.class);
        softly.assertThat(getSenderAccountTransactionsResponseBefore).isEqualTo(getSenderAccountTransactionsResponseAfter);
    }

    @Test
    public void userCanNotTransferFromAnotherUserAccountTest() {
        CreateUserRequest baseUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();
        CreateUserRequest anotherUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(baseUserRequest);
        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(anotherUserRequest);
        CreateAccountResponse createReceiverAccountResponse = new CreateAccountRequester(RequestSpecs.authAsUser(baseUserRequest.getUsername(), baseUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null).extract().body().as(CreateAccountResponse.class);
        CreateAccountResponse createSenderAccountResponse = new CreateAccountRequester(RequestSpecs.authAsUser(anotherUserRequest.getUsername(), anotherUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null).extract().body().as(CreateAccountResponse.class);
        int amountForDeposit = 5000;
        DepositRequest depositRequest = DepositRequest.builder().id(createSenderAccountResponse.getId()).balance(amountForDeposit).build();
        new DepositRequester(RequestSpecs.authAsUser(anotherUserRequest.getUsername(), anotherUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(depositRequest).extract().body().as(DepositResponse.class);

        TransferRequest transferRequest = TransferRequest.builder().amount(amountForDeposit)
                .senderAccountId(createSenderAccountResponse.getId())
                .receiverAccountId(createReceiverAccountResponse.getId()).build();
       new TransferRequester(RequestSpecs.authAsUser(baseUserRequest.getUsername(), baseUserRequest.getPassword()),
                ResponseSpecs.requestReturnsForbidden(Messages.FORBIDDEN_ERROR.getMessage())).post(transferRequest);

        GetAccountTransactionsRequest getReceiverAccountTransactionsRequest = GetAccountTransactionsRequest.builder().accountId(createReceiverAccountResponse.getId()).build();
        GetAccountTransactionsResponse getReceiverAccountTransactions = new GetAccountTransactionsRequester(RequestSpecs.authAsUser(baseUserRequest.getUsername(), baseUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk()).post(getReceiverAccountTransactionsRequest).extract().as(GetAccountTransactionsResponse.class);
        softly.assertThat(0).isEqualTo(getReceiverAccountTransactions.getTransactions().size());
    }
}
