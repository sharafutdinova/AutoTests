package common.utils;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class RetryUtils {
    public static <T> T retry(
            Supplier<T> action,
            Predicate<T> condition,
            int maxAttempts,
            long delayMillis) {
        T result = null;
        int attempts = 0;
        while (attempts < maxAttempts) {
            attempts++;
            result = action.get();
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
            Optional<Integer> optionalMaxAttempts,
            Optional<Long> optionalDelayMillis) {
        int maxAttempts = optionalMaxAttempts.orElse(3);
        long delayMillis = optionalDelayMillis.orElse(500L);
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
        while (attempts < maxAttempts && !element.getValue().equals(value));
        System.out.println(attempts);
    }
}
