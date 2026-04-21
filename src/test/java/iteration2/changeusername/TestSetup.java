package iteration2.changeusername;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;

import java.lang.reflect.Type;
import java.util.List;

import static io.restassured.RestAssured.given;

public class TestSetup {
    private static String userToken;

    public String getUserToken() {
        return userToken;
    }

    @BeforeAll
    public static void setup() {
        RestAssured.filters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));
    }

    @BeforeAll
    public static void prepareTestData() {
        createUser();
        getToken();
    }

    public static void createUser() {
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                          {
                          "username": "AlsuSha",
                          "password": "Test123!",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users");
    }

    public static void getToken() {
        userToken = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "AlsuSha",
                          "password": "Test123!"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");
    }

    public CustomerProfile getCustomerProfile() {
        String result = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userToken)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().asString();

        Gson gson = new Gson();
        Type type = new TypeToken<CustomerProfile>() {
        }.getType();
        return gson.fromJson(result, type);
    }
}
