package iteration2.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import api.generators.RandomData;
import api.models.customer.GetUserResponse;
import org.junit.jupiter.api.Test;
import api.requests.steps.UserSteps;
import ui.pages.BankAlert;
import ui.pages.EditProfilePage;
import ui.pages.UserDashboard;

import static com.codeborne.selenide.Condition.visible;

public class ChangeUserNameUiTest extends BaseUiTest {
    @Test
    public void userCanChangeNameTest() {
        String newName = RandomData.getName();
        authAsUser(userRequest);
        new EditProfilePage().open()
                .changeName(newName)
                .checkAlertMessageAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage());

        Selenide.refresh();
        new EditProfilePage().getNameInHeader().shouldHave(Condition.text(newName));
        new EditProfilePage().goHome()
                .getPage(UserDashboard.class).getWelcomeText()
                .shouldBe(visible)
                .shouldHave(Condition.text("Welcome, " + newName + "!"));

        GetUserResponse getUserResponse = UserSteps.getUserResponse(userRequest);
        softly.assertThat(getUserResponse.getName()).isEqualTo(newName);
    }

    @Test
    public void userCanNotChangeNameToInvalidValueTest() {
        String newName = RandomData.getUsername();
        authAsUser(userRequest);
        new UserDashboard().open().getNameInHeader().click();
        String nameBefore = new EditProfilePage().getNameInHeader().getText();
        new EditProfilePage().open()
                .changeName(newName)
                .checkAlertMessageAndAccept(BankAlert.NAME_MUST_CONTAIN_TWO_WORDS.getMessage());

        Selenide.refresh();
        new EditProfilePage().getNameInHeader().shouldHave(Condition.text(nameBefore));
        new EditProfilePage().goHome()
                .getPage(UserDashboard.class).getWelcomeText()
                .shouldBe(visible)
                .shouldHave(Condition.text("Welcome, " + nameBefore + "!"));

        GetUserResponse getUserResponse = UserSteps.getUserResponse(userRequest);
        softly.assertThat(getUserResponse.getName()).isEqualTo(null);
    }

    @Test
    public void userCanNotChangeNameToEmptyValueTest() {
        String newName ="";
        authAsUser(userRequest);
        new UserDashboard().open().getNameInHeader().click();
        String nameBefore = new EditProfilePage().getNameInHeader().getText();
        new EditProfilePage()
                .changeName(newName)
                .checkAlertMessageAndAccept(BankAlert.ENTER_VALID_NAME.getMessage());

        Selenide.refresh();
        new EditProfilePage().getNameInHeader().shouldHave(Condition.text(nameBefore));
        new EditProfilePage().goHome()
                .getPage(UserDashboard.class).getWelcomeText()
                .shouldBe(visible)
                .shouldHave(Condition.text("Welcome, " + nameBefore + "!"));

        GetUserResponse getUserResponse = UserSteps.getUserResponse(userRequest);
        softly.assertThat(getUserResponse.getName()).isEqualTo(null);
    }
}