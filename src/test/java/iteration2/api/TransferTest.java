package iteration2.api;

import api.dao.AccountDao;
import api.dao.TransactionDao;
import api.generators.RandomData;
import api.models.Messages;
import api.models.TransactionTypes;
import api.models.accounts.CreateAccountResponse;
import api.models.accounts.TransferRequest;
import api.models.accounts.TransferResponse;
import api.requests.steps.DataBaseSteps;
import baseTests.BaseTest;
import common.annotations.UserApiSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

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
    @UserApiSession
    public void userCanTransferTest(double amount) {
        CreateAccountResponse senderAccount = SessionStorage.getSteps().createAccount();
        int maxAmountForDeposit = 5000;
        for (int i = 0; i < 3; i++) {
            SessionStorage.getSteps().deposit(senderAccount.getId(), maxAmountForDeposit);
        }
        CreateAccountResponse receiverAccountResponse = SessionStorage.getSteps().createAccount();
        AccountDao senderAccountDaoBefore = DataBaseSteps.getAccountByAccountNumber(senderAccount.getAccountNumber());

        TransferRequest transferRequest = TransferRequest.builder().amount(amount)
                .senderAccountId(senderAccount.getId())
                .receiverAccountId(receiverAccountResponse.getId()).build();
        TransferResponse transferResponse = new ValidatedCrudRequester<TransferResponse>
                (RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                        Endpoint.TRANSFER,
                        ResponseSpecs.requestReturnsOK()).post(transferRequest);

        softly.assertThat(transferResponse.getMessage()).isEqualTo(Messages.TRANSFER_SUCCESSFUL.getMessage());
        AccountDao receiverAccountDao = DataBaseSteps.getAccountByAccountNumber(receiverAccountResponse.getAccountNumber());
        softly.assertThat(receiverAccountDao.getBalance()).isEqualTo(amount);
        AccountDao senderAccountDaoAfter = DataBaseSteps.getAccountByAccountNumber(senderAccount.getAccountNumber());
        softly.assertThat(senderAccountDaoAfter.getBalance()).isEqualTo(senderAccountDaoBefore.getBalance() - amount);

        TransactionDao receiverTransactionDao = DataBaseSteps.getLastTransactionByAccountId(receiverAccountResponse.getId());
        softly.assertThat(receiverTransactionDao.getType()).isEqualTo(TransactionTypes.TRANSACTION_TYPE_FOR_TRANSFER_IN.getDescription());
        softly.assertThat(receiverTransactionDao.getAmount()).isEqualTo(amount);
        TransactionDao senderTransactionDao = DataBaseSteps.getLastTransactionByAccountId(senderAccount.getId());
        softly.assertThat(senderTransactionDao.getType()).isEqualTo(TransactionTypes.TRANSACTION_TYPE_FOR_TRANSFER_OUT.getDescription());
        softly.assertThat(senderTransactionDao.getAmount()).isEqualTo(amount);
    }

    @Test
    @UserApiSession
    public void userCanTransferValueEqualToBalanceToAnotherUserAccountTest() {
        SessionStorage.addUser(AdminSteps.createUser());
        int anotherUserId = 1;
        CreateAccountResponse createSenderAccountResponse = SessionStorage.getSteps().createAccount();
        CreateAccountResponse createReceiverAccountResponse = SessionStorage.getSteps(anotherUserId).createAccount();
        double amountForDeposit = RandomData.getDepositAmount();
        SessionStorage.getSteps().deposit(createSenderAccountResponse.getId(), amountForDeposit);

        TransferRequest transferRequest = TransferRequest.builder().amount(amountForDeposit)
                .senderAccountId(createSenderAccountResponse.getId())
                .receiverAccountId(createReceiverAccountResponse.getId()).build();
        TransferResponse transferResponse = new ValidatedCrudRequester<TransferResponse>
                (RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                        Endpoint.TRANSFER,
                        ResponseSpecs.requestReturnsOK()).post(transferRequest);

        softly.assertThat(transferResponse.getMessage()).isEqualTo(Messages.TRANSFER_SUCCESSFUL.getMessage());
        AccountDao receiverAccountDao = DataBaseSteps.getAccountByAccountNumber(createReceiverAccountResponse.getAccountNumber());
        softly.assertThat(receiverAccountDao.getBalance()).isEqualTo(amountForDeposit);
        AccountDao senderAccountDao = DataBaseSteps.getAccountByAccountNumber(createSenderAccountResponse.getAccountNumber());
        softly.assertThat(senderAccountDao.getBalance()).isZero();

        TransactionDao receiverTransactionDao = DataBaseSteps.getLastTransactionByAccountId(createReceiverAccountResponse.getId());
        softly.assertThat(receiverTransactionDao.getType()).isEqualTo(TransactionTypes.TRANSACTION_TYPE_FOR_TRANSFER_IN.getDescription());
        softly.assertThat(receiverTransactionDao.getAmount()).isEqualTo(amountForDeposit);
        TransactionDao senderTransactionDao = DataBaseSteps.getLastTransactionByAccountId(createSenderAccountResponse.getId());
        softly.assertThat(senderTransactionDao.getType()).isEqualTo(TransactionTypes.TRANSACTION_TYPE_FOR_TRANSFER_OUT.getDescription());
        softly.assertThat(senderTransactionDao.getAmount()).isEqualTo(amountForDeposit);
    }

    /*
        User cannot transfer value greater than 10000
        User cannot transfer 0
        User cannot transfer negative value
    */
    public static Stream<Arguments> negativeCasesForTransferTest() {
        return Stream.of(
                Arguments.of(10000.01, "Transfer amount cannot exceed 10000"),
                Arguments.of(0.00, "Invalid transfer: insufficient funds or invalid accounts"),
                Arguments.of(-0.01, "Invalid transfer: insufficient funds or invalid accounts"));
    }

    @ParameterizedTest
    @MethodSource("negativeCasesForTransferTest")
    @UserApiSession
    public void userCanNotTransferInvalidAmountTest(double amount, String errorMessage) {
        CreateAccountResponse createSenderAccountResponse = SessionStorage.getSteps().createAccount();
        int maxAmountForDeposit = 5000;
        for (int i = 0; i < 3; i++) {
            SessionStorage.getSteps().deposit(createSenderAccountResponse.getId(), maxAmountForDeposit);
        }
        CreateAccountResponse createReceiverAccountResponse = SessionStorage.getSteps().createAccount();
        AccountDao senderAccountDaoBefore = DataBaseSteps.getAccountByAccountNumber(createSenderAccountResponse.getAccountNumber());

        TransferRequest transferRequest = TransferRequest.builder().amount(amount)
                .senderAccountId(createSenderAccountResponse.getId())
                .receiverAccountId(createReceiverAccountResponse.getId()).build();
        new CrudRequester(RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequest(errorMessage)).post(transferRequest);

        AccountDao receiverAccountDao = DataBaseSteps.getAccountByAccountNumber(createReceiverAccountResponse.getAccountNumber());
        softly.assertThat(receiverAccountDao.getBalance()).isZero();
        AccountDao senderAccountDaoAfter = DataBaseSteps.getAccountByAccountNumber(createSenderAccountResponse.getAccountNumber());
        softly.assertThat(senderAccountDaoAfter).isEqualTo(senderAccountDaoBefore);
    }

    @Test
    @UserApiSession
    public void userCanNotTransferValueGreaterThanBalanceTest() {
        CreateAccountResponse createSenderAccountResponse = SessionStorage.getSteps().createAccount();
        CreateAccountResponse createReceiverAccountResponse = SessionStorage.getSteps().createAccount();
        double amountForDeposit = RandomData.getDepositAmount();
        SessionStorage.getSteps().deposit(createSenderAccountResponse.getId(), amountForDeposit);
        AccountDao senderAccountDaoBefore = DataBaseSteps.getAccountByAccountNumber(createSenderAccountResponse.getAccountNumber());

        TransferRequest transferRequest = TransferRequest.builder().amount(amountForDeposit + 1)
                .senderAccountId(createSenderAccountResponse.getId())
                .receiverAccountId(createReceiverAccountResponse.getId()).build();
        new CrudRequester(RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequest(Messages.TRANSFER_INVALID.getMessage())).post(transferRequest);

        AccountDao receiverAccountDao = DataBaseSteps.getAccountByAccountNumber(createReceiverAccountResponse.getAccountNumber());
        softly.assertThat(receiverAccountDao.getBalance()).isZero();
        AccountDao senderAccountDaoAfter = DataBaseSteps.getAccountByAccountNumber(createSenderAccountResponse.getAccountNumber());
        softly.assertThat(senderAccountDaoAfter).isEqualTo(senderAccountDaoBefore);
    }

    @Test
    @UserApiSession
    public void userCanNotTransferToTheSameAccountTest() {
        CreateAccountResponse senderAccount = SessionStorage.getSteps().createAccount();
        double depositAmount = RandomData.getDepositAmount();
        SessionStorage.getSteps().deposit(senderAccount.getId(), depositAmount);
        AccountDao senderAccountDaoBefore = DataBaseSteps.getAccountByAccountNumber(senderAccount.getAccountNumber());
        List<TransactionDao> senderTransactionsBefore = DataBaseSteps.getTransactionsByAccountId(senderAccount.getId());

        TransferRequest transferRequest = TransferRequest.builder().amount(depositAmount)
                .senderAccountId(senderAccount.getId())
                .receiverAccountId(senderAccount.getId()).build();
        new CrudRequester(RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequest(Messages.TRANSFER_INVALID.getMessage())).post(transferRequest);

        AccountDao senderAccountDaoAfter = DataBaseSteps.getAccountByAccountNumber(senderAccount.getAccountNumber());
        softly.assertThat(senderAccountDaoAfter).isEqualTo(senderAccountDaoBefore);
        List<TransactionDao> senderTransactionsAfter = DataBaseSteps.getTransactionsByAccountId(senderAccount.getId());
        softly.assertThat(senderTransactionsAfter).isEqualTo(senderTransactionsBefore);
    }

    @Test
    @UserApiSession
    public void userCanNotTransferFromNotExistsAccountTest() {
        CreateAccountResponse createAccount = SessionStorage.getSteps().createAccount();
        double amountForDeposit = RandomData.getDepositAmount();

        int notExistsAccountId = 9999;
        TransferRequest transferRequest = TransferRequest.builder().amount(amountForDeposit)
                .senderAccountId(createAccount.getId() + notExistsAccountId)
                .receiverAccountId(createAccount.getId()).build();
        new CrudRequester(RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsForbidden(Messages.FORBIDDEN_ERROR.getMessage())).post(transferRequest);

        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createAccount.getAccountNumber());
        softly.assertThat(accountDao.getBalance()).isZero();
    }

    @Test
    @UserApiSession
    public void userCanNotTransferToNotExistsAccountTest() {
        CreateAccountResponse createAccount = SessionStorage.getSteps().createAccount();
        double amountForDeposit = RandomData.getDepositAmount();
        SessionStorage.getSteps().deposit(createAccount.getId(), amountForDeposit);

        int notExistsAccountId = 9999;
        TransferRequest transferRequest = TransferRequest.builder().amount(amountForDeposit)
                .senderAccountId(createAccount.getId())
                .receiverAccountId(createAccount.getId() + notExistsAccountId).build();
        new CrudRequester(RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequest(Messages.TRANSFER_INVALID.getMessage())).post(transferRequest);

        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createAccount.getAccountNumber());
        softly.assertThat(accountDao.getBalance()).isEqualTo(amountForDeposit);
    }

    @Test
    @UserApiSession
    public void userCanNotTransferFromAnotherUserAccountTest() {
        SessionStorage.addUser(AdminSteps.createUser());
        int anotherUserId = 1;
        CreateAccountResponse createReceiverAccount = SessionStorage.getSteps().createAccount();
        CreateAccountResponse createSenderAccount = SessionStorage.getSteps(anotherUserId).createAccount();
        double amountForDeposit = RandomData.getDepositAmount();
        SessionStorage.getSteps(anotherUserId).deposit(createSenderAccount.getId(), amountForDeposit);

        TransferRequest transferRequest = TransferRequest.builder().amount(amountForDeposit)
                .senderAccountId(createSenderAccount.getId())
                .receiverAccountId(createReceiverAccount.getId()).build();
        new CrudRequester(RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsForbidden(Messages.FORBIDDEN_ERROR.getMessage())).post(transferRequest);

        AccountDao receiverAccountDao = DataBaseSteps.getAccountByAccountNumber(createReceiverAccount.getAccountNumber());
        softly.assertThat(receiverAccountDao.getBalance()).isZero();
        AccountDao senderAccountDao = DataBaseSteps.getAccountByAccountNumber(createSenderAccount.getAccountNumber());
        softly.assertThat(senderAccountDao.getBalance()).isEqualTo(amountForDeposit);
    }
}
