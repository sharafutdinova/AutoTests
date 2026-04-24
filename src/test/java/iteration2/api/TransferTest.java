package iteration2.api;

import api.generators.RandomData;
import api.models.Account;
import api.models.Messages;
import api.models.Transaction;
import api.models.TransactionTypes;
import api.models.accounts.CreateAccountResponse;
import api.models.accounts.GetAccountTransactionsResponse;
import api.models.accounts.TransferRequest;
import api.models.accounts.TransferResponse;
import api.models.admin.CreateUserRequest;
import api.models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.requests.steps.UserSteps;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.stream.Stream;

public class TransferTest extends BaseAPITest {
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
        CreateAccountResponse createSenderAccountResponse = UserSteps.createAccount(userRequest);
        int maxAmountForDeposit = 5000;
        for (int i = 0; i < 3; i++) {
            UserSteps.deposit(userRequest, createSenderAccountResponse.getId(), maxAmountForDeposit);
        }
        CreateAccountResponse createReceiverAccountResponse = UserSteps.createAccount(userRequest);
        Account senderAccountBefore = UserSteps.getCustomerAccount(userRequest, createSenderAccountResponse.getId());

        TransferRequest transferRequest = TransferRequest.builder().amount(amount)
                .senderAccountId(createSenderAccountResponse.getId())
                .receiverAccountId(createReceiverAccountResponse.getId()).build();
        TransferResponse transferResponse = new ValidatedCrudRequester<TransferResponse>(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsOK()).post(transferRequest);

