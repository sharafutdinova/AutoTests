package testexapmples;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

public class RequestParametersTest {
    @Test
    public void testGetUsersWithParams() {
        given()
                .queryParam("age", 30)  // Добавляем параметр ?age=30
                .pathParam("id", 123)  // Подставляем в путь /users/{id}
                .when()
                .get("/users/{id}")
                .then()
                .statusCode(200);
    }
}