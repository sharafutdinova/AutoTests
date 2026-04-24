package iteration2.ui;

import api.models.customer.GetAccountsResponse;
import api.generators.RandomData;
import api.models.Account;
import api.models.TransactionTypes;
import api.models.accounts.CreateAccountResponse;
import api.models.accounts.GetAccountTransactionsResponse;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.DepositPage;
import ui.pages.UserDashboard;

public class DepositUiTest extends BaseUiTest {
    @Test
    @UserSession
    public void userCanDepositTest() {
        CreateAccountResponse createAccountResponse = SessionStorage.getSteps().createAccount();
        Double amount = RandomData.getDepositAmount();
        new UserDashboard().open().goToDepositPage();
        new DepositPage().performDeposit(createAccountResponse.getAccountNumber(), amount)
                .checkAlertMessageAndAccept(String.format(BankAlert.SUCCESSFULLY_DEPOSITED.getMessage(), amount, createAccountResponse.getAccountNumber()));

        GetAccountTransactionsResponse getAccountTransactionsResponse = SessionStorage.getSteps().getAccountTransactions(createAccountResponse.getId());
        softly.assertThat(getAccountTransactionsResponse.getTransactions().size()).isEqualTo(1);
        softly.assertThat(getAccountTransactionsResponse.getTransactions().getFirst().getAmount()).isEqualTo(amount);
        softly.assertThat(getAccountTransactionsResponse.getTransactions().getFirst().getType()).isEqualTo(TransactionTypes.TRANSACTION_TYPE_FOR_DEPOSIT.getDescription());

        Account account = SessionStorage.getSteps().getCustomerAccount( createAccountResponse.getId());
        softly.assertThat(account.getBalance()).isEqualTo(amount);
    }

    @Test
    @UserSession
    public void userCanNotDepositInvalidValueTest() {
        CreateAccountResponse createAccountResponse = SessionStorage.getSteps().createAccount();
        Double amount = 6000.00;
        new DepositPage().open().performDeposit(createAccountResponse.getAccountNumber(), amount)
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_LESS_OR_EQUAL_5000.getMessage());

        GetAccountTransactionsResponse getAccountTransactionsResponse = SessionStorage.getSteps().getAccountTransactions(createAccountResponse.getId());
        softly.assertThat(getAccountTransactionsResponse.getTransactions().size()).isZero();
        Account account = SessionStorage.getSteps().getCustomerAccount(createAccountResponse.getId());
        softly.assertThat(account.getBalance()).isZero();
    }

    @Test
    @UserSession
    public void userCanNotDepositWithoutEnteringAmountTest() {
        CreateAccountResponse createAccountResponse = SessionStorage.getSteps().createAccount();
        new DepositPage().open()
                .selectAccount(createAccountResponse.getAccountNumber())
                .clickToDeposit()
                .checkAlertMessageAndAccept(BankAlert.ENTER_A_VALID_AMOUNT.getMessage());

        GetAccountTransactionsResponse getAccountTransactionsResponse = SessionStorage.getSteps().getAccountTransactions(createAccountResponse.getId());
        softly.assertThat(getAccountTransactionsResponse.getTransactions().size()).isZero();
        Account account = SessionStorage.getSteps().getCustomerAccount(createAccountResponse.getId());
        softly.assertThat(account.getBalance()).isZero();
    }

    @Test
    @UserSession
    public void userCanNotDepositWithoutSelectingAccountTest() {
        Double amount = 6000.00;
        new DepositPage().open().enterAmount(amount)
                .clickToDeposit()
                .checkAlertMessageAndAccept(BankAlert.SELECT_AN_ACCOUNT.getMessage());

        GetAccountsResponse accounts = SessionStorage.getSteps().getCustomerAccounts();
        softly.assertThat(accounts.getAccounts().size()).isZero();
    }

    @Test
    @UserSession
    public void userCanNotDepositWithEmptyValuesTest() {
        new DepositPage().open()
                .clickToDeposit()
                .checkAlertMessageAndAccept(BankAlert.SELECT_AN_ACCOUNT.getMessage());

        GetAccountsResponse accounts = SessionStorage.getSteps().getCustomerAccounts();
        softly.assertThat(accounts.getAccounts().size()).isZero();
    }
}