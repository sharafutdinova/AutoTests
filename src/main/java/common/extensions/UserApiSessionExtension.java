package common.extensions;

import api.requests.steps.AdminSteps;
import common.annotations.UserApiSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class UserApiSessionExtension implements BeforeEachCallback, AfterEachCallback {
    @Override
    public void beforeEach(ExtensionContext context) {
        UserApiSession annotation = context.getRequiredTestMethod().getAnnotation(UserApiSession.class);
        if (annotation != null) {
            SessionStorage.clear();
            SessionStorage.addUser(AdminSteps.createUser());
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        UserApiSession annotation = context.getRequiredTestMethod().getAnnotation(UserApiSession.class);
        if (annotation != null) {
            int count = SessionStorage.getUserStepMap().size();
            for (int i = 0; i < count; i++) {
                AdminSteps.deleteUserByCreateUserRequest(SessionStorage.getUser(i));
            }
        }
    }
}
