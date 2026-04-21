package iteration2.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import generators.RandomData;
import models.Account;
import models.Transaction;
import models.TransactionTypes;
import models.accounts.CreateAccountResponse;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.steps.UserSteps;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.Assertions.assertThat;

public class TransferTest extends BaseTest {
    @Test
    public void userCanTransferWithUsernameTest() {
        CreateAccountResponse createSenderAccountResponse = UserSteps.createAccount(userRequest);
        int maxAmountForDeposit = 5000;
        for (int i = 0; i < 3; i++) {
            UserSteps.deposit(userRequest, createSenderAccountResponse.getId(), maxAmountForDeposit);
        }
        Account senderAccountBefore = UserSteps.getCustomerAccount(userRequest, createSenderAccountResponse.getId());
        CreateAccountResponse createReceiverAccountResponse = UserSteps.createAccount(userRequest);

        $(Selectors.byXpath("//*[contains(text(),'Make a Transfer')]")).click();

        $(Selectors.byTagName("select")).click();
        $(Selectors.byTagName("select")).selectOptionContainingText(createSenderAccountResponse.getAccountNumber());

        SelenideElement nameInput = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
        nameInput.shouldBe(Condition.visible, Condition.enabled).clear();
        nameInput.sendKeys(userRequest.getUsername());

        SelenideElement accountInput = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
        accountInput.shouldBe(Condition.visible, Condition.enabled).clear();
        accountInput.sendKeys(createReceiverAccountResponse.getAccountNumber());

        Double amount = RandomData.getTransferAmount();
        SelenideElement input = $(Selectors.byAttribute("placeholder", "Enter amount"));
        input.shouldBe(Condition.visible, Condition.enabled).clear();
        input.sendKeys(amount.toString());

        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byXpath("//button[contains(text(),'Send Transfer')]")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("✅ Successfully transferred $" + amount + " to account " + createReceiverAccountResponse.getAccountNumber() + "!");
        alert.accept();

        Account receiverAccount = UserSteps.getCustomerAccount(userRequest, createReceiverAccountResponse.getId());
        softly.assertThat(receiverAccount.getBalance()).isEqualTo(amount);
        Account senderAccountAfter = UserSteps.getCustomerAccount(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(senderAccountAfter.getBalance()).isEqualTo(senderAccountBefore.getBalance() - amount);
        Transaction lastReceiverTransaction = UserSteps.getAccountLastTransactions(userRequest, createReceiverAccountResponse.getId());
        softly.assertThat(lastReceiverTransaction.validateTransaction(TransactionTypes.TRANSACTION_TYPE_FOR_TRANSFER_IN, amount)).isTrue();
        Transaction lastSenderTransaction = UserSteps.getAccountLastTransactions(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(lastSenderTransaction.validateTransaction(TransactionTypes.TRANSACTION_TYPE_FOR_TRANSFER_OUT, amount)).isTrue();
    }

    @Test
    public void userCanTransferWithNameAfterNameChangingTest() {
        CreateAccountResponse createSenderAccountResponse = UserSteps.createAccount(userRequest);
        int maxAmountForDeposit = 5000;
        for (int i = 0; i < 3; i++) {
            UserSteps.deposit(userRequest, createSenderAccountResponse.getId(), maxAmountForDeposit);
        }
        Account senderAccountBefore = UserSteps.getCustomerAccount(userRequest, createSenderAccountResponse.getId());
        CreateAccountResponse createReceiverAccountResponse = UserSteps.createAccount(userRequest);
        String newName = RandomData.getName();
        UserSteps.changeName(userRequest, newName);

        $(Selectors.byXpath("//*[contains(text(),'Make a Transfer')]")).click();

        $(Selectors.byTagName("select")).click();
        $(Selectors.byTagName("select")).selectOptionContainingText(createSenderAccountResponse.getAccountNumber());

        SelenideElement nameInput = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
        nameInput.shouldBe(Condition.visible, Condition.enabled).clear();
        nameInput.sendKeys(newName);

        SelenideElement accountInput = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
        accountInput.shouldBe(Condition.visible, Condition.enabled).clear();
        accountInput.sendKeys(createReceiverAccountResponse.getAccountNumber());

        Double amount = RandomData.getTransferAmount();
        SelenideElement input = $(Selectors.byAttribute("placeholder", "Enter amount"));
        input.shouldBe(Condition.visible, Condition.enabled).clear();
        input.sendKeys(amount.toString());

        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byXpath("//button[contains(text(),'Send Transfer')]")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("✅ Successfully transferred $" + amount + " to account " + createReceiverAccountResponse.getAccountNumber() + "!");
        alert.accept();

        Account receiverAccount = UserSteps.getCustomerAccount(userRequest, createReceiverAccountResponse.getId());
        softly.assertThat(receiverAccount.getBalance()).isEqualTo(amount);
        Account senderAccountAfter = UserSteps.getCustomerAccount(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(senderAccountAfter.getBalance()).isEqualTo(senderAccountBefore.getBalance() - amount);
        Transaction lastReceiverTransaction = UserSteps.getAccountLastTransactions(userRequest, createReceiverAccountResponse.getId());
        softly.assertThat(lastReceiverTransaction.validateTransaction(TransactionTypes.TRANSACTION_TYPE_FOR_TRANSFER_IN, amount)).isTrue();
        Transaction lastSenderTransaction = UserSteps.getAccountLastTransactions(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(lastSenderTransaction.validateTransaction(TransactionTypes.TRANSACTION_TYPE_FOR_TRANSFER_OUT, amount)).isTrue();
    }

    @Test
    public void userCanTransferWithoutNameIfNameWasNotChangedTest() {
        CreateAccountResponse createSenderAccountResponse = UserSteps.createAccount(userRequest);
        CreateAccountResponse createReceiverAccountResponse = UserSteps.createAccount(userRequest);
        int maxAmountForDeposit = 5000;
        UserSteps.deposit(userRequest, createSenderAccountResponse.getId(), maxAmountForDeposit);

        $(Selectors.byXpath("//*[contains(text(),'Make a Transfer')]")).click();

        $(Selectors.byTagName("select")).click();
        $(Selectors.byTagName("select")).selectOptionContainingText(createSenderAccountResponse.getAccountNumber());

        SelenideElement accountInput = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
        accountInput.shouldBe(Condition.visible, Condition.enabled).clear();
        accountInput.sendKeys(createReceiverAccountResponse.getAccountNumber());

        Double amount = RandomData.getDepositAmount();
        SelenideElement input = $(Selectors.byAttribute("placeholder", "Enter amount"));
        input.shouldBe(Condition.visible, Condition.enabled).clear();
        input.sendKeys(amount.toString());

        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byXpath("//button[contains(text(),'Send Transfer')]")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("✅ Successfully transferred $" + amount + " to account " + createReceiverAccountResponse.getAccountNumber() + "!");
        alert.accept();

        Account receiverAccount = UserSteps.getCustomerAccount(userRequest, createReceiverAccountResponse.getId());
        softly.assertThat(receiverAccount.getBalance()).isEqualTo(amount);
        Account senderAccountAfter = UserSteps.getCustomerAccount(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(senderAccountAfter.getBalance()).isEqualTo(maxAmountForDeposit - amount);
        Transaction lastReceiverTransaction = UserSteps.getAccountLastTransactions(userRequest, createReceiverAccountResponse.getId());
        softly.assertThat(lastReceiverTransaction.validateTransaction(TransactionTypes.TRANSACTION_TYPE_FOR_TRANSFER_IN, amount)).isTrue();
        Transaction lastSenderTransaction = UserSteps.getAccountLastTransactions(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(lastSenderTransaction.validateTransaction(TransactionTypes.TRANSACTION_TYPE_FOR_TRANSFER_OUT, amount)).isTrue();
    }

    @Test
    public void userCanNotTransferWithoutNameIfNameWasChangedTest() {
        CreateAccountResponse createSenderAccountResponse = UserSteps.createAccount(userRequest);
        CreateAccountResponse createReceiverAccountResponse = UserSteps.createAccount(userRequest);
        int maxAmountForDeposit = 5000;
        UserSteps.deposit(userRequest, createSenderAccountResponse.getId(), maxAmountForDeposit);
        String newName = RandomData.getName();
        UserSteps.changeName(userRequest, newName);

        $(Selectors.byXpath("//*[contains(text(),'Make a Transfer')]")).click();

        $(Selectors.byTagName("select")).click();
        $(Selectors.byTagName("select")).selectOptionContainingText(createSenderAccountResponse.getAccountNumber());

        SelenideElement accountInput = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
        accountInput.shouldBe(Condition.visible, Condition.enabled).clear();
        accountInput.sendKeys(createReceiverAccountResponse.getAccountNumber());

        Double amount = RandomData.getDepositAmount();
        SelenideElement input = $(Selectors.byAttribute("placeholder", "Enter amount"));
        input.shouldBe(Condition.visible, Condition.enabled).clear();
        input.sendKeys(amount.toString());

        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byXpath("//button[contains(text(),'Send Transfer')]")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("❌ The recipient name does not match the registered name.");
        alert.accept();

        Account receiverAccount = UserSteps.getCustomerAccount(userRequest, createReceiverAccountResponse.getId());
        softly.assertThat(receiverAccount.getBalance()).isZero();
        Account senderAccount = UserSteps.getCustomerAccount(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(senderAccount.getBalance()).isEqualTo(maxAmountForDeposit);
    }

    @Test
    public void userCanNotTransferWithUsernameAfterNameChangingTest() {
        CreateAccountResponse createSenderAccountResponse = UserSteps.createAccount(userRequest);
        int maxAmountForDeposit = 5000;
        for (int i = 0; i < 3; i++) {
            UserSteps.deposit(userRequest, createSenderAccountResponse.getId(), maxAmountForDeposit);
        }
        Account senderAccountBefore = UserSteps.getCustomerAccount(userRequest, createSenderAccountResponse.getId());
        CreateAccountResponse createReceiverAccountResponse = UserSteps.createAccount(userRequest);
        String newName = RandomData.getName();
        UserSteps.changeName(userRequest, newName);

        $(Selectors.byXpath("//*[contains(text(),'Make a Transfer')]")).click();

        $(Selectors.byTagName("select")).click();
        $(Selectors.byTagName("select")).selectOptionContainingText(createSenderAccountResponse.getAccountNumber());

        SelenideElement nameInput = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
        nameInput.shouldBe(Condition.visible, Condition.enabled).clear();
        nameInput.sendKeys(userRequest.getUsername());

        SelenideElement accountInput = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
        accountInput.shouldBe(Condition.visible, Condition.enabled).clear();
        accountInput.sendKeys(createReceiverAccountResponse.getAccountNumber());

        Double amount = RandomData.getTransferAmount();
        SelenideElement input = $(Selectors.byAttribute("placeholder", "Enter amount"));
        input.shouldBe(Condition.visible, Condition.enabled).clear();
        input.sendKeys(amount.toString());

        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byXpath("//button[contains(text(),'Send Transfer')]")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("❌ The recipient name does not match the registered name.");
        alert.accept();

        Account receiverAccount = UserSteps.getCustomerAccount(userRequest, createReceiverAccountResponse.getId());
        softly.assertThat(receiverAccount.getBalance()).isZero();
        Account senderAccountAfter = UserSteps.getCustomerAccount(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(senderAccountAfter.getBalance()).isEqualTo(senderAccountBefore.getBalance());
    }

    @Test
    public void userCanNotTransferWithInvalidAmountTest() {
        CreateAccountResponse createSenderAccountResponse = UserSteps.createAccount(userRequest);
        CreateAccountResponse createReceiverAccountResponse = UserSteps.createAccount(userRequest);

        $(Selectors.byXpath("//*[contains(text(),'Make a Transfer')]")).click();

        $(Selectors.byTagName("select")).click();
        $(Selectors.byTagName("select")).selectOptionContainingText(createSenderAccountResponse.getAccountNumber());

        SelenideElement nameInput = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
        nameInput.shouldBe(Condition.visible, Condition.enabled).clear();
        nameInput.sendKeys(userRequest.getUsername());

        SelenideElement accountInput = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
        accountInput.shouldBe(Condition.visible, Condition.enabled).clear();
        accountInput.sendKeys(createReceiverAccountResponse.getAccountNumber());

        Double amount = RandomData.getTransferAmount();
        SelenideElement input = $(Selectors.byAttribute("placeholder", "Enter amount"));
        input.shouldBe(Condition.visible, Condition.enabled).clear();
        input.sendKeys(amount.toString());

        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byXpath("//button[contains(text(),'Send Transfer')]")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("❌ Error: Invalid transfer: insufficient funds or invalid accounts");
        alert.accept();

        Account receiverAccount = UserSteps.getCustomerAccount(userRequest, createReceiverAccountResponse.getId());
        softly.assertThat(receiverAccount.getBalance()).isZero();
        Account senderAccount = UserSteps.getCustomerAccount(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(senderAccount.getBalance()).isZero();
    }

    @Test
    public void userCanNotTransferWithoutSelectingSenderAccountTest() {
        CreateAccountResponse createSenderAccountResponse = UserSteps.createAccount(userRequest);
        CreateAccountResponse createReceiverAccountResponse = UserSteps.createAccount(userRequest);
        int maxAmountForDeposit = 5000;
        UserSteps.deposit(userRequest, createSenderAccountResponse.getId(), maxAmountForDeposit);

        $(Selectors.byXpath("//*[contains(text(),'Make a Transfer')]")).click();

        SelenideElement nameInput = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
        nameInput.shouldBe(Condition.visible, Condition.enabled).clear();
        nameInput.sendKeys(userRequest.getUsername());

        SelenideElement accountInput = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
        accountInput.shouldBe(Condition.visible, Condition.enabled).clear();
        accountInput.sendKeys(createReceiverAccountResponse.getAccountNumber());

        Double amount = RandomData.getDepositAmount();
        SelenideElement input = $(Selectors.byAttribute("placeholder", "Enter amount"));
        input.shouldBe(Condition.visible, Condition.enabled).clear();
        input.sendKeys(amount.toString());

        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byXpath("//button[contains(text(),'Send Transfer')]")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("❌ Please fill all fields and confirm.");
        alert.accept();

        Account receiverAccount = UserSteps.getCustomerAccount(userRequest, createReceiverAccountResponse.getId());
        softly.assertThat(receiverAccount.getBalance()).isZero();
        Account senderAccount = UserSteps.getCustomerAccount(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(senderAccount.getBalance()).isEqualTo(maxAmountForDeposit);
    }

    @Test
    public void userCanNotTransferWithoutEnteringRecipientAccountTest() {
        CreateAccountResponse createSenderAccountResponse = UserSteps.createAccount(userRequest);
        int maxAmountForDeposit = 5000;
        UserSteps.deposit(userRequest, createSenderAccountResponse.getId(), maxAmountForDeposit);

        $(Selectors.byXpath("//*[contains(text(),'Make a Transfer')]")).click();

        $(Selectors.byTagName("select")).click();
        $(Selectors.byTagName("select")).selectOptionContainingText(createSenderAccountResponse.getAccountNumber());

        SelenideElement nameInput = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
        nameInput.shouldBe(Condition.visible, Condition.enabled).clear();
        nameInput.sendKeys(userRequest.getUsername());

        Double amount = RandomData.getDepositAmount();
        SelenideElement input = $(Selectors.byAttribute("placeholder", "Enter amount"));
        input.shouldBe(Condition.visible, Condition.enabled).clear();
        input.sendKeys(amount.toString());

        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byXpath("//button[contains(text(),'Send Transfer')]")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("❌ Please fill all fields and confirm.");
        alert.accept();

        Account senderAccount = UserSteps.getCustomerAccount(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(senderAccount.getBalance()).isEqualTo(maxAmountForDeposit);
    }

    @Test
    public void userCanNotTransferWithoutEnteringAmountTest() {
        CreateAccountResponse createSenderAccountResponse = UserSteps.createAccount(userRequest);
        CreateAccountResponse createReceiverAccountResponse = UserSteps.createAccount(userRequest);
        int maxAmountForDeposit = 5000;
        UserSteps.deposit(userRequest, createSenderAccountResponse.getId(), maxAmountForDeposit);

        $(Selectors.byXpath("//*[contains(text(),'Make a Transfer')]")).click();

        $(Selectors.byTagName("select")).click();
        $(Selectors.byTagName("select")).selectOptionContainingText(createSenderAccountResponse.getAccountNumber());

        SelenideElement nameInput = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
        nameInput.shouldBe(Condition.visible, Condition.enabled).clear();
        nameInput.sendKeys(userRequest.getUsername());

        SelenideElement accountInput = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
        accountInput.shouldBe(Condition.visible, Condition.enabled).clear();
        accountInput.sendKeys(createReceiverAccountResponse.getAccountNumber());

        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byXpath("//button[contains(text(),'Send Transfer')]")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("❌ Please fill all fields and confirm.");
        alert.accept();

        Account receiverAccount = UserSteps.getCustomerAccount(userRequest, createReceiverAccountResponse.getId());
        softly.assertThat(receiverAccount.getBalance()).isZero();
        Account senderAccount = UserSteps.getCustomerAccount(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(senderAccount.getBalance()).isEqualTo(maxAmountForDeposit);
    }

    @Test
    public void userCanNotTransferWithoutConfirmingTest() {
        CreateAccountResponse createSenderAccountResponse = UserSteps.createAccount(userRequest);
        CreateAccountResponse createReceiverAccountResponse = UserSteps.createAccount(userRequest);
        int maxAmountForDeposit = 5000;
        UserSteps.deposit(userRequest, createSenderAccountResponse.getId(), maxAmountForDeposit);

        $(Selectors.byXpath("//*[contains(text(),'Make a Transfer')]")).click();

        $(Selectors.byTagName("select")).click();
        $(Selectors.byTagName("select")).selectOptionContainingText(createSenderAccountResponse.getAccountNumber());

        SelenideElement nameInput = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
        nameInput.shouldBe(Condition.visible, Condition.enabled).clear();
        nameInput.sendKeys(userRequest.getUsername());

        SelenideElement accountInput = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
        accountInput.shouldBe(Condition.visible, Condition.enabled).clear();
        accountInput.sendKeys(createReceiverAccountResponse.getAccountNumber());

        Double amount = RandomData.getDepositAmount();
        SelenideElement input = $(Selectors.byAttribute("placeholder", "Enter amount"));
        input.shouldBe(Condition.visible, Condition.enabled).clear();
        input.sendKeys(amount.toString());

        $(Selectors.byXpath("//button[contains(text(),'Send Transfer')]")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("❌ Please fill all fields and confirm.");
        alert.accept();

        Account receiverAccount = UserSteps.getCustomerAccount(userRequest, createReceiverAccountResponse.getId());
        softly.assertThat(receiverAccount.getBalance()).isZero();
        Account senderAccount = UserSteps.getCustomerAccount(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(senderAccount.getBalance()).isEqualTo(maxAmountForDeposit);
    }
}