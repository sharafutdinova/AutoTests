package ui.pages;

import com.codeborne.selenide.*;
import common.utils.RetryUtils;
import lombok.Getter;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

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
        RetryUtils.selectOptionRetry(accountSelect, senderAccount, 3, 1000);
        sendKeys(recipientNameInput, recipientName);
        sendKeys(accountNumberInput, recipientAccount);
        sendKeys(amountInput, String.valueOf(amount));
        confirmCheckbox.shouldBe(Condition.enabled).click();
        clickToTransferWithRetry();
        return this;
    }

    public TransferPage performTransfer(String senderAccount, String recipientAccount, double amount) {
        RetryUtils.selectOptionRetry(accountSelect, senderAccount, 3, 1000);
        sendKeys(accountNumberInput, recipientAccount);
        sendKeys(amountInput, String.valueOf(amount));
        confirmCheckbox.shouldBe(Condition.enabled).click();
        clickToTransferWithRetry();
        return this;
    }

    public TransferPage selectAccount(String accountNumber) {
        RetryUtils.selectOptionRetry(accountSelect, accountNumber, 3, 1000);
        return this;
    }

    public TransferPage enterRecipientName(String recipientName) {
        sendKeys(recipientNameInput, recipientName);
        return this;
    }

    public TransferPage enterRecipientAccount(String recipientAccount) {
        sendKeys(accountNumberInput, recipientAccount);
        return this;
    }

    public TransferPage enterAmount(double amount) {
        sendKeys(amountInput, String.valueOf(amount));
        return this;
    }

    public TransferPage confirm() {
        confirmCheckbox.shouldBe(Condition.enabled).click();
        return this;
    }

    public void clickToTransferWithRetry() {
        RetryUtils.retry(
                () -> {
                    transferButton.click();
                    try {
                        WebDriverWait wait = new WebDriverWait(WebDriverRunner.getWebDriver(), Duration.ofMillis(1000));
                        wait.until(ExpectedConditions.alertIsPresent());
                        return true;
                    } catch (NoAlertPresentException | TimeoutException e) {
                        return false;
                    }
                },
                obj -> obj.equals(true),
                3,
                1000
        );
    }

    public TransferPage clickToTransfer() {
        clickToTransferWithRetry();
        return this;
    }
}
