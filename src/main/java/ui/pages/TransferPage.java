package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class TransferPage extends BasePage<TransferPage> {
    private SelenideElement accountSelect = $(Selectors.byTagName("select"));
    private SelenideElement recipientNameInput = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
    private SelenideElement accountNumberInput = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
    private SelenideElement amountInput = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private SelenideElement transferButton = $(Selectors.byXpath("//button[contains(text(),'Send Transfer')]"));
    private SelenideElement confirmCheckbox = $(Selectors.byId("confirmCheck"));

    @Override
    public String url() {
        return "/transfer";
    }

    public TransferPage performTransfer(String senderAccount, String recipientName, String recipientAccount, double amount) {
        accountSelect.selectOptionContainingText(senderAccount);
        recipientNameInput.shouldBe(Condition.enabled).clear();
        recipientNameInput.sendKeys(recipientName);
        accountNumberInput.shouldBe(Condition.enabled).clear();
        accountNumberInput.sendKeys(recipientAccount);
        amountInput.shouldBe(Condition.enabled).clear();
        amountInput.sendKeys(String.valueOf(amount));
        confirmCheckbox.click();
        transferButton.click();
        return this;
    }

    public TransferPage performTransfer(String senderAccount, String recipientAccount, double amount) {
        accountSelect.selectOptionContainingText(senderAccount);
        accountNumberInput.shouldBe(Condition.enabled).clear();
        accountNumberInput.sendKeys(recipientAccount);
        amountInput.shouldBe(Condition.enabled).clear();
        amountInput.sendKeys(String.valueOf(amount));
        confirmCheckbox.click();
        transferButton.click();
        return this;
    }

    public TransferPage selectAccount(String accountNumber) {
        accountSelect.selectOptionContainingText(accountNumber);
        return this;
    }

    public TransferPage enterRecipientName(String recipientName) {
        recipientNameInput.shouldBe(Condition.enabled).clear();
        recipientNameInput.sendKeys(recipientName);
        return this;
    }

    public TransferPage enterRecipientAccount(String recipientAccount) {
        accountNumberInput.shouldBe(Condition.enabled).clear();
        accountNumberInput.sendKeys(recipientAccount);
        return this;
    }

    public TransferPage enterAmount(double amount) {
        amountInput.shouldBe(Condition.enabled).clear();
        amountInput.sendKeys(String.valueOf(amount));
        return this;
    }

    public TransferPage confirm() {
        confirmCheckbox.click();
        return this;
    }

    public TransferPage clickToTransfer() {
        transferButton.click();
        return this;
    }
}
