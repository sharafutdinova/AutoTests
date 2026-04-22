package iteration1.ui;

import api.models.customer.GetAccountsResponse;
import api.requests.steps.UserSteps;
import api.models.admin.CreateUserRequest;
import org.junit.jupiter.api.Test;
import api.requests.steps.AdminSteps;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUiTest {
    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);

        new UserDashboard().open().createNewAccount();

        GetAccountsResponse createdAccounts = UserSteps
                .getCustomerAccounts(user);

        assertThat(createdAccounts.getAccounts()).hasSize(1);

        new UserDashboard().checkAlertMessageAndAccept
                (BankAlert.NEW_ACCOUNT_CREATED.getMessage() + createdAccounts.getAccounts().getFirst().getAccountNumber());

        assertThat(createdAccounts.getAccounts().getFirst().getBalance()).isZero();
    }
}