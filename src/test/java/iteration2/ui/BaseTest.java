package iteration2.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import models.admin.CreateUserRequest;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Map;

import static com.codeborne.selenide.Selenide.executeJavaScript;

public class BaseTest {
    protected SoftAssertions softly;
    protected CreateUserRequest userRequest;

    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://172.20.80.1:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";
        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true)
        );
    }

    @BeforeEach
    public void setUp() {
        softly = new SoftAssertions();
        userRequest = AdminSteps.createUser();
        loginAndOpenMainPage();
    }

    @AfterEach
    public void afterTest() {
        softly.assertAll();
        AdminSteps.deleteUserByCreateUserRequest(userRequest);
    }

    public void loginAndOpenMainPage() {
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(userRequest)
                .extract()
                .header("Authorization");

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");
    }
}
