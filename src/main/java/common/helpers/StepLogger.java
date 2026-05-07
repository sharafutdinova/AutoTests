package common.helpers;

import com.codeborne.selenide.Selenide;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;

import java.io.ByteArrayInputStream;

/*
Example of usage:

StepLogger.log("Get all users", () -> {

 click() ->  click log
 post() -> post log

 }

 "Get all users" ->
       "click log"
       "post log"

*/
public class StepLogger {
  @FunctionalInterface
  public interface ThrowableRunnable<T> {
    T run() throws Throwable;
  }

  @FunctionalInterface
  public interface ThrowableVoidRunnable {
    void run() throws Throwable;
  }

  public static <T> T log(String title, ThrowableRunnable<T> runnable) {
    return Allure.step(title, () -> runnable.run());
  }

  public static void log(String title, ThrowableVoidRunnable runnable) {
    Allure.step(
        title,
        () -> {
          runnable.run();
          return null;
        });
  }

  public static <T> T logWithScreen(String title, ThrowableRunnable<T> runnable) {
    return Allure.step(title, () -> {
      try {
        T result = runnable.run();
        byte[] screenshot = Selenide.screenshot(OutputType.BYTES);
        Allure.addAttachment(title, "image/png", new ByteArrayInputStream(screenshot), "png");
        return result;
      } catch (Throwable e) {
        System.out.println("Error " + e.getMessage());
        byte[] screenshot = Selenide.screenshot(OutputType.BYTES);
        Allure.addAttachment(title, "image/png", new ByteArrayInputStream(screenshot), "png");
        return null;
      }
    });
  }

  public static void logWithScreen(String title, ThrowableVoidRunnable runnable) {
    Allure.step(title, () -> {
      try {
        runnable.run();
      } catch (Throwable e) {
        System.out.println("Error " + e.getMessage());
      } finally {
        byte[] screenshot = Selenide.screenshot(OutputType.BYTES);
        Allure.addAttachment(title, "image/png", new ByteArrayInputStream(screenshot), "png");
      }
    });
  }
}
