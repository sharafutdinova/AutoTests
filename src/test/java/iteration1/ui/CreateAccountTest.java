package iteration1.ui;

import static org.assertj.core.api.Assertions.assertThat;

import api.models.accounts.CreateAccountResponse;
import baseTests.BaseUiTest;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import java.util.List;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

public class CreateAccountTest extends BaseUiTest {
  @Test
  @UserSession
  public void userCanCreateAccountTest() {
    new UserDashboard().open().createNewAccount();

    List<CreateAccountResponse> createdAccounts = SessionStorage.getSteps().getCustomerAccounts();

    assertThat(createdAccounts).hasSize(1);

    new UserDashboard()
        .checkAlertMessageAndAccept(
            BankAlert.NEW_ACCOUNT_CREATED.getMessage()
                + createdAccounts.getFirst().getAccountNumber());

    assertThat(createdAccounts.getFirst().getBalance()).isZero();
  }
}
