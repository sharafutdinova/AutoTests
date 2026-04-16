package testexapmples;

import io.restassured.RestAssured;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class PostRequestTest {
    @Test
    public void testCreateUser() {
        RestAssured.baseURI = "https://api.example.com";

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "Alice");
        requestBody.put("email", "alice@example.com");

        given()
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .body("message", equalTo("User created successfully"));
    }
}