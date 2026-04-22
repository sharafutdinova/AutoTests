package iteration2.ui;

import api.generators.RandomData;
import api.models.Account;
import api.models.Transaction;
import api.models.TransactionTypes;
import api.models.accounts.CreateAccountResponse;
import org.junit.jupiter.api.Test;
import api.requests.steps.UserSteps;
import ui.pages.BankAlert;
import ui.pages.TransferPage;
import ui.pages.UserDashboard;

public class TransferUiTest extends BaseUiTest {
    @Test
    public void userCanTransferWithUsernameTest() {
        CreateAccountResponse createSenderAccountResponse = UserSteps.createAccount(userRequest);
        int maxAmountForDeposit = 5000;
        for (int i = 0; i < 3; i++) {
            UserSteps.deposit(userRequest, createSenderAccountResponse.getId(), maxAmountForDeposit);
        }
        Account senderAccountBefore = UserSteps.getCustomerAccount(userRequest, createSenderAccountResponse.getId());
        CreateAccountResponse createReceiverAccountResponse = UserSteps.createAccount(userRequest);

        authAsUser(userRequest);
        Double amount = RandomData.getTransferAmount();
        new UserDashboard().open().goToTransferPage();
        new TransferPage().performTransfer(createSenderAccountResponse.getAccountNumber(), userRequest.getUsername(), createReceiverAccountResponse.getAccountNumber(), amount)
                .checkAlertMessageAndAccept(String.format(BankAlert.SUCCESSFULLY_TRANSFERRED.getMessage(), amount, createReceiverAccountResponse.getAccountNumber()));

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

        authAsUser(userRequest);
        Double amount = RandomData.getTransferAmount();
        new TransferPage().open().performTransfer(createSenderAccountResponse.getAccountNumber(), newName, createReceiverAccountResponse.getAccountNumber(), amount)
                .checkAlertMessageAndAccept(String.format(BankAlert.SUCCESSFULLY_TRANSFERRED.getMessage(), amount, createReceiverAccountResponse.getAccountNumber()));

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
        for (int i = 0; i < 3; i++) {
            UserSteps.deposit(userRequest, createSenderAccountResponse.getId(), maxAmountForDeposit);
        }
        Account senderAccountBefore = UserSteps.getCustomerAccount(userRequest, createSenderAccountResponse.getId());

        authAsUser(userRequest);
        Double amount = RandomData.getTransferAmount();
        new TransferPage().open().performTransfer(createSenderAccountResponse.getAccountNumber(), createReceiverAccountResponse.getAccountNumber(), amount)
                .checkAlertMessageAndAccept(String.format(BankAlert.SUCCESSFULLY_TRANSFERRED.getMessage(), amount, createReceiverAccountResponse.getAccountNumber()));

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
    public void userCanNotTransferWithoutNameIfNameWasChangedTest() {
        CreateAccountResponse createSenderAccountResponse = UserSteps.createAccount(userRequest);
        CreateAccountResponse createReceiverAccountResponse = UserSteps.createAccount(userRequest);
        int maxAmountForDeposit = 5000;
        UserSteps.deposit(userRequest, createSenderAccountResponse.getId(), maxAmountForDeposit);
        String newName = RandomData.getName();
        UserSteps.changeName(userRequest, newName);

        authAsUser(userRequest);
        Double amount = RandomData.getTransferAmount();
        new TransferPage().open().performTransfer(createSenderAccountResponse.getAccountNumber(), createReceiverAccountResponse.getAccountNumber(), amount)
                .checkAlertMessageAndAccept(BankAlert.RECIPIENT_NAME_DOES_NOT_MATCH_THE_REGISTERED_NAME.getMessage());

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

        authAsUser(userRequest);
        Double amount = RandomData.getTransferAmount();
        new TransferPage().open().performTransfer(createSenderAccountResponse.getAccountNumber(), userRequest.getUsername(), createReceiverAccountResponse.getAccountNumber(), amount)
                .checkAlertMessageAndAccept(BankAlert.RECIPIENT_NAME_DOES_NOT_MATCH_THE_REGISTERED_NAME.getMessage());

        Account receiverAccount = UserSteps.getCustomerAccount(userRequest, createReceiverAccountResponse.getId());
        softly.assertThat(receiverAccount.getBalance()).isZero();
        Account senderAccountAfter = UserSteps.getCustomerAccount(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(senderAccountAfter.getBalance()).isEqualTo(senderAccountBefore.getBalance());
    }

    @Test
    public void userCanNotTransferWithInvalidAmountTest() {
        CreateAccountResponse createSenderAccountResponse = UserSteps.createAccount(userRequest);
        CreateAccountResponse createReceiverAccountResponse = UserSteps.createAccount(userRequest);

        authAsUser(userRequest);
        Double amount = RandomData.getTransferAmount();
        new TransferPage().open().performTransfer(createSenderAccountResponse.getAccountNumber(), userRequest.getUsername(), createReceiverAccountResponse.getAccountNumber(), amount)
                .checkAlertMessageAndAccept(BankAlert.ERROR_INVALID_TRANSFER.getMessage());

        Account receiverAccount = UserSteps.getCustomerAccount(userRequest, createReceiverAccountResponse.getId());
        softly.assertThat(receiverAccount.getBalance()).isZero();
        Account senderAccount = UserSteps.getCustomerAccount(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(senderAccount.getBalance()).isZero();
    }

    @Test
    public void userCanNotTransferWithoutSelectingSenderAccountTest() {
        CreateAccountResponse createReceiverAccountResponse = UserSteps.createAccount(userRequest);

        Double amount = RandomData.getDepositAmount();
        authAsUser(userRequest);
        new TransferPage().open()
                .enterRecipientName(userRequest.getUsername())
                .enterRecipientAccount(createReceiverAccountResponse.getAccountNumber())
                .enterAmount(amount)
                .confirm()
                .clickToTransfer()
                .checkAlertMessageAndAccept(BankAlert.FILL_ALL_FIELDS_AND_CONFIRM.getMessage());

        Account receiverAccount = UserSteps.getCustomerAccount(userRequest, createReceiverAccountResponse.getId());
        softly.assertThat(receiverAccount.getBalance()).isZero();
    }

    @Test
    public void userCanNotTransferWithoutEnteringRecipientAccountTest() {
        CreateAccountResponse createSenderAccountResponse = UserSteps.createAccount(userRequest);
        int maxAmountForDeposit = 5000;
        UserSteps.deposit(userRequest, createSenderAccountResponse.getId(), maxAmountForDeposit);

        Double amount = RandomData.getDepositAmount();
        authAsUser(userRequest);
        new TransferPage().open()
                .selectAccount(createSenderAccountResponse.getAccountNumber())
                .enterRecipientName(userRequest.getUsername())
                .enterAmount(amount)
                .confirm()
                .clickToTransfer()
                .checkAlertMessageAndAccept(BankAlert.FILL_ALL_FIELDS_AND_CONFIRM.getMessage());

        Account senderAccount = UserSteps.getCustomerAccount(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(senderAccount.getBalance()).isEqualTo(maxAmountForDeposit);
    }

    @Test
    public void userCanNotTransferWithoutEnteringAmountTest() {
        CreateAccountResponse createSenderAccountResponse = UserSteps.createAccount(userRequest);
        CreateAccountResponse createReceiverAccountResponse = UserSteps.createAccount(userRequest);
        int maxAmountForDeposit = 5000;
        UserSteps.deposit(userRequest, createSenderAccountResponse.getId(), maxAmountForDeposit);

        authAsUser(userRequest);
        new TransferPage().open()
                .selectAccount(createSenderAccountResponse.getAccountNumber())
                .enterRecipientName(userRequest.getUsername())
                .enterRecipientAccount(createReceiverAccountResponse.getAccountNumber())
                .confirm()
                .clickToTransfer()
                .checkAlertMessageAndAccept(BankAlert.FILL_ALL_FIELDS_AND_CONFIRM.getMessage());

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

        Double amount = RandomData.getDepositAmount();
        authAsUser(userRequest);
        new TransferPage().open()
                .selectAccount(createSenderAccountResponse.getAccountNumber())
                .enterRecipientName(userRequest.getUsername())
                .enterRecipientAccount(createReceiverAccountResponse.getAccountNumber())
                .enterAmount(amount)
                .clickToTransfer()
                .checkAlertMessageAndAccept(BankAlert.FILL_ALL_FIELDS_AND_CONFIRM.getMessage());

        Account receiverAccount = UserSteps.getCustomerAccount(userRequest, createReceiverAccountResponse.getId());
        softly.assertThat(receiverAccount.getBalance()).isZero();
        Account senderAccount = UserSteps.getCustomerAccount(userRequest, createSenderAccountResponse.getId());
        softly.assertThat(senderAccount.getBalance()).isEqualTo(maxAmountForDeposit);
    }
}