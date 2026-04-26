package common.utils;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.NotFoundException;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class RetryUtils {
    public static <T> T retry(
            Supplier<T> action,
            Predicate<T> condition,
            int maxAttempts,
            long delayMillis) {
        int attempts = 0;
        while (attempts < maxAttempts) {
            attempts++;
            T result = action.get();
            if (condition.test(result)) {
                return result;
            }
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Retry Failed");
    }

    public static void sendKeysRetry(
            SelenideElement element,
            String value,
            int maxAttempts,
            long delayMillis) {
        int attempts = 0;
        do {
            attempts++;
            element.shouldBe(Condition.visible, Condition.enabled).clear();
            element.sendKeys(value);
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                throw new RuntimeException("Retry Failed " + e);
            }
        }
        while (attempts < maxAttempts && !Objects.equals(element.getValue(), value));
    }

    public static void selectOptionRetry(
            SelenideElement element,
            String optionValue,
            int maxAttempts,
            long delayMillis) {
        int attempts = 0;
        boolean isExists = false;
        element.shouldBe(Condition.visible, Condition.enabled);
        while (attempts < maxAttempts && !isExists) {
            attempts++;
            isExists = element.getOptions().stream().anyMatch(option -> option.getText().contains(optionValue));
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (isExists) {
            element.selectOptionContainingText(optionValue);
        } else {
            System.out.println("Option elements " + element.getOptions().stream().map(SelenideElement::getText).toList());
            throw new NotFoundException("Option " + optionValue + " not found during " + attempts + " attempts");
        }
    }
}
