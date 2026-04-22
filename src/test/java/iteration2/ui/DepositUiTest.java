package iteration2.ui;

import api.models.customer.GetAccountsResponse;
import api.generators.RandomData;
import api.models.Account;
import api.models.TransactionTypes;
import api.models.accounts.CreateAccountResponse;
import api.models.accounts.GetAccountTransactionsResponse;
import org.junit.jupiter.api.Test;
import api.requests.steps.UserSteps;
import ui.pages.BankAlert;
import ui.pages.DepositPage;
import ui.pages.UserDashboard;

public class DepositUiTest extends BaseUiTest {
    @Test
    public void userCanDepositTest() {
        authAsUser(userRequest);
        CreateAccountResponse createAccountResponse = UserSteps.createAccount(userRequest);
        Double amount = RandomData.getDepositAmount();
        new UserDashboard().open().goToDepositPage();
        new DepositPage().performDeposit(createAccountResponse.getAccountNumber(), amount)
                .checkAlertMessageAndAccept(String.format(BankAlert.SUCCESSFULLY_DEPOSITED.getMessage(), amount, createAccountResponse.getAccountNumber()));

        GetAccountTransactionsResponse getAccountTransactionsResponse = UserSteps.getAccountTransactions(userRequest, createAccountResponse.getId());
        softly.assertThat(getAccountTransactionsResponse.getTransactions().size()).isEqualTo(1);
        softly.assertThat(getAccountTransactionsResponse.getTransactions().getFirst().getAmount()).isEqualTo(amount);
        softly.assertThat(getAccountTransactionsResponse.getTransactions().getFirst().getType()).isEqualTo(TransactionTypes.TRANSACTION_TYPE_FOR_DEPOSIT.getDescription());

        Account account = UserSteps.getCustomerAccount(userRequest, createAccountResponse.getId());
        softly.assertThat(account.getBalance()).isEqualTo(amount);
    }

    @Test
    public void userCanNotDepositInvalidValueTest() {
        authAsUser(userRequest);
        CreateAccountResponse createAccountResponse = UserSteps.createAccount(userRequest);
        Double amount = 6000.00;
        new DepositPage().open().performDeposit(createAccountResponse.getAccountNumber(), amount)
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_LESS_OR_EQUAL_5000.getMessage());

        GetAccountTransactionsResponse getAccountTransactionsResponse = UserSteps.getAccountTransactions(userRequest, createAccountResponse.getId());
        softly.assertThat(getAccountTransactionsResponse.getTransactions().size()).isZero();
        Account account = UserSteps.getCustomerAccount(userRequest, createAccountResponse.getId());
        softly.assertThat(account.getBalance()).isZero();
    }

    @Test
    public void userCanNotDepositWithoutEnteringAmountTest() {
        authAsUser(userRequest);
        CreateAccountResponse createAccountResponse = UserSteps.createAccount(userRequest);
        new DepositPage().open()
                .selectAccount(createAccountResponse.getAccountNumber())
                .clickToDeposit()
                .checkAlertMessageAndAccept(BankAlert.ENTER_A_VALID_AMOUNT.getMessage());

        GetAccountTransactionsResponse getAccountTransactionsResponse = UserSteps.getAccountTransactions(userRequest, createAccountResponse.getId());
        softly.assertThat(getAccountTransactionsResponse.getTransactions().size()).isZero();
        Account account = UserSteps.getCustomerAccount(userRequest, createAccountResponse.getId());
        softly.assertThat(account.getBalance()).isZero();
    }

    @Test
    public void userCanNotDepositWithoutSelectingAccountTest() {
        authAsUser(userRequest);
        Double amount = 6000.00;
        new DepositPage().open().enterAmount(amount)
                .clickToDeposit()
                .checkAlertMessageAndAccept(BankAlert.SELECT_AN_ACCOUNT.getMessage());

        GetAccountsResponse accounts = UserSteps.getCustomerAccounts(userRequest);
        softly.assertThat(accounts.getAccounts().size()).isZero();
    }

    @Test
    public void userCanNotDepositWithEmptyValuesTest() {
        authAsUser(userRequest);
        new DepositPage().open()
                .clickToDeposit()
                .checkAlertMessageAndAccept(BankAlert.SELECT_AN_ACCOUNT.getMessage());

        GetAccountsResponse accounts = UserSteps.getCustomerAccounts(userRequest);
        softly.assertThat(accounts.getAccounts().size()).isZero();
    }
}