package common.storage;

import api.models.admin.CreateUserRequest;
import api.requests.steps.UserSteps;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SessionStorage {
    private static final ThreadLocal<SessionStorage> INSTANCE = ThreadLocal.withInitial(SessionStorage::new);
    private final LinkedHashMap<CreateUserRequest, UserSteps> userStepMap = new LinkedHashMap<>();

    private SessionStorage() {
    }

    public static void addUsers(List<CreateUserRequest> users) {
        for (CreateUserRequest user : users) {
            INSTANCE.get().userStepMap.put(user, new UserSteps(user));
        }
    }

    public static void addUser(CreateUserRequest user) {
        INSTANCE.get().userStepMap.put(user, new UserSteps(user));
    }

    public static CreateUserRequest getUser(int index) {
        return new ArrayList<>(INSTANCE.get().userStepMap.keySet()).get(index);
    }

    public static CreateUserRequest getUser() {
        return getUser(0);
    }

    public static UserSteps getSteps(int index) {
        return new ArrayList<>(INSTANCE.get().userStepMap.values()).get(index);
    }

    public static UserSteps getSteps() {
        return getSteps(0);
    }

    public static void clear() {
        INSTANCE.get().userStepMap.clear();
    }

    public static LinkedHashMap<CreateUserRequest, UserSteps> getUserStepMap() {
        return INSTANCE.get().userStepMap;
    }
}
