package iteration2.changeusername;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChangeUserNameTest extends TestSetup {
    /*
    User can change name to name contains two words
    User can change name to the same name
    User can change name to name contains two words with length 1 with russian letters
     */
    @ParameterizedTest
    @ValueSource(strings = {"Alsu Sharaf", "Alsu Sharaf", "а ш"})
    public void userCanChangeNameTest(String name) {
        String userName = "AlsuSha";
        JSONObject requestParams = new JSONObject();
        requestParams.put("name", name);
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("Authorization", getUserToken())
                .body(requestParams.toString())
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("customer.username", equalTo(userName))
                .body("customer.name", equalTo(name));
        CustomerProfile customerProfile = getCustomerProfile();
        assertEquals(name, customerProfile.getName());
        assertEquals(userName, customerProfile.getUsername());
    }

    /*
    User cannot change name to empty value
    User cannot change name to value contains only spaces
    User cannot change name to value contains number
    User cannot change name to value contains special symbol
    User cannot change name to value contains only one word
    User cannot change name to value separated by two spaces
    User cannot change name to value separated by _
     */
    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "Alsu1 test2", "Alsu!@#$%%^&*() test_+/,<>?}{[]';/.!", "Alsutest", "Alsu  test", "Alsu_test"})
    public void userCannotChangeNameToInvalidValueTest(String name) {
        CustomerProfile customerProfileBefore = getCustomerProfile();
        JSONObject requestParams = new JSONObject();
        requestParams.put("name", name);
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("Authorization", getUserToken())
                .body(requestParams.toString())
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(400)
                .body(equalTo("Name must contain two words with letters only"));
        CustomerProfile customerProfileAfter = getCustomerProfile();
        assertEquals(customerProfileBefore, customerProfileAfter);
    }

    @Test
    public void userCannotChangeNameToNullTest() {
        CustomerProfile customerProfileBefore = getCustomerProfile();
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("Authorization", getUserToken())
                .body("""                        
                        {
                          "name": null
                        }
                        """)
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(400)
                .body(equalTo("Name must contain two words with letters only"));
        CustomerProfile customerProfileAfter = getCustomerProfile();
        assertEquals(customerProfileBefore, customerProfileAfter);
    }

    @Test
    public void userCannotChangeNameWithWrongTokenTest() {
        CustomerProfile customerProfileBefore = getCustomerProfile();
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("Authorization", getUserToken() + 1)
                .body("""                        
                        {
                          "name": null
                        }
                        """)
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(401);
        CustomerProfile customerProfileAfter = getCustomerProfile();
        assertEquals(customerProfileBefore, customerProfileAfter);
    }
}
