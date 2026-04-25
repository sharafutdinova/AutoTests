package iteration2.ui;

import api.generators.RandomData;
import api.models.Transaction;
import api.models.TransactionTypes;
import api.models.accounts.CreateAccountResponse;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.TransferPage;
import ui.pages.UserDashboard;

public class TransferUiTest extends BaseUiTest {
    @Test
    @UserSession
    public void userCanTransferWithUsernameTest() {
        CreateAccountResponse createSenderAccountResponse = SessionStorage.getSteps().createAccount();
        int maxAmountForDeposit = 5000;
        for (int i = 0; i < 3; i++) {
            SessionStorage.getSteps().deposit(createSenderAccountResponse.getId(), maxAmountForDeposit);
        }
        CreateAccountResponse senderAccountBefore = SessionStorage.getSteps().getCustomerAccount(createSenderAccountResponse.getId());
        CreateAccountResponse createReceiverAccountResponse = SessionStorage.getSteps().createAccount();

        Double amount = RandomData.getTransferAmount();
        new UserDashboard().open().goToTransferPage();
        String username = SessionStorage.getUser().getUsername();
        new TransferPage().performTransfer(createSenderAccountResponse.getAccountNumber(), username, createReceiverAccountResponse.getAccountNumber(), amount)
                .checkAlertMessageAndAccept(String.format(BankAlert.SUCCESSFULLY_TRANSFERRED.getMessage(), amount, createReceiverAccountResponse.getAccountNumber()));

        CreateAccountResponse receiverAccount = SessionStorage.getSteps().getCustomerAccount(createReceiverAccountResponse.getId());
        softly.assertThat(receiverAccount.getBalance()).isEqualTo(amount);
        CreateAccountResponse senderAccountAfter = SessionStorage.getSteps().getCustomerAccount(createSenderAccountResponse.getId());
        softly.assertThat(senderAccountAfter.getBalance()).isEqualTo(senderAccountBefore.getBalance() - amount);
        Transaction lastReceiverTransaction = SessionStorage.getSteps().getAccountLastTransactions(createReceiverAccountResponse.getId());
        softly.assertThat(lastReceiverTransaction.validateTransaction(TransactionTypes.TRANSACTION_TYPE_FOR_TRANSFER_IN, amount)).isTrue();
        Transaction lastSenderTransaction = SessionStorage.getSteps().getAccountLastTransactions(createSenderAccountResponse.getId());
        softly.assertThat(lastSenderTransaction.validateTransaction(TransactionTypes.TRANSACTION_TYPE_FOR_TRANSFER_OUT, amount)).isTrue();
    }

    @Test
    @UserSession
    public void userCanTransferWithNameAfterNameChangingTest() {
        CreateAccountResponse createSenderAccountResponse = SessionStorage.getSteps().createAccount();
        int maxAmountForDeposit = 5000;
        for (int i = 0; i < 3; i++) {
            SessionStorage.getSteps().deposit(createSenderAccountResponse.getId(), maxAmountForDeposit);
        }
        CreateAccountResponse senderAccountBefore = SessionStorage.getSteps().getCustomerAccount(createSenderAccountResponse.getId());
        CreateAccountResponse createReceiverAccountResponse = SessionStorage.getSteps().createAccount();
        String newName = RandomData.getName();
        SessionStorage.getSteps().changeName(newName);

        Double amount = RandomData.getTransferAmount();
        new TransferPage().open().performTransfer(createSenderAccountResponse.getAccountNumber(), newName, createReceiverAccountResponse.getAccountNumber(), amount)
                .checkAlertMessageAndAccept(String.format(BankAlert.SUCCESSFULLY_TRANSFERRED.getMessage(), amount, createReceiverAccountResponse.getAccountNumber()));

        CreateAccountResponse receiverAccount = SessionStorage.getSteps().getCustomerAccount(createReceiverAccountResponse.getId());
        softly.assertThat(receiverAccount.getBalance()).isEqualTo(amount);
        CreateAccountResponse senderAccountAfter = SessionStorage.getSteps().getCustomerAccount(createSenderAccountResponse.getId());
        softly.assertThat(senderAccountAfter.getBalance()).isEqualTo(senderAccountBefore.getBalance() - amount);
        Transaction lastReceiverTransaction = SessionStorage.getSteps().getAccountLastTransactions(createReceiverAccountResponse.getId());
        softly.assertThat(lastReceiverTransaction.validateTransaction(TransactionTypes.TRANSACTION_TYPE_FOR_TRANSFER_IN, amount)).isTrue();
        Transaction lastSenderTransaction = SessionStorage.getSteps().getAccountLastTransactions(createSenderAccountResponse.getId());
        softly.assertThat(lastSenderTransaction.validateTransaction(TransactionTypes.TRANSACTION_TYPE_FOR_TRANSFER_OUT, amount)).isTrue();
    }

