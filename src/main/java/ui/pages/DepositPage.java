package ui.pages;

import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import common.utils.RetryUtils;
import lombok.Getter;

@Getter
public class DepositPage extends BasePage<DepositPage> {
  private SelenideElement accountSelect = $(Selectors.byTagName("select"));
  private SelenideElement amountInput = $(Selectors.byAttribute("placeholder", "Enter amount"));
  private SelenideElement depositButton =
      $(Selectors.byXpath("//button[contains(text(),'Deposit')]"));

  @Override
  public String url() {
    return "/deposit";
  }

  public DepositPage performDeposit(String accountNumber, double amount) {
    RetryUtils.selectOptionRetry(accountSelect, accountNumber, 3, 1000);
    RetryUtils.sendKeysRetry(amountInput, String.valueOf(amount), 3, 500);
    clickWithRetry(depositButton);
    return this;
  }

  public DepositPage selectAccount(String accountNumber) {
    RetryUtils.selectOptionRetry(accountSelect, accountNumber, 3, 1000);
    return this;
  }

  public DepositPage enterAmount(double amount) {
    RetryUtils.sendKeysRetry(amountInput, String.valueOf(amount), 3, 500);
    return this;
  }

  public DepositPage clickToDeposit() {
    clickWithRetry(depositButton);
    return this;
  }
}
