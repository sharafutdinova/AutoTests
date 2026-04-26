package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import common.utils.RetryUtils;
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
        RetryUtils.selectOptionRetry(accountSelect, accountNumber, 3, 1000);
        sendKeys(amountInput, String.valueOf(amount));
        depositButton.shouldBe(Condition.enabled).click();
        return this;
    }

    public DepositPage selectAccount(String accountNumber) {
        RetryUtils.selectOptionRetry(accountSelect, accountNumber, 3, 1000);
        return this;
    }

    public DepositPage enterAmount(double amount) {
        sendKeys(amountInput, String.valueOf(amount));
        return this;
    }

    public DepositPage clickToDeposit() {
        depositButton.shouldBe(Condition.enabled).click();
        return this;
    }
}
