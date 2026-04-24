package common.extensions;

import api.models.admin.CreateUserRequest;
import api.requests.steps.AdminSteps;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import ui.pages.BasePage;

import java.util.LinkedList;
import java.util.List;

public class UserSessionExtension implements BeforeEachCallback, AfterEachCallback {
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        UserSession annotation = context.getRequiredTestMethod().getAnnotation(UserSession.class);
        if (annotation != null) {
            int userCount = annotation.value();
            SessionStorage.clear();
            List<CreateUserRequest> users = new LinkedList<>();
            for (int i = 0; i < userCount; i++) {
                CreateUserRequest createUserRequest = AdminSteps.createUser();
                users.add(createUserRequest);
            }
            SessionStorage.addUsers(users);
            int auth = annotation.auth();
            BasePage.authAsUser(SessionStorage.getUser(auth));
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        UserSession annotation = context.getRequiredTestMethod().getAnnotation(UserSession.class);
        if (annotation != null) {
            int count = SessionStorage.getUserStepMap().size();
            for (int i = 0; i < count; i++) {
                AdminSteps.deleteUserByCreateUserRequest(SessionStorage.getUser(i));
            }
        }
    }
}
