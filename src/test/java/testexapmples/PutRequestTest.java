package testexapmples;

import io.restassured.RestAssured;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class PutRequestTest {
    @Test
    public void testUpdateUser() {
        RestAssured.baseURI = "https://api.example.com";

        JSONObject requestBody = new JSONObject();
        requestBody.put("email", "newemail@example.com");

        given()
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .when()
                .put("/users/123")
                .then()
                .statusCode(200)
                .body("email", equalTo("newemail@example.com"));
    }
}