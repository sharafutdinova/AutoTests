package testexapmples;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

public class DeleteRequestTest {
    @Test
    public void testDeleteUser() {
        RestAssured.baseURI = "https://api.example.com";

        given()
                .when()
                .delete("/users/123")
                .then()
                .statusCode(204);  // No Content
    }
}