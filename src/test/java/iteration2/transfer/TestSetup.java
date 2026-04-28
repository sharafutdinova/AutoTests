package iteration2.transfer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import iteration2.Transaction;
import iteration2.TransactionsList;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;

import java.lang.reflect.Type;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class TestSetup {
    private static String userToken;
    public static final String TRANSACTION_TYPE_FOR_TRANSFER_OUT = "TRANSFER_OUT";
    public static final String TRANSACTION_TYPE_FOR_TRANSFER_IN = "TRANSFER_IN";
    private static final String DEFAULT_USERNAME = "Alisa";
    private static final String DEFAULT_PASSWORD = "Test123!";
    private static int senderAccountId;
    private static int receiverAccountId;

    public String getDefaultUserToken() {
        return userToken;
    }

    public int getSenderAccountId() {
        return senderAccountId;
    }

    public int getReceiverAccountId() {
        return receiverAccountId;
    }

    @BeforeAll
    public static void setup() {
        RestAssured.filters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));
    }

    @BeforeAll
    public static void prepareTestData() {
        createUser(DEFAULT_USERNAME);
        userToken = getToken(DEFAULT_USERNAME);
        senderAccountId = createAccount(userToken);
        receiverAccountId = createAccount(userToken);
        double maxAmount = 5000.00;
        for (int i = 0; i <= 4; i++) {
            balanceReplenishment(senderAccountId, maxAmount, userToken);
        }
    }

    public static void createUser(String username) {
        JSONObject requestParams = new JSONObject();
        requestParams.put("username", username);
        requestParams.put("password", DEFAULT_PASSWORD);
        requestParams.put("role", "USER");
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(requestParams.toString())
                .post("http://localhost:4111/api/v1/admin/users");
    }

    public static String getToken(String username) {
        JSONObject requestParams = new JSONObject();
        requestParams.put("username", username);
        requestParams.put("password", DEFAULT_PASSWORD);
        return given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(requestParams.toString())
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");
    }

    public static Integer createAccount(String token) {
        return given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", token)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(201)
                .extract()
                .body().path("id");
    }

    public static void balanceReplenishment(int accountId, double amount, String token) {
        JSONObject requestParams = new JSONObject();
        requestParams.put("id", accountId);
        requestParams.put("balance", amount);
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("Authorization", token)
                .body(requestParams.toString())
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(200);
    }

    public List<Transaction> getAccountTransactions(int accountId, String token) {
        String result = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", token)
                .pathParam("accountId", accountId)
                .get("http://localhost:4111/api/v1/accounts/{accountId}/transactions")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().asString();

        Gson gson = new Gson();
        Type type = new TypeToken<List<Transaction>>() {
        }.getType();
        return gson.fromJson(result, type);
    }

    public TransactionsList getTransactionsList(String response) {
        Gson gson = new Gson();
        Type type = new TypeToken<TransactionsList>() {
        }.getType();
        return gson.fromJson(response, type);
    }
}
