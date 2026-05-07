package ui.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import common.helpers.StepLogger;
import common.utils.RetryUtils;
import lombok.Getter;
import ui.elements.UserBage;

import java.util.List;
import java.util.Objects;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class AdminPanel extends BasePage<AdminPanel> {
  private SelenideElement adminPanelText = $(Selectors.byText("Admin Panel"));
  private SelenideElement addUserButton = $(Selectors.byText("Add User"));

  @Override
  public String url() {
    return "/admin";
  }

  public AdminPanel createUser(String username, String password) {
    return StepLogger.log("Creating user " + username + " from admin panel ", () -> {
      sendKeys(usernameInput, username);
      sendKeys(passwordInput, password);
      addUserButton.click();
      return this;
    });
  }

  public List<UserBage> getAllUsers() {
    return StepLogger.log("Get all users from Dashboard", () -> {
      ElementsCollection elementsCollection = $(Selectors.byText("All Users")).parent().findAll("li");
      return generatePageElements(elementsCollection, UserBage::new);
    });
  }

  public UserBage findUserByUsername(String username) {
    return RetryUtils.retry("Find user by username " + username,
        () ->
            getAllUsers().stream()
                .filter(it -> it.getUsername().equals(username))
                .findAny()
                .orElse(null),
        Objects::nonNull,
        3,
        1000);
  }
}