        ModelAssertions.assertThatModels(transferRequest, transferResponse).match();
        softly.assertThat(Messages.TRANSFER_SUCCESSFUL.getMessage()).isEqualTo(transferResponse.getMessage());
        Transaction lastReceiverTransaction = UserSteps.getAccountLastTransactions(userRequest, createReceiverAccountResponse.getId());
        softly.assertThat(lastReceiverTransaction.validateTransaction(TransactionTypes.TRANSACTION_TYPE_FOR_TRANSFER_IN, transferRequest.getAmount())).isTrue();
        Transaction lastSenderTransaction = UserSteps.getAccountLastTransactions(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(lastSenderTransaction.validateTransaction(TransactionTypes.TRANSACTION_TYPE_FOR_TRANSFER_OUT, transferRequest.getAmount())).isTrue();

        Account receiverAccount = UserSteps.getCustomerAccount(userRequest, createReceiverAccountResponse.getId());
        softly.assertThat(receiverAccount.getBalance()).isEqualTo(amount);
        Account senderAccountAfter = UserSteps.getCustomerAccount(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(senderAccountAfter.getBalance()).isEqualTo(senderAccountBefore.getBalance() - amount);
    }

    @Test
    public void userCanTransferValueEqualToBalanceToAnotherUserAccountTest() {
        CreateUserRequest anotherUserRequest = AdminSteps.createUser();
        CreateAccountResponse createSenderAccountResponse = UserSteps.createAccount(userRequest);
        CreateAccountResponse createReceiverAccountResponse = UserSteps.createAccount(anotherUserRequest);
        double amountForDeposit = RandomData.getDepositAmount();
        UserSteps.deposit(userRequest, createSenderAccountResponse.getId(), amountForDeposit);

        TransferRequest transferRequest = TransferRequest.builder().amount(amountForDeposit)
                .senderAccountId(createSenderAccountResponse.getId())
                .receiverAccountId(createReceiverAccountResponse.getId()).build();
        TransferResponse transferResponse = new ValidatedCrudRequester<TransferResponse>(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsOK()).post(transferRequest);

        ModelAssertions.assertThatModels(transferRequest, transferResponse).match();
        softly.assertThat(Messages.TRANSFER_SUCCESSFUL.getMessage()).isEqualTo(transferResponse.getMessage());
        Transaction lastReceiverTransaction = UserSteps.getAccountLastTransactions(anotherUserRequest, createReceiverAccountResponse.getId());
        softly.assertThat(lastReceiverTransaction.validateTransaction(TransactionTypes.TRANSACTION_TYPE_FOR_TRANSFER_IN, transferRequest.getAmount())).isTrue();
        Transaction lastSenderTransaction = UserSteps.getAccountLastTransactions(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(lastSenderTransaction.validateTransaction(TransactionTypes.TRANSACTION_TYPE_FOR_TRANSFER_OUT, transferRequest.getAmount())).isTrue();

        Account receiverAccount = UserSteps.getCustomerAccount(anotherUserRequest, createReceiverAccountResponse.getId());
        softly.assertThat(receiverAccount.getBalance()).isEqualTo(amountForDeposit);
        Account senderAccount = UserSteps.getCustomerAccount(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(senderAccount.getBalance()).isZero();
        AdminSteps.deleteUserByCreateUserRequest(anotherUserRequest);
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
        CreateAccountResponse createSenderAccountResponse = UserSteps.createAccount(userRequest);
        int maxAmountForDeposit = 5000;
        for (int i = 0; i < 3; i++) {
            UserSteps.deposit(userRequest, createSenderAccountResponse.getId(), maxAmountForDeposit);
            UserSteps.deposit(userRequest, createSenderAccountResponse.getId(), maxAmountForDeposit);
        }
        CreateAccountResponse createReceiverAccountResponse = UserSteps.createAccount(userRequest);
        GetAccountTransactionsResponse getSenderAccountTransactionsResponseBefore = UserSteps.getAccountTransactions(userRequest, createSenderAccountResponse.getId());
        Account senderAccountBefore = UserSteps.getCustomerAccount(userRequest, createSenderAccountResponse.getId());

        TransferRequest transferRequest = TransferRequest.builder().amount(amount)
                .senderAccountId(createSenderAccountResponse.getId())
                .receiverAccountId(createReceiverAccountResponse.getId()).build();
        new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequest(errorMessage)).post(transferRequest);

        GetAccountTransactionsResponse getSenderAccountTransactionsResponseAfter = UserSteps.getAccountTransactions(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(getSenderAccountTransactionsResponseBefore).isEqualTo(getSenderAccountTransactionsResponseAfter);
        GetAccountTransactionsResponse getReceiverAccountTransactionsResponse = UserSteps.getAccountTransactions(userRequest, createReceiverAccountResponse.getId());
        softly.assertThat(0).isEqualTo(getReceiverAccountTransactionsResponse.getTransactions().size());

        Account receiverAccount = UserSteps.getCustomerAccount(userRequest, createReceiverAccountResponse.getId());
        softly.assertThat(receiverAccount.getBalance()).isZero();
        Account senderAccountAfter = UserSteps.getCustomerAccount(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(senderAccountAfter.getBalance()).isEqualTo(senderAccountBefore.getBalance());
    }

    @Test
    public void userCanNotTransferValueGreaterThanBalanceTest() {
        CreateAccountResponse createSenderAccountResponse = UserSteps.createAccount(userRequest);
        CreateAccountResponse createReceiverAccountResponse = UserSteps.createAccount(userRequest);
        double amountForDeposit = RandomData.getDepositAmount();
        UserSteps.deposit(userRequest, createSenderAccountResponse.getId(), amountForDeposit);
        GetAccountTransactionsResponse getSenderAccountTransactionsResponseBefore = UserSteps.getAccountTransactions(userRequest, createSenderAccountResponse.getId());
        Account senderAccountBefore = UserSteps.getCustomerAccount(userRequest, createSenderAccountResponse.getId());

        TransferRequest transferRequest = TransferRequest.builder().amount(amountForDeposit + 1)
                .senderAccountId(createSenderAccountResponse.getId())
                .receiverAccountId(createReceiverAccountResponse.getId()).build();
        new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequest(Messages.TRANSFER_INVALID.getMessage())).post(transferRequest);

        GetAccountTransactionsResponse getSenderAccountTransactionsResponseAfter = UserSteps.getAccountTransactions(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(getSenderAccountTransactionsResponseBefore).isEqualTo(getSenderAccountTransactionsResponseAfter);
        GetAccountTransactionsResponse getReceiverAccountTransactionsResponse = UserSteps.getAccountTransactions(userRequest, createReceiverAccountResponse.getId());
        softly.assertThat(0).isEqualTo(getReceiverAccountTransactionsResponse.getTransactions().size());

        Account receiverAccount = UserSteps.getCustomerAccount(userRequest, createReceiverAccountResponse.getId());
        softly.assertThat(receiverAccount.getBalance()).isZero();
        Account senderAccountAfter = UserSteps.getCustomerAccount(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(senderAccountAfter.getBalance()).isEqualTo(senderAccountBefore.getBalance());
    }

    @Test
    public void userCanNotTransferToTheSameAccountTest() {
        CreateAccountResponse createSenderAccountResponse = UserSteps.createAccount(userRequest);
        double depositAmount = RandomData.getDepositAmount();
        UserSteps.deposit(userRequest, createSenderAccountResponse.getId(), depositAmount);
        GetAccountTransactionsResponse getSenderAccountTransactionsResponseBefore = UserSteps.getAccountTransactions(userRequest, createSenderAccountResponse.getId());

        TransferRequest transferRequest = TransferRequest.builder().amount(depositAmount)
                .senderAccountId(createSenderAccountResponse.getId())
                .receiverAccountId(createSenderAccountResponse.getId()).build();
        new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequest(Messages.TRANSFER_INVALID.getMessage())).post(transferRequest);

        GetAccountTransactionsResponse getSenderAccountTransactionsResponseAfter = UserSteps.getAccountTransactions(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(getSenderAccountTransactionsResponseBefore).isEqualTo(getSenderAccountTransactionsResponseAfter);
        Account senderAccountAfter = UserSteps.getCustomerAccount(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(senderAccountAfter.getBalance()).isEqualTo(depositAmount);
        softly.assertThat(senderAccountAfter.getTransactions().size()).isEqualTo(1);
    }

    @Test
    public void userCanNotTransferFromNotExistsAccountTest() {
        CreateAccountResponse createReceiverAccountResponse = UserSteps.createAccount(userRequest);
        double amountForDeposit = RandomData.getDepositAmount();

        int notExistsAccountId = 9999;
        TransferRequest transferRequest = TransferRequest.builder().amount(amountForDeposit)
                .senderAccountId(createReceiverAccountResponse.getId() + notExistsAccountId)
                .receiverAccountId(createReceiverAccountResponse.getId()).build();
        new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsForbidden(Messages.FORBIDDEN_ERROR.getMessage())).post(transferRequest);

        GetAccountTransactionsResponse getReceiverAccountTransactionsResponse = UserSteps.getAccountTransactions(userRequest, createReceiverAccountResponse.getId());
        softly.assertThat(getReceiverAccountTransactionsResponse.getTransactions().size()).isZero();
        Account receiverAccount = UserSteps.getCustomerAccount(userRequest, createReceiverAccountResponse.getId());
        softly.assertThat(receiverAccount.getBalance()).isZero();
    }

    @Test
    public void userCanNotTransferToNotExistsAccountTest() {
        CreateAccountResponse createSenderAccountResponse = UserSteps.createAccount(userRequest);
        double amountForDeposit = RandomData.getDepositAmount();
        UserSteps.deposit(userRequest, createSenderAccountResponse.getId(), amountForDeposit);
        GetAccountTransactionsResponse getSenderAccountTransactionsResponseBefore = UserSteps.getAccountTransactions(userRequest, createSenderAccountResponse.getId());

        int notExistsAccountId = 9999;
        TransferRequest transferRequest = TransferRequest.builder().amount(amountForDeposit)
                .senderAccountId(createSenderAccountResponse.getId())
                .receiverAccountId(createSenderAccountResponse.getId() + notExistsAccountId).build();
        new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequest(Messages.TRANSFER_INVALID.getMessage())).post(transferRequest);

        GetAccountTransactionsResponse getSenderAccountTransactionsResponseAfter = UserSteps.getAccountTransactions(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(getSenderAccountTransactionsResponseAfter).isEqualTo(getSenderAccountTransactionsResponseBefore);
        Account senderAccount = UserSteps.getCustomerAccount(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(senderAccount.getBalance()).isEqualTo(amountForDeposit);
    }

    @Test
    public void userCanNotTransferFromAnotherUserAccountTest() {
        CreateUserRequest anotherUserRequest = AdminSteps.createUser();
        CreateAccountResponse createReceiverAccountResponse = UserSteps.createAccount(userRequest);
        CreateAccountResponse createSenderAccountResponse = UserSteps.createAccount(anotherUserRequest);
        double amountForDeposit = RandomData.getDepositAmount();
        UserSteps.deposit(anotherUserRequest, createSenderAccountResponse.getId(), amountForDeposit);
        GetAccountTransactionsResponse getSenderAccountTransactionsBefore = UserSteps.getAccountTransactions(anotherUserRequest, createSenderAccountResponse.getId());

        TransferRequest transferRequest = TransferRequest.builder().amount(amountForDeposit)
                .senderAccountId(createSenderAccountResponse.getId())
                .receiverAccountId(createReceiverAccountResponse.getId()).build();
        new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsForbidden(Messages.FORBIDDEN_ERROR.getMessage())).post(transferRequest);

        GetAccountTransactionsResponse getReceiverAccountTransactions = UserSteps.getAccountTransactions(userRequest, createReceiverAccountResponse.getId());
        softly.assertThat(getReceiverAccountTransactions.getTransactions().size()).isZero();
        GetAccountTransactionsResponse getSenderAccountTransactionsAfter = UserSteps.getAccountTransactions(anotherUserRequest, createSenderAccountResponse.getId());
        softly.assertThat(getSenderAccountTransactionsAfter).isEqualTo(getSenderAccountTransactionsBefore);

        Account receiverAccount = UserSteps.getCustomerAccount(userRequest, createReceiverAccountResponse.getId());
        softly.assertThat(receiverAccount.getBalance()).isZero();
        Account senderAccount = UserSteps.getCustomerAccount(anotherUserRequest, createSenderAccountResponse.getId());
        softly.assertThat(senderAccount.getBalance()).isEqualTo(amountForDeposit);
        AdminSteps.deleteUserByCreateUserRequest(anotherUserRequest);
    }
}
