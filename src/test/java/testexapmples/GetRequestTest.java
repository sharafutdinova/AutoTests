package testexapmples;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class GetRequestTest {
    @Test
    public void testGetUser() {
        baseURI = "https://api.example.com";

        given()
            .header("Authorization", "Bearer TOKEN")
        .when()
            .get("/users/123")
        .then()
            .statusCode(200)
            .body("name", equalTo("John Doe"))
            .body("email", equalTo("john.doe@example.com"));
    }
}