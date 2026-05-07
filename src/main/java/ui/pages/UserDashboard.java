package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import common.helpers.StepLogger;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class UserDashboard extends BasePage<UserDashboard> {
  private SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
  private SelenideElement createNewAccount = $(Selectors.byText("➕ Create New Account"));
  private SelenideElement depositButton =
      $(Selectors.byXpath("//*[contains(text(),'Deposit Money')]"));
  private SelenideElement transferButton =
      $(Selectors.byXpath("//*[contains(text(),'Make a Transfer')]"));

  @Override
  public String url() {
    return "/dashboard";
  }

  public UserDashboard createNewAccount() {
    return StepLogger.logWithScreen("Creating new account", () -> {
      clickWithRetry(createNewAccount);
      return this;
    });
  }

  public UserDashboard goToDepositPage() {
    return StepLogger.logWithScreen("Going to deposit page", () -> {
      depositButton.click();
      return this;
    });
  }

  public UserDashboard goToTransferPage() {
    return StepLogger.logWithScreen("Going to transfer page", () -> {
      transferButton.click();
      return this;
    });
  }
}
