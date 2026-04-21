package iteration2.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import generators.RandomData;
import models.customer.GetUserResponse;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.steps.UserSteps;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ChangeUserNameTest extends BaseTest {
    @Test
    public void userCanChangeNameTest() {
        $(Selectors.byClassName("welcome-text")).shouldBe(visible).shouldHave(Condition.text("Welcome, noname!"));
        $(Selectors.byClassName("user-name")).click();

        String newName = RandomData.getName();
        SelenideElement input = $(Selectors.byAttribute("placeholder", "Enter new name"));
        input.shouldBe(Condition.visible, Condition.enabled).clear();
        sleep(500);
        input.sendKeys(newName);
        input.shouldHave(Condition.value(newName));
        $(Selectors.byCssSelector("[placeholder='Enter new name'] ~ button")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("✅ Name updated successfully!");
        alert.accept();

        Selenide.refresh();
        $(Selectors.byClassName("user-name")).shouldHave(Condition.text(newName));
        $(Selectors.byXpath("//*[contains(text(),'Home')]")).click();
        $(Selectors.byClassName("welcome-text")).shouldBe(visible).shouldHave(Condition.text("Welcome, " + newName + "!"));

        GetUserResponse getUserResponse = UserSteps.getUserResponse(userRequest);
        softly.assertThat(getUserResponse.getName()).isEqualTo(newName);
    }

    @Test
    public void userCanNotChangeNameToInvalidValueTest() {
        String nameInHeaderBefore = $(Selectors.byClassName("user-name")).getText();
        String nameOnMainPageBefore = $(Selectors.byCssSelector(".welcome-text span")).getText();
        $(Selectors.byClassName("welcome-text")).shouldBe(visible).shouldHave(Condition.text("Welcome, noname!"));
        $(Selectors.byClassName("user-name")).click();

        String newName = RandomData.getUsername();
        SelenideElement input = $(Selectors.byAttribute("placeholder", "Enter new name"));
        input.shouldBe(Condition.visible, Condition.enabled).clear();
        sleep(500);
        input.sendKeys(newName);
        input.shouldHave(Condition.value(newName));
        $(Selectors.byCssSelector("[placeholder='Enter new name'] ~ button")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("Name must contain two words with letters only");
        alert.accept();

        Selenide.refresh();
        $(Selectors.byClassName("user-name")).shouldHave(Condition.text(nameInHeaderBefore));
        $(Selectors.byXpath("//*[contains(text(),'Home')]")).click();
        $(Selectors.byClassName("welcome-text")).shouldBe(visible).shouldHave(Condition.text("Welcome, " + nameOnMainPageBefore + "!"));

        GetUserResponse getUserResponse = UserSteps.getUserResponse(userRequest);
        softly.assertThat(getUserResponse.getName()).isEqualTo(null);
    }

    @Test
    public void userCanNotChangeNameToEmptyValueTest() {
        String nameInHeaderBefore = $(Selectors.byClassName("user-name")).getText();
        String nameOnMainPageBefore = $(Selectors.byCssSelector(".welcome-text span")).getText();
        $(Selectors.byClassName("welcome-text")).shouldBe(visible).shouldHave(Condition.text("Welcome, noname!"));
        $(Selectors.byClassName("user-name")).click();

        String newName = "";
        SelenideElement input = $(Selectors.byAttribute("placeholder", "Enter new name"));
        input.shouldBe(Condition.visible, Condition.enabled).clear();
        input.sendKeys(newName);
        $(Selectors.byCssSelector("[placeholder='Enter new name'] ~ button")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("❌ Please enter a valid name.");
        alert.accept();

        Selenide.refresh();
        $(Selectors.byClassName("user-name")).shouldHave(Condition.text(nameInHeaderBefore));
        $(Selectors.byXpath("//*[contains(text(),'Home')]")).click();
        $(Selectors.byClassName("welcome-text")).shouldBe(visible).shouldHave(Condition.text("Welcome, " + nameOnMainPageBefore + "!"));

        GetUserResponse getUserResponse = UserSteps.getUserResponse(userRequest);
        softly.assertThat(getUserResponse.getName()).isEqualTo(null);
    }
}