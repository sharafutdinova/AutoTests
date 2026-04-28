package testexapmples;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

public class HeadersTest {
    @Test
    public void testCustomHeaders() {
        given()
                .header("X-Client-Id", "123456")
                .header("Authorization", "Bearer TOKEN")
                .when()
                .get("/users")
                .then()
                .statusCode(200);
    }
}