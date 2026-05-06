package iteration2.api;

import static org.junit.jupiter.api.Assertions.assertNull;

import api.dao.AccountDao;
import api.dao.TransactionDao;
import api.dao.UserDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.generators.RandomData;
import api.models.Messages;
import api.models.TransactionTypes;
import api.models.accounts.CreateAccountResponse;
import api.models.accounts.DepositRequest;
import api.models.accounts.DepositResponse;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.DataBaseSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import baseTests.BaseTest;
import common.annotations.UserApiSession;
import common.storage.SessionStorage;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

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

    DepositRequest depositRequest =
        DepositRequest.builder().id(createAccountResponse.getId()).balance(amount).build();
    DepositResponse depositResponse =
        new ValidatedCrudRequester<DepositResponse>(
                RequestSpecs.authAsUser(
                    SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK())
            .post(depositRequest);

    AccountDao accountDao =
        DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());
    DaoAndModelAssertions.assertThat(depositResponse, accountDao).match();
    softly.assertThat(depositResponse.getTransactions().size()).isEqualTo(1);
    TransactionDao transactionDao =
        DataBaseSteps.getTransactionById(depositResponse.getTransactions().getFirst().getId());
    DaoAndModelAssertions.assertThat(depositResponse.getTransactions().getFirst(), transactionDao)
        .match();
    softly
        .assertThat(transactionDao.getType())
        .isEqualTo(TransactionTypes.TRANSACTION_TYPE_FOR_DEPOSIT.getDescription());
  }

  /*
  User cannot deposit 0
  User cannot deposit negative value
  User cannot deposit value more than 5000
   */
  public static Stream<Arguments> negativeCasesForDepositTest() {
    return Stream.of(
        Arguments.of(0.00, "Invalid account or amount"),
        Arguments.of(-0.01, "Invalid account or amount"),
        Arguments.of(5000.01, "Deposit amount exceeds the 5000 limit"));
  }

  @ParameterizedTest
  @MethodSource("negativeCasesForDepositTest")
  @UserApiSession
  public void userCanNotDepositTest(Double amount, String errorMessage) {
    CreateAccountResponse createAccountResponse = SessionStorage.getSteps().createAccount();

    DepositRequest depositRequest =
        DepositRequest.builder().id(createAccountResponse.getId()).balance(amount).build();
    new CrudRequester(
            RequestSpecs.authAsUser(
                SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
            Endpoint.DEPOSIT,
            ResponseSpecs.requestReturnsBadRequest(errorMessage))
        .post(depositRequest);

    AccountDao accountDao =
        DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());
    softly.assertThat(accountDao.getBalance()).isZero();
    List<TransactionDao> transactions =
        DataBaseSteps.getTransactionsByAccountId(createAccountResponse.getId());
    softly.assertThat(transactions.size()).isZero();
  }

  @Test
  @UserApiSession
  public void userCanNotDepositToNotExistsAccountTest() {
    int notExistsAccountId = 10000;
    double amount = RandomData.getDepositAmount();
    DepositRequest depositRequest =
        DepositRequest.builder().id(notExistsAccountId).balance(amount).build();
    new CrudRequester(
            RequestSpecs.authAsUser(
                SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
            Endpoint.DEPOSIT,
            ResponseSpecs.requestReturnsForbidden(Messages.FORBIDDEN_ERROR.getMessage()))
        .post(depositRequest);

    UserDao userDao = DataBaseSteps.getUserByUsername(SessionStorage.getUser().getUsername());
    AccountDao accountDao = DataBaseSteps.getAccountByCustomerId(userDao.getId());
    assertNull(accountDao);
  }

  @Test
  @UserApiSession
  public void userCanNotDepositToAnotherUserAccountTest() {
    SessionStorage.addUser(AdminSteps.createUser());
    int anotherUserId = 1;
    CreateAccountResponse createAccountResponse =
        SessionStorage.getSteps(anotherUserId).createAccount();

    double amount = RandomData.getDepositAmount();
    DepositRequest depositRequest =
        DepositRequest.builder().id(createAccountResponse.getId()).balance(amount).build();
    new CrudRequester(
            RequestSpecs.authAsUser(
                SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
            Endpoint.DEPOSIT,
            ResponseSpecs.requestReturnsForbidden(Messages.FORBIDDEN_ERROR.getMessage()))
        .post(depositRequest);

    AccountDao accountDao =
        DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());
    softly.assertThat(accountDao.getBalance()).isZero();
    List<TransactionDao> transactions =
        DataBaseSteps.getTransactionsByAccountId(createAccountResponse.getId());
    softly.assertThat(transactions.size()).isZero();
  }
}
