package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.sleep;

@Getter
public class EditProfilePage extends BasePage<EditProfilePage> {
    private SelenideElement newNameInput = $(Selectors.byAttribute("placeholder", "Enter new name"));
    private SelenideElement saveButton = $(Selectors.byCssSelector("[placeholder='Enter new name'] ~ button"));

    @Override
    public String url() {
        return "/edit-profile";
    }

    public EditProfilePage changeName(String newName) {
        newNameInput.shouldBe(Condition.visible, Condition.enabled).clear();
        sleep(500);
        newNameInput.sendKeys(newName);
        saveButton.click();
        return this;
    }
}
