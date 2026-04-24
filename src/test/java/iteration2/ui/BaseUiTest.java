package iteration2.ui;

import api.configs.Config;
import com.codeborne.selenide.Configuration;
import common.extensions.BrowserMatchExtension;
import common.extensions.EnvironmentMatchExtension;
import common.extensions.UserSessionExtension;
import iteration2.BaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;


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
    }
}
