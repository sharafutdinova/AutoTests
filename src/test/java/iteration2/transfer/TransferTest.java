package iteration2.transfer;

import io.restassured.http.ContentType;
import iteration2.Transaction;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransferTest extends TestSetup {
    /*
    User can transfer 10000 to self account
    User can transfer 9999.99 to self account
    User can transfer 0.01 to self account
     */
    public static Stream<Arguments> positiveCasesForTransferTest() {
        return Stream.of(
                Arguments.of(10000.00),
                Arguments.of(9999.99),
                Arguments.of(0.01));
    }

    @ParameterizedTest
    @MethodSource("positiveCasesForTransferTest")
    public void userCanTransferTest(double amount) {
        List<Transaction> transactionsBeforeForSender = getAccountTransactions(getSenderAccountId(), getDefaultUserToken());
        List<Transaction> transactionsBeforeForReceiver = getAccountTransactions(getReceiverAccountId(), getDefaultUserToken());
        JSONObject requestParams = new JSONObject();
        requestParams.put("senderAccountId", getSenderAccountId());
        requestParams.put("receiverAccountId", getReceiverAccountId());
        requestParams.put("amount", amount);
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("Authorization", getDefaultUserToken())
                .body(requestParams.toString())
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("senderAccountId", equalTo(getSenderAccountId()))
                .body("receiverAccountId", equalTo(getReceiverAccountId()))
                .body("amount", equalTo((float) amount))
                .body("message", equalTo("Transfer successful"));

        List<Transaction> transactionsAfterForSender = getAccountTransactions(getSenderAccountId(), getDefaultUserToken());
        Transaction lastTransactionForSender = transactionsAfterForSender.stream().max(Comparator.comparing(Transaction::getId)).get();
        assertEquals(amount, lastTransactionForSender.getAmount());
        assertEquals(TRANSACTION_TYPE_FOR_TRANSFER_OUT, lastTransactionForSender.getType());
        assertEquals(transactionsBeforeForSender.size() + 1, transactionsAfterForSender.size());

        List<Transaction> transactionsAfterForReceiver = getAccountTransactions(getReceiverAccountId(), getDefaultUserToken());
        Transaction lastTransactionForReceiver = transactionsAfterForReceiver.stream().max(Comparator.comparing(Transaction::getId)).get();
        assertEquals(amount, lastTransactionForReceiver.getAmount());
        assertEquals(TRANSACTION_TYPE_FOR_TRANSFER_IN, lastTransactionForReceiver.getType());
        assertEquals(transactionsBeforeForReceiver.size() + 1, transactionsAfterForReceiver.size());
    }

    @Test
    public void userCanTransferValueEqualToBalanceToAnotherUserAccountTest() {
        String newUserName = "Anna";
        double amount = 500.00;
        createUser(newUserName);
        String anotherUserToken = getToken(newUserName);
        Integer anotherUserAccountId = createAccount(anotherUserToken);
        Integer selfAccountId = createAccount(getDefaultUserToken());
        balanceReplenishment(selfAccountId, amount, getDefaultUserToken());
        List<Transaction> transactionsBeforeForSender = getAccountTransactions(selfAccountId, getDefaultUserToken());
        List<Transaction> transactionsBeforeForReceiver = getAccountTransactions(anotherUserAccountId, anotherUserToken);
        JSONObject requestParams = new JSONObject();
        requestParams.put("senderAccountId", selfAccountId);
        requestParams.put("receiverAccountId", anotherUserAccountId);
        requestParams.put("amount", amount);
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("Authorization", getDefaultUserToken())
                .body(requestParams.toString())
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("senderAccountId", equalTo(selfAccountId))
                .body("receiverAccountId", equalTo(anotherUserAccountId))
                .body("amount", equalTo((float) amount))
                .body("message", equalTo("Transfer successful"));

        List<Transaction> transactionsAfterForSender = getAccountTransactions(selfAccountId, getDefaultUserToken());
        Transaction lastTransactionForSender = transactionsAfterForSender.stream().max(Comparator.comparing(Transaction::getId)).get();
        assertEquals(amount, lastTransactionForSender.getAmount());
        assertEquals(TRANSACTION_TYPE_FOR_TRANSFER_OUT, lastTransactionForSender.getType());
        assertEquals(transactionsBeforeForSender.size() + 1, transactionsAfterForSender.size());

        List<Transaction> transactionsAfterForReceiver = getAccountTransactions(anotherUserAccountId, anotherUserToken);
        Transaction lastTransactionForReceiver = transactionsAfterForReceiver.stream().max(Comparator.comparing(Transaction::getId)).get();
        assertEquals(amount, lastTransactionForReceiver.getAmount());
        assertEquals(TRANSACTION_TYPE_FOR_TRANSFER_IN, lastTransactionForReceiver.getType());
        assertEquals(transactionsBeforeForReceiver.size() + 1, transactionsAfterForReceiver.size());
    }

    /*
        User cannot transfer value greater than 10000
        User cannot transfer 0
        User cannot transfer negative value
    */
    public static Stream<Arguments> negativeCasesForTransferTest() {
        return Stream.of(
                Arguments.of(10000.01, "Transfer amount cannot exceed 10000"),
                Arguments.of(0.00, "Transfer amount must be at least 0.01"),
                Arguments.of(-0.01, "Transfer amount must be at least 0.01"));
    }

    @ParameterizedTest
    @MethodSource("negativeCasesForTransferTest")
    public void userCanNotTransferInvalidAmountTest(double amount, String errorMessage) {
        List<Transaction> transactionsBeforeForSender = getAccountTransactions(getSenderAccountId(), getDefaultUserToken());
        List<Transaction> transactionsBeforeForReceiver = getAccountTransactions(getReceiverAccountId(), getDefaultUserToken());
        JSONObject requestParams = new JSONObject();
        requestParams.put("senderAccountId", getSenderAccountId());
        requestParams.put("receiverAccountId", getReceiverAccountId());
        requestParams.put("amount", amount);
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("Authorization", getDefaultUserToken())
                .body(requestParams.toString())
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(400)
                .body(equalTo(errorMessage));

        List<Transaction> transactionsAfterForSender = getAccountTransactions(getSenderAccountId(), getDefaultUserToken());
        assertEquals(transactionsBeforeForSender.size(), transactionsAfterForSender.size());
        List<Transaction> transactionsAfterForReceiver = getAccountTransactions(getReceiverAccountId(), getDefaultUserToken());
        assertEquals(transactionsBeforeForReceiver.size(), transactionsAfterForReceiver.size());
    }

    @Test
    public void userCanNotTransferValueGreaterThanBalanceTest() {
        double amount = 150.00;
        Integer selfAccountId = createAccount(getDefaultUserToken());
        balanceReplenishment(selfAccountId, amount, getDefaultUserToken());
        List<Transaction> transactionsBefore = getAccountTransactions(selfAccountId, getDefaultUserToken());
        JSONObject requestParams = new JSONObject();
        requestParams.put("senderAccountId", selfAccountId);
        requestParams.put("receiverAccountId", getReceiverAccountId());
        requestParams.put("amount", amount + 1);
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("Authorization", getDefaultUserToken())
                .body(requestParams.toString())
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(400)
                .body(equalTo("Invalid transfer: insufficient funds or invalid accounts"));

        List<Transaction> transactionsAfter = getAccountTransactions(selfAccountId, getDefaultUserToken());
        assertEquals(transactionsBefore.size(), transactionsAfter.size());
    }

    @Test
    public void userCanNotTransferToTheSameAccountTest() {
        double amount = 150.00;
        List<Transaction> transactionsBefore = getAccountTransactions(getSenderAccountId(), getDefaultUserToken());
        JSONObject requestParams = new JSONObject();
        requestParams.put("senderAccountId", getSenderAccountId());
        requestParams.put("receiverAccountId", getSenderAccountId());
        requestParams.put("amount", amount);
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("Authorization", getDefaultUserToken())
                .body(requestParams.toString())
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(400)
                .body(equalTo("Invalid transfer: insufficient funds or invalid accounts"));

        List<Transaction> transactionsAfter = getAccountTransactions(getSenderAccountId(), getDefaultUserToken());
        assertEquals(transactionsBefore.size(), transactionsAfter.size());
    }

    @Test
    public void userCanNotTransferFromNotExistsAccountTest() {
        double amount = 150.00;
        int accountId = 1000;
        List<Transaction> transactionsBefore = getAccountTransactions(getReceiverAccountId(), getDefaultUserToken());
        JSONObject requestParams = new JSONObject();
        requestParams.put("senderAccountId", accountId);
        requestParams.put("receiverAccountId", getReceiverAccountId());
        requestParams.put("amount", amount);
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("Authorization", getDefaultUserToken())
                .body(requestParams.toString())
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(403)
                .body(equalTo("Unauthorized access to account"));

        List<Transaction> transactionsAfter = getAccountTransactions(getReceiverAccountId(), getDefaultUserToken());
        assertEquals(transactionsBefore.size(), transactionsAfter.size());
    }

    @Test
    public void userCanNotTransferToNotExistsAccountTest() {
        double amount = 150.00;
        int accountId = 1000;
        List<Transaction> transactionsBefore = getAccountTransactions(getSenderAccountId(), getDefaultUserToken());
        JSONObject requestParams = new JSONObject();
        requestParams.put("senderAccountId", getSenderAccountId());
        requestParams.put("receiverAccountId", accountId);
        requestParams.put("amount", amount);
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("Authorization", getDefaultUserToken())
                .body(requestParams.toString())
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(400)
                .body(equalTo("Invalid transfer: insufficient funds or invalid accounts"));

        List<Transaction> transactionsAfter = getAccountTransactions(getSenderAccountId(), getDefaultUserToken());
        assertEquals(transactionsBefore.size(), transactionsAfter.size());
    }

    @Test
    public void userCanNotTransferFromAnotherUserAccountTest() {
        String newUserName = "Anna";
        double amount = 500.00;
        createUser(newUserName);
        String anotherUserToken = getToken(newUserName);
        Integer anotherUserAccountId = createAccount(anotherUserToken);
        balanceReplenishment(anotherUserAccountId, amount, anotherUserToken);
        JSONObject requestParams = new JSONObject();
        requestParams.put("senderAccountId", anotherUserAccountId);
        requestParams.put("receiverAccountId", getReceiverAccountId());
        requestParams.put("amount", amount);
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("Authorization", getDefaultUserToken())
                .body(requestParams.toString())
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(403)
                .body(equalTo("Unauthorized access to account"));
    }
}
