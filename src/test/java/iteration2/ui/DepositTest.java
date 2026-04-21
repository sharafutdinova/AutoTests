package iteration2.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import generators.RandomData;
import models.Account;
import models.TransactionTypes;
import models.accounts.CreateAccountResponse;
import models.accounts.GetAccountTransactionsResponse;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.steps.UserSteps;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DepositTest extends BaseTest {
    @Test
    public void userCanDepositTest() {
        CreateAccountResponse createAccountResponse = UserSteps.createAccount(userRequest);
        $(Selectors.byXpath("//*[contains(text(),'Deposit Money')]")).click();

        $(Selectors.byTagName("select")).click();
        $(Selectors.byTagName("select")).selectOptionContainingText(createAccountResponse.getAccountNumber());

        Double amount = RandomData.getDepositAmount();
        SelenideElement input = $(Selectors.byAttribute("placeholder", "Enter amount"));
        input.shouldBe(Condition.visible, Condition.enabled).clear();
        input.sendKeys(amount.toString());
        input.shouldHave(Condition.value(amount.toString()));
        $(Selectors.byXpath("//button[contains(text(),'Deposit')]")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("✅ Successfully deposited $" + amount + " to account " + createAccountResponse.getAccountNumber() + "!");
        alert.accept();
        $(Selectors.byClassName("welcome-text")).shouldBe(visible).shouldHave(Condition.text("Welcome, noname!"));

        GetAccountTransactionsResponse getAccountTransactionsResponse = UserSteps.getAccountTransactions(userRequest, createAccountResponse.getId());
        softly.assertThat(getAccountTransactionsResponse.getTransactions().size()).isEqualTo(1);
        softly.assertThat(getAccountTransactionsResponse.getTransactions().getFirst().getAmount()).isEqualTo(amount);
        softly.assertThat(getAccountTransactionsResponse.getTransactions().getFirst().getType()).isEqualTo(TransactionTypes.TRANSACTION_TYPE_FOR_DEPOSIT.getDescription());

        Account account = UserSteps.getCustomerAccount(userRequest, createAccountResponse.getId());
        softly.assertThat(account.getBalance()).isEqualTo(amount);
    }

    @Test
    public void userCanNotDepositInvalidValueTest() {
        CreateAccountResponse createAccountResponse = UserSteps.createAccount(userRequest);
        $(Selectors.byXpath("//*[contains(text(),'Deposit Money')]")).click();

        $(Selectors.byTagName("select")).click();
        $(Selectors.byTagName("select")).selectOptionContainingText(createAccountResponse.getAccountNumber());

        Double amount = 6000.00;
        SelenideElement input = $(Selectors.byAttribute("placeholder", "Enter amount"));
        input.shouldBe(Condition.visible, Condition.enabled).clear();
        input.sendKeys(amount.toString());
        input.shouldHave(Condition.value(amount.toString()));
        $(Selectors.byXpath("//button[contains(text(),'Deposit')]")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("❌ Please deposit less or equal to 5000$.");
        alert.accept();

        GetAccountTransactionsResponse getAccountTransactionsResponse = UserSteps.getAccountTransactions(userRequest, createAccountResponse.getId());
        softly.assertThat(getAccountTransactionsResponse.getTransactions().size()).isZero();
        Account account = UserSteps.getCustomerAccount(userRequest, createAccountResponse.getId());
        softly.assertThat(account.getBalance()).isZero();
    }

    @Test
    public void userCanNotDepositWithoutEnteringAmountTest() {
        CreateAccountResponse createAccountResponse = UserSteps.createAccount(userRequest);
        $(Selectors.byXpath("//*[contains(text(),'Deposit Money')]")).click();
        $(Selectors.byTagName("select")).click();
        $(Selectors.byTagName("select")).selectOptionContainingText(createAccountResponse.getAccountNumber());
        $(Selectors.byXpath("//button[contains(text(),'Deposit')]")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("❌ Please enter a valid amount.");
        alert.accept();

        GetAccountTransactionsResponse getAccountTransactionsResponse = UserSteps.getAccountTransactions(userRequest, createAccountResponse.getId());
        softly.assertThat(getAccountTransactionsResponse.getTransactions().size()).isZero();
        Account account = UserSteps.getCustomerAccount(userRequest, createAccountResponse.getId());
        softly.assertThat(account.getBalance()).isZero();
    }

    @Test
    public void userCanNotDepositWithoutSelectingAccountTest() {
        CreateAccountResponse createAccountResponse = UserSteps.createAccount(userRequest);
        $(Selectors.byXpath("//*[contains(text(),'Deposit Money')]")).click();

        Double amount = 6000.00;
        SelenideElement input = $(Selectors.byAttribute("placeholder", "Enter amount"));
        input.shouldBe(Condition.visible, Condition.enabled).clear();
        input.sendKeys(amount.toString());
        input.shouldHave(Condition.value(amount.toString()));
        $(Selectors.byXpath("//button[contains(text(),'Deposit')]")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("❌ Please select an account.");
        alert.accept();

        GetAccountTransactionsResponse getAccountTransactionsResponse = UserSteps.getAccountTransactions(userRequest, createAccountResponse.getId());
        softly.assertThat(getAccountTransactionsResponse.getTransactions().size()).isZero();
        Account account = UserSteps.getCustomerAccount(userRequest, createAccountResponse.getId());
        softly.assertThat(account.getBalance()).isZero();
    }

    @Test
    public void userCanNotDepositWithEmptyValuesTest() {
        CreateAccountResponse createAccountResponse = UserSteps.createAccount(userRequest);
        $(Selectors.byXpath("//*[contains(text(),'Deposit Money')]")).click();

        $(Selectors.byXpath("//button[contains(text(),'Deposit')]")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("❌ Please select an account.");
        alert.accept();

        GetAccountTransactionsResponse getAccountTransactionsResponse = UserSteps.getAccountTransactions(userRequest, createAccountResponse.getId());
        softly.assertThat(getAccountTransactionsResponse.getTransactions().size()).isZero();
        Account account = UserSteps.getCustomerAccount(userRequest, createAccountResponse.getId());
        softly.assertThat(account.getBalance()).isZero();
    }
}