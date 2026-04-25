package iteration2.api;

import api.models.admin.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import iteration2.BaseTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class BaseAPITest extends BaseTest {
    protected CreateUserRequest userRequest;
    protected UserSteps userSteps;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        userRequest = AdminSteps.createUser();
        userSteps = new UserSteps(userRequest);
    }

    @AfterEach
    @Override
    public void afterTest() {
        super.afterTest();
        AdminSteps.deleteUserByCreateUserRequest(userRequest);
    }
}
