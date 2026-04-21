package iteration2.deposit;

import io.restassured.http.ContentType;
import iteration2.Transaction;
import iteration2.TransactionsList;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DepositTest extends TestSetup {
    /*
    User can deposit 5000
    User can deposit 4999.99
    User can deposit 0.01
     */
    @ParameterizedTest
    @ValueSource(doubles = {5000, 4999.99, 0.01})
    public void userCanDepositTest(Double amount) {
        int accountId = getDefaultUserAccountId();
        List<Transaction> transactionsBefore = getAccountTransactions(accountId);
        JSONObject requestParams = new JSONObject();
        requestParams.put("id", accountId);
        requestParams.put("balance", amount);
        String result = given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("Authorization", getDefaultUserToken())
                .body(requestParams.toString())
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().asString();
        TransactionsList transactionsFromResponse = getTransactionsList(result);
        List<Transaction> transactionsAfter = getAccountTransactions(accountId);
        Transaction lastTransaction = transactionsAfter.stream().max(Comparator.comparing(Transaction::getId)).get();
        assertEquals(amount, lastTransaction.getAmount());
        assertEquals(TRANSACTION_TYPE_FOR_DEPOSIT, lastTransaction.getType());
        assertEquals(transactionsBefore.size() + 1, transactionsAfter.size());
        assertTrue(transactionsFromResponse.getTransactions().contains(lastTransaction));
    }

    /*
    User cannot deposit 0
    User cannot deposit negative value
    User cannot deposit value more than 5000
     */
    public static Stream<Arguments> negativeCasesForDepositTest() {
        return Stream.of(
                Arguments.of(0.00, "Deposit amount must be at least 0.01"),
                Arguments.of(-0.01, "Deposit amount must be at least 0.01"),
                Arguments.of(5000.01, "Deposit amount cannot exceed 5000"));
    }

    @ParameterizedTest
    @MethodSource("negativeCasesForDepositTest")
    public void userCanNotDepositTest(Double amount, String errorMessage) {
        int accountId = getDefaultUserAccountId();
        List<Transaction> transactionsBefore = getAccountTransactions(accountId);
        JSONObject requestParams = new JSONObject();
        requestParams.put("id", accountId);
        requestParams.put("balance", amount);
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("Authorization", getDefaultUserToken())
                .body(requestParams.toString())
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(400)
                .body(equalTo(errorMessage));
        List<Transaction> transactionsAfter = getAccountTransactions(accountId);
        assertEquals(transactionsBefore.size(), transactionsAfter.size());
    }

    @Test
    public void userCanNotDepositToNotExistsAccountTest() {
        int accountId = 100;
        double amount = 500.00;
        JSONObject requestParams = new JSONObject();
        requestParams.put("id", accountId);
        requestParams.put("balance", amount);
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("Authorization", this.getDefaultUserToken())
                .body(requestParams.toString())
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(403);
    }

    @Test
    public void userCanNotDepositToAnotherUserAccountTest() {
        String newUserName = "Kate";
        createUser(newUserName);
        String token = getToken(newUserName);
        Integer accountId = createAccount(token);
        double amount = 500.00;
        JSONObject requestParams = new JSONObject();
        requestParams.put("id", accountId);
        requestParams.put("balance", amount);
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("Authorization", getDefaultUserToken())
                .body(requestParams.toString())
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(403);
    }

    @Test
    public void userCanNotDepositWithWrongTokenTest() {
        int accountId = getDefaultUserAccountId();
        double amount = 500.00;
        JSONObject requestParams = new JSONObject();
        requestParams.put("id", accountId);
        requestParams.put("balance", amount);
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("Authorization", getDefaultUserToken() + 1)
                .body(requestParams.toString())
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(401);
    }
}
