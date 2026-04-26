package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import common.utils.RetryUtils;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class EditProfilePage extends BasePage<EditProfilePage> {
    private SelenideElement newNameInput = $(Selectors.byAttribute("placeholder", "Enter new name"));
    private SelenideElement saveButton = $(Selectors.byCssSelector("[placeholder='Enter new name'] ~ button"));

    @Override
    public String url() {
        return "/edit-profile";
    }

    public EditProfilePage changeName(String newName) {
        RetryUtils.sendKeysRetry(newNameInput, newName, 3, 500);
        saveButton.shouldBe(Condition.enabled).click();
        return this;
    }
}
