package iteration2.ui;

import static com.codeborne.selenide.Condition.visible;

import api.generators.RandomData;
import api.models.customer.GetUserResponse;
import baseTests.BaseUiTest;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import common.annotations.Environments;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.EditProfilePage;
import ui.pages.UserDashboard;

public class ChangeUserNameUiTest extends BaseUiTest {
  @Test
  @UserSession
  @Environments({"1920x1080"})
  public void userCanChangeNameTest() {
    String newName = RandomData.getName();

    new EditProfilePage()
        .open()
        .changeName(newName)
        .checkAlertMessageAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage());

    Selenide.refresh();
    new EditProfilePage().getNameInHeader().shouldHave(Condition.text(newName));
    new EditProfilePage()
        .goHome()
        .getPage(UserDashboard.class)
        .getWelcomeText()
        .shouldBe(visible)
        .shouldHave(Condition.text("Welcome, " + newName + "!"));

    GetUserResponse getUserResponse = SessionStorage.getSteps().getUserResponse();
    softly.assertThat(getUserResponse.getName()).isEqualTo(newName);
  }

  @Test
  @UserSession
  public void userCanNotChangeNameToInvalidValueTest() {
    String newName = RandomData.getUsername();
    new UserDashboard().open().getNameInHeader().click();
    String nameBefore = new EditProfilePage().getNameInHeader().getText();
    new EditProfilePage()
        .open()
        .changeName(newName)
        .checkAlertMessageAndAccept(BankAlert.NAME_MUST_CONTAIN_TWO_WORDS.getMessage());

    Selenide.refresh();
    new EditProfilePage().getNameInHeader().shouldHave(Condition.text(nameBefore));
    new EditProfilePage()
        .goHome()
        .getPage(UserDashboard.class)
        .getWelcomeText()
        .shouldBe(visible)
        .shouldHave(Condition.text("Welcome, " + nameBefore + "!"));

    GetUserResponse getUserResponse = SessionStorage.getSteps().getUserResponse();
    softly.assertThat(getUserResponse.getName()).isEqualTo(null);
  }

  @Test
  @UserSession
  public void userCanNotChangeNameToEmptyValueTest() {
    String newName = "";
    new UserDashboard().open().getNameInHeader().click();
    String nameBefore = new EditProfilePage().getNameInHeader().getText();
    new EditProfilePage()
        .changeName(newName)
        .checkAlertMessageAndAccept(BankAlert.ENTER_VALID_NAME.getMessage());

    Selenide.refresh();
    new EditProfilePage().getNameInHeader().shouldHave(Condition.text(nameBefore));
    new EditProfilePage()
        .goHome()
        .getPage(UserDashboard.class)
        .getWelcomeText()
        .shouldBe(visible)
        .shouldHave(Condition.text("Welcome, " + nameBefore + "!"));

    GetUserResponse getUserResponse = SessionStorage.getSteps().getUserResponse();
    softly.assertThat(getUserResponse.getName()).isEqualTo(null);
  }
}