    @Test
    @UserSession
    public void userCanTransferWithoutNameIfNameWasNotChangedTest() {
        CreateAccountResponse createSenderAccountResponse = SessionStorage.getSteps().createAccount();
        CreateAccountResponse createReceiverAccountResponse = SessionStorage.getSteps().createAccount();
        int maxAmountForDeposit = 5000;
        for (int i = 0; i < 3; i++) {
            SessionStorage.getSteps().deposit(createSenderAccountResponse.getId(), maxAmountForDeposit);
        }
        CreateAccountResponse senderAccountBefore = SessionStorage.getSteps().getCustomerAccount(createSenderAccountResponse.getId());

        Double amount = RandomData.getTransferAmount();
        new TransferPage().open().performTransfer(createSenderAccountResponse.getAccountNumber(), createReceiverAccountResponse.getAccountNumber(), amount)
                .checkAlertMessageAndAccept(String.format(BankAlert.SUCCESSFULLY_TRANSFERRED.getMessage(), amount, createReceiverAccountResponse.getAccountNumber()));

        CreateAccountResponse receiverAccount = SessionStorage.getSteps().getCustomerAccount(createReceiverAccountResponse.getId());
        softly.assertThat(receiverAccount.getBalance()).isEqualTo(amount);
        CreateAccountResponse senderAccountAfter = SessionStorage.getSteps().getCustomerAccount(createSenderAccountResponse.getId());
        softly.assertThat(senderAccountAfter.getBalance()).isEqualTo(senderAccountBefore.getBalance() - amount);
        Transaction lastReceiverTransaction = SessionStorage.getSteps().getAccountLastTransactions(createReceiverAccountResponse.getId());
        softly.assertThat(lastReceiverTransaction.validateTransaction(TransactionTypes.TRANSACTION_TYPE_FOR_TRANSFER_IN, amount)).isTrue();
        Transaction lastSenderTransaction = SessionStorage.getSteps().getAccountLastTransactions(createSenderAccountResponse.getId());
        softly.assertThat(lastSenderTransaction.validateTransaction(TransactionTypes.TRANSACTION_TYPE_FOR_TRANSFER_OUT, amount)).isTrue();
    }

    @Test
    @UserSession
    public void userCanNotTransferWithoutNameIfNameWasChangedTest() {
        CreateAccountResponse createSenderAccountResponse = SessionStorage.getSteps().createAccount();
        CreateAccountResponse createReceiverAccountResponse = SessionStorage.getSteps().createAccount();
        int maxAmountForDeposit = 5000;
        SessionStorage.getSteps().deposit(createSenderAccountResponse.getId(), maxAmountForDeposit);
        String newName = RandomData.getName();
        SessionStorage.getSteps().changeName(newName);

        Double amount = RandomData.getTransferAmount();
        new TransferPage().open().performTransfer(createSenderAccountResponse.getAccountNumber(), createReceiverAccountResponse.getAccountNumber(), amount)
                .checkAlertMessageAndAccept(BankAlert.RECIPIENT_NAME_DOES_NOT_MATCH_THE_REGISTERED_NAME.getMessage());

        CreateAccountResponse receiverAccount = SessionStorage.getSteps().getCustomerAccount(createReceiverAccountResponse.getId());
        softly.assertThat(receiverAccount.getBalance()).isZero();
        CreateAccountResponse senderAccount = SessionStorage.getSteps().getCustomerAccount(createSenderAccountResponse.getId());
        softly.assertThat(senderAccount.getBalance()).isEqualTo(maxAmountForDeposit);
    }

