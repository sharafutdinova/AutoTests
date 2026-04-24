package iteration1.ui;

import api.models.customer.GetAccountsResponse;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUiTest {
    @Test
    @UserSession
    public void userCanCreateAccountTest() {
        new UserDashboard().open().createNewAccount();

        GetAccountsResponse createdAccounts = SessionStorage.getSteps()
                .getCustomerAccounts();

        assertThat(createdAccounts.getAccounts()).hasSize(1);

        new UserDashboard().checkAlertMessageAndAccept
                (BankAlert.NEW_ACCOUNT_CREATED.getMessage() + createdAccounts.getAccounts().getFirst().getAccountNumber());

        assertThat(createdAccounts.getAccounts().getFirst().getBalance()).isZero();
    }
}