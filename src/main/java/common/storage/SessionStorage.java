package common.storage;

import api.models.admin.CreateUserRequest;
import api.requests.steps.UserSteps;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SessionStorage {
    private static final SessionStorage INSTANCE = new SessionStorage();
    private final LinkedHashMap<CreateUserRequest, UserSteps> userStepMap = new LinkedHashMap<>();

    private SessionStorage() {
    }

    public static void addUsers(List<CreateUserRequest> users) {
        for (CreateUserRequest user : users) {
            INSTANCE.userStepMap.put(user, new UserSteps(user.getUsername(), user.getPassword()));
        }
    }

    public static CreateUserRequest getUser(int index) {
        return new ArrayList<>(INSTANCE.userStepMap.keySet()).get(index);
    }

    public static CreateUserRequest getUser() {
        return getUser(0);
    }

    public static UserSteps getSteps(int index) {
        return new ArrayList<>(INSTANCE.userStepMap.values()).get(index);
    }

    public static UserSteps getSteps() {
        return getSteps(0);
    }

    public static void clear() {
        INSTANCE.userStepMap.clear();
    }

    public static LinkedHashMap<CreateUserRequest, UserSteps> getUserStepMap() {
        return INSTANCE.userStepMap;
    }
}