    @Test
    @UserSession
    public void userCanNotTransferWithUsernameAfterNameChangingTest() {
        CreateAccountResponse createSenderAccountResponse = SessionStorage.getSteps().createAccount();
        int maxAmountForDeposit = 5000;
        for (int i = 0; i < 3; i++) {
            SessionStorage.getSteps().deposit(createSenderAccountResponse.getId(), maxAmountForDeposit);
        }
        CreateAccountResponse senderAccountBefore = SessionStorage.getSteps().getCustomerAccount(createSenderAccountResponse.getId());
        CreateAccountResponse createReceiverAccountResponse = SessionStorage.getSteps().createAccount();
        String newName = RandomData.getName();
        SessionStorage.getSteps().changeName(newName);

        Double amount = RandomData.getTransferAmount();
        String username = SessionStorage.getUser().getUsername();
        new TransferPage().open().performTransfer(createSenderAccountResponse.getAccountNumber(), username, createReceiverAccountResponse.getAccountNumber(), amount)
                .checkAlertMessageAndAccept(BankAlert.RECIPIENT_NAME_DOES_NOT_MATCH_THE_REGISTERED_NAME.getMessage());

        CreateAccountResponse receiverAccount = SessionStorage.getSteps().getCustomerAccount(createReceiverAccountResponse.getId());
        softly.assertThat(receiverAccount.getBalance()).isZero();
        CreateAccountResponse senderAccountAfter = SessionStorage.getSteps().getCustomerAccount(createSenderAccountResponse.getId());
        softly.assertThat(senderAccountAfter.getBalance()).isEqualTo(senderAccountBefore.getBalance());
    }

    @Test
    @UserSession
    public void userCanNotTransferWithInvalidAmountTest() {
        CreateAccountResponse createSenderAccountResponse = SessionStorage.getSteps().createAccount();
        CreateAccountResponse createReceiverAccountResponse = SessionStorage.getSteps().createAccount();

        Double amount = RandomData.getTransferAmount();
        new TransferPage().open().performTransfer(createSenderAccountResponse.getAccountNumber(), SessionStorage.getUser().getUsername(), createReceiverAccountResponse.getAccountNumber(), amount)
                .checkAlertMessageAndAccept(BankAlert.ERROR_INVALID_TRANSFER.getMessage());

        CreateAccountResponse receiverAccount = SessionStorage.getSteps().getCustomerAccount(createReceiverAccountResponse.getId());
        softly.assertThat(receiverAccount.getBalance()).isZero();
        CreateAccountResponse senderAccount = SessionStorage.getSteps().getCustomerAccount(createSenderAccountResponse.getId());
        softly.assertThat(senderAccount.getBalance()).isZero();
    }

    @Test
    @UserSession
    public void userCanNotTransferWithoutSelectingSenderAccountTest() {
        CreateAccountResponse createReceiverAccountResponse = SessionStorage.getSteps().createAccount();

        Double amount = RandomData.getDepositAmount();
        new TransferPage().open()
                .enterRecipientName(SessionStorage.getUser().getUsername())
                .enterRecipientAccount(createReceiverAccountResponse.getAccountNumber())
                .enterAmount(amount)
                .confirm()
                .clickToTransfer()
                .checkAlertMessageAndAccept(BankAlert.FILL_ALL_FIELDS_AND_CONFIRM.getMessage());

        CreateAccountResponse receiverAccount = SessionStorage.getSteps().getCustomerAccount(createReceiverAccountResponse.getId());
        softly.assertThat(receiverAccount.getBalance()).isZero();
    }

