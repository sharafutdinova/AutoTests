package ui.pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import common.helpers.StepLogger;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.executeJavaScript;

public class LoginPage extends BasePage<LoginPage> {
  private SelenideElement button = $("button");

  @Override
  public String url() {
    return "/login";
  }

  public LoginPage login(String username, String password) {
    return StepLogger.logWithScreen("Login with username " + username + " and password " + password, () -> {
      executeJavaScript("localStorage.clear();");
      Selenide.refresh();
      sendKeys(usernameInput, username);
      sendKeys(passwordInput, password);
      button.click();
      return this;
    });
  }
}
