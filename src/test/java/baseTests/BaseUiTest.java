package baseTests;

import api.configs.Config;
import com.codeborne.selenide.Configuration;
import common.extensions.AdminSessionExtension;
import common.extensions.BrowserMatchExtension;
import common.extensions.EnvironmentMatchExtension;
import common.extensions.UserSessionExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
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
        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true)
        );
        Configuration.pageLoadTimeout = 60000; // таймаут загрузки страницы
        Configuration.timeout = 10000; // таймаут ожидания элементов (по умолчанию 4000)
        Configuration.pollingInterval = 500; // интервал опроса (по умолчанию 100 мс)
        Configuration.pageLoadStrategy = "eager"; // ждёт только DOM, не ждёт ресурсы
        ChromeOptions options = new ChromeOptions();
        options.setCapability("unhandledPromptBehavior", "dismiss");
        Configuration.browserCapabilities = options;
    }
}
