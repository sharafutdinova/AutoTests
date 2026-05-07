package baseTests;

import api.configs.Config;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.logevents.SelenideLogger;
import common.extensions.AdminSessionExtension;
import common.extensions.BrowserMatchExtension;
import common.extensions.EnvironmentMatchExtension;
import common.extensions.UserSessionExtension;
import io.qameta.allure.Attachment;
import io.qameta.allure.selenide.AllureSelenide;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Map;

@ExtendWith(AdminSessionExtension.class)
@ExtendWith(UserSessionExtension.class)
@ExtendWith(BrowserMatchExtension.class)
@ExtendWith(EnvironmentMatchExtension.class)
public class BaseUiTest extends BaseTest {
  @BeforeAll
  public static void setupSelenoid() {
    Configuration.remote = Config.getProperty("uiRemote");
    Configuration.baseUrl = Config.getProperty("uiBaseUrl");
    Configuration.browser = Config.getProperty("browser");
    Configuration.browserSize = Config.getProperty("browserSize");
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
    options.setCapability(
        "selenoid:options",
        Map.of(
            "enableVNC", true,
            "enableLog", true));
    options.setCapability("unhandledPromptBehavior", "dismiss");
    Configuration.browserCapabilities = options;
    Configuration.pageLoadTimeout = 120000; // таймаут загрузки страницы
    Configuration.timeout = 10000; // таймаут ожидания элементов (по умолчанию 4000)
    Configuration.pollingInterval = 500; // интервал опроса (по умолчанию 100 мс)
    Configuration.pageLoadStrategy = "eager"; // ждёт только DOM, не ждёт ресурсы
    Configuration.headless = true;
    Configuration.reopenBrowserOnFail = true;
    SelenideLogger.addListener("AllureSelenide", new AllureSelenide()
        .screenshots(true)
        .savePageSource(false)
        .includeSelenideSteps(true));
  }
}
