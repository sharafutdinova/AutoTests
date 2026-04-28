package testexapmples;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

public class CookiesTest {
    @Test
    public void testCookies() {
        given()
                .cookie("session_id", "abcdef12345")
                .when()
                .get("/dashboard")
                .then()
                .statusCode(200);
    }
}
