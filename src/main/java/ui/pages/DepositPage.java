package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class DepositPage extends BasePage<DepositPage> {
    private SelenideElement accountSelect = $(Selectors.byTagName("select"));
    private SelenideElement amountInput = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private SelenideElement depositButton = $(Selectors.byXpath("//button[contains(text(),'Deposit')]"));

    @Override
    public String url() {
        return "/deposit";
    }

    public DepositPage performDeposit(String accountNumber, double amount) {
        accountSelect.selectOptionContainingText(accountNumber);
        amountInput.shouldBe(Condition.visible, Condition.enabled).clear();
        amountInput.sendKeys(String.valueOf(amount));
        depositButton.click();
        return this;
    }

    public DepositPage selectAccount(String accountNumber) {
        accountSelect.selectOptionContainingText(accountNumber);
        return this;
    }
    public DepositPage enterAmount(double amount) {
        amountInput.shouldBe(Condition.visible, Condition.enabled).clear();
        amountInput.sendKeys(String.valueOf(amount));
        return this;
    }

    public DepositPage clickToDeposit() {
        depositButton.click();
        return this;
    }
}
