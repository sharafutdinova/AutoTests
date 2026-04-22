package iteration2.api;

import api.models.admin.CreateUserRequest;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import api.requests.steps.AdminSteps;

public class BaseTest {
    protected SoftAssertions softly;
    protected CreateUserRequest userRequest;

    @BeforeEach
    public void setUp() {
        softly = new SoftAssertions();
        userRequest = AdminSteps.createUser();
    }

    @AfterEach
    public void afterTest() {
        softly.assertAll();
        AdminSteps.deleteUserByCreateUserRequest(userRequest);
    }
}
