package ui.pages;

import api.models.admin.CreateUserRequest;
import api.specs.RequestSpecs;
import com.codeborne.selenide.*;
import common.helpers.StepLogger;
import common.utils.RetryUtils;
import lombok.Getter;
import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ui.elements.BaseElement;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

@Getter
public abstract class BasePage<T extends BasePage> {
  protected SelenideElement usernameInput = $(Selectors.byAttribute("placeholder", "Username"));
  protected SelenideElement passwordInput = $(Selectors.byAttribute("placeholder", "Password"));
  protected SelenideElement nameInHeader = $(Selectors.byClassName("user-name"));

  public abstract String url();

  public T open() {
    int maxAttempts = 3;
    for (int i = 0; i < maxAttempts; i++) {
      try {
        return StepLogger.logWithScreen("Opening page " + url(), () -> Selenide.open(url(), (Class<T>) this.getClass()));
      } catch (TimeoutException e) {
        System.out.println("Retry opening page, attempt " + (i + 1));
        Selenide.sleep(2000);
      }
    }
    throw new RuntimeException("Retry Failed");
  }

  public <T extends BasePage> T getPage(Class<T> pageClass) {
    return Selenide.page(pageClass);
  }

  public static void authAsUser(String username, String password) {
    StepLogger.log("Login as user " + username, () -> {
      Selenide.open("/");
      executeJavaScript("localStorage.clear();");
      String userAuthHeader = RequestSpecs.getUserAuthHeader(username, password);
      executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
    });
  }

  public static void authAsUser(CreateUserRequest createUserRequest) {
    authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword());
  }

  public T checkAlertMessageAndAccept(String bankAlert) {
    return StepLogger.logWithScreen("Checking and accepting alert message: " + bankAlert, () -> {
      Alert alert = switchTo().alert();
      assertThat(alert.getText()).contains(bankAlert);
      alert.accept();
      return (T) this;
    });
  }

  public T goHome() {
    return StepLogger.logWithScreen("Going to the main page", () -> {
      $(Selectors.byXpath("//*[contains(text(),'Home')]")).click();
      return (T) this;
    });
  }

  public T refresh() {
    return StepLogger.logWithScreen("Refresh page", () -> {
      Selenide.refresh();
      return (T) this;
    });
  }

  protected <T extends BaseElement> List<T> generatePageElements(
      ElementsCollection elementsCollection, Function<SelenideElement, T> constructor) {
    return elementsCollection.stream().map(constructor).toList();
  }

  public void sendKeys(SelenideElement element, String keysToSend) {
    StepLogger.logWithScreen("Sending " + keysToSend + " to input", () -> {
      element.shouldBe(Condition.visible, Condition.enabled).clear();
      element.sendKeys(String.valueOf(keysToSend));
    });
  }

  public void clickWithRetry(SelenideElement button) {
    RetryUtils.retry("Clicking to button with alert expecting",
        () -> {
          button.click();
          try {
            WebDriverWait wait =
                new WebDriverWait(WebDriverRunner.getWebDriver(), Duration.ofMillis(1000));
            wait.until(ExpectedConditions.alertIsPresent());
            return true;
          } catch (NoAlertPresentException | TimeoutException e) {
            return false;
          }
        },
        obj -> obj.equals(true),
        3,
        1000);
  }
}