    @Test
    @UserSession
    public void userCanNotTransferWithoutEnteringRecipientAccountTest() {
        CreateAccountResponse createSenderAccountResponse = SessionStorage.getSteps().createAccount();
        int maxAmountForDeposit = 5000;
        SessionStorage.getSteps().deposit(createSenderAccountResponse.getId(), maxAmountForDeposit);

        Double amount = RandomData.getDepositAmount();
        new TransferPage().open()
                .selectAccount(createSenderAccountResponse.getAccountNumber())
                .enterRecipientName(SessionStorage.getUser().getUsername())
                .enterAmount(amount)
                .confirm()
                .clickToTransfer()
                .checkAlertMessageAndAccept(BankAlert.FILL_ALL_FIELDS_AND_CONFIRM.getMessage());

        CreateAccountResponse senderAccount = SessionStorage.getSteps().getCustomerAccount(createSenderAccountResponse.getId());
        softly.assertThat(senderAccount.getBalance()).isEqualTo(maxAmountForDeposit);
    }

    @Test
    @UserSession
    public void userCanNotTransferWithoutEnteringAmountTest() {
        CreateAccountResponse createSenderAccountResponse = SessionStorage.getSteps().createAccount();
        CreateAccountResponse createReceiverAccountResponse = SessionStorage.getSteps().createAccount();
        int maxAmountForDeposit = 5000;
        SessionStorage.getSteps().deposit(createSenderAccountResponse.getId(), maxAmountForDeposit);

        new TransferPage().open()
                .selectAccount(createSenderAccountResponse.getAccountNumber())
                .enterRecipientName(SessionStorage.getUser().getUsername())
                .enterRecipientAccount(createReceiverAccountResponse.getAccountNumber())
                .confirm()
                .clickToTransfer()
                .checkAlertMessageAndAccept(BankAlert.FILL_ALL_FIELDS_AND_CONFIRM.getMessage());

        CreateAccountResponse receiverAccount = SessionStorage.getSteps().getCustomerAccount(createReceiverAccountResponse.getId());
        softly.assertThat(receiverAccount.getBalance()).isZero();
        CreateAccountResponse senderAccount = SessionStorage.getSteps().getCustomerAccount(createSenderAccountResponse.getId());
        softly.assertThat(senderAccount.getBalance()).isEqualTo(maxAmountForDeposit);
    }

    @Test
    @UserSession
    public void userCanNotTransferWithoutConfirmingTest() {
        CreateAccountResponse createSenderAccountResponse = SessionStorage.getSteps().createAccount();
        CreateAccountResponse createReceiverAccountResponse = SessionStorage.getSteps().createAccount();
        int maxAmountForDeposit = 5000;
        SessionStorage.getSteps().deposit(createSenderAccountResponse.getId(), maxAmountForDeposit);

        Double amount = RandomData.getDepositAmount();
        new TransferPage().open()
                .selectAccount(createSenderAccountResponse.getAccountNumber())
                .enterRecipientName(SessionStorage.getUser().getUsername())
                .enterRecipientAccount(createReceiverAccountResponse.getAccountNumber())
                .enterAmount(amount)
                .clickToTransfer()
                .checkAlertMessageAndAccept(BankAlert.FILL_ALL_FIELDS_AND_CONFIRM.getMessage());

        CreateAccountResponse receiverAccount = SessionStorage.getSteps().getCustomerAccount(createReceiverAccountResponse.getId());
        softly.assertThat(receiverAccount.getBalance()).isZero();
        CreateAccountResponse senderAccount = SessionStorage.getSteps().getCustomerAccount(createSenderAccountResponse.getId());
        softly.assertThat(senderAccount.getBalance()).isEqualTo(maxAmountForDeposit);
    }
}