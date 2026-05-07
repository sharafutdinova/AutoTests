package common.utils;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.ex.ElementNotFound;
import common.helpers.StepLogger;
import org.openqa.selenium.NotFoundException;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class RetryUtils {
  public static <T> T retry(
      String title, Supplier<T> action, Predicate<T> condition, int maxAttempts, long delayMillis) {
    int attempts = 0;
    while (attempts < maxAttempts) {
      attempts++;
      try {
        T result = StepLogger.log("Attempt " + attempts + ": " + title, action::get);

        if (condition.test(result)) {
          return result;
        }
      } catch (Throwable e) {
        System.out.println("Exception " + e.getMessage());
      }
      Selenide.sleep(delayMillis);
    }
    throw new RuntimeException("Retry Failed");
  }

  public static void sendKeysRetry(
      SelenideElement element, String value, int maxAttempts, long delayMillis) {
    int attempts = 0;
    do {
      attempts++;
      element.shouldBe(Condition.visible, Condition.enabled).clear();
      element.sendKeys(value);
      Selenide.sleep(delayMillis);
    } while (attempts < maxAttempts && !Objects.equals(element.getValue(), value));
  }

  public static void selectOptionRetry(
      SelenideElement element, String optionValue, int maxAttempts, long delayMillis) {
    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      try {
        // Пытаемся выбрать опцию, содержащую заданный текст
        element.selectOptionContainingText(optionValue);
        return;
      } catch (ElementNotFound | RuntimeException e) {
        if (attempt == maxAttempts) {
          throw new NotFoundException(
              String.format(
                  "Option '%s' not found after %d attempts and page refreshes.",
                  optionValue, maxAttempts),
              e);
        }
        Selenide.refresh();
        element.shouldBe(Condition.visible, Condition.enabled);
        Selenide.sleep(delayMillis);
      }
    }
  }
}
