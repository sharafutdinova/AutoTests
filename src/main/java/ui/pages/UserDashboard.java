package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class UserDashboard extends BasePage<UserDashboard> {
    private SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
    private SelenideElement createNewAccount = $(Selectors.byText("➕ Create New Account"));
    private SelenideElement depositButton = $(Selectors.byXpath("//*[contains(text(),'Deposit Money')]"));
    private SelenideElement transferButton = $(Selectors.byXpath("//*[contains(text(),'Make a Transfer')]"));

    @Override
    public String url() {
        return "/dashboard";
    }

    public UserDashboard createNewAccount() {
        createNewAccount.click();
        return this;
    }

    public UserDashboard goToDepositPage() {
        depositButton.click();
        return this;
    }

    public UserDashboard goToTransferPage() {
        transferButton.click();
        return this;
    }
}
