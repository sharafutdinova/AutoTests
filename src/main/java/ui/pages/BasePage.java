package ui.pages;

import api.models.admin.CreateUserRequest;
import api.specs.RequestSpecs;
import com.codeborne.selenide.*;
import lombok.Getter;
import org.openqa.selenium.Alert;
import org.openqa.selenium.TimeoutException;
import ui.elements.BaseElement;

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
                return Selenide.open(url(), (Class<T>) this.getClass());
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
        Selenide.open("/");
        executeJavaScript("localStorage.clear();");
        String userAuthHeader = RequestSpecs.getUserAuthHeader(username, password);
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
    }

    public static void authAsUser(CreateUserRequest createUserRequest) {
        authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword());
    }

    public T checkAlertMessageAndAccept(String bankAlert) {
        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains(bankAlert);
        alert.accept();
        return (T) this;
    }

    public T goHome() {
        $(Selectors.byXpath("//*[contains(text(),'Home')]")).click();
        return (T) this;
    }

    protected <T extends BaseElement> List<T> generatePageElements(ElementsCollection elementsCollection, Function<SelenideElement, T> constructor) {
        return elementsCollection.stream().map(constructor).toList();
    }

    public void sendKeys(SelenideElement element, String keysToSend) {
        element.shouldBe(Condition.visible, Condition.enabled).clear();
        element.sendKeys(String.valueOf(keysToSend));
    }
}
