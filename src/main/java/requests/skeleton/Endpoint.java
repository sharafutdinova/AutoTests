package requests.skeleton;

import lombok.AllArgsConstructor;
import lombok.Getter;
import models.BaseModel;
import models.accounts.*;
import models.admin.CreateUserRequest;
import models.admin.CreateUserResponse;
import models.authentification.LoginUserRequest;
import models.authentification.LoginUserResponse;
import models.customer.UpdateProfileRequest;
import models.customer.UpdateProfileResponse;
import models.customer.GetAccountsResponse;
import models.customer.GetUserResponse;

@AllArgsConstructor
@Getter
public enum Endpoint {
    ADMIN_USER("/admin/users", CreateUserRequest.class, CreateUserResponse.class),
    ADMIN_USERS("/admin/users", BaseModel.class, CreateUserResponse.class),
    ADMIN_DELETE_USER("/admin/users/{id}", BaseModel.class, BaseModel.class),
    ACCOUNTS("/accounts", BaseModel.class, CreateAccountResponse.class),
    LOGIN("/auth/login", LoginUserRequest.class, LoginUserResponse.class),
    GET_CUSTOMER_PROFILE("/customer/profile", BaseModel.class, GetUserResponse.class),
    UPDATE_CUSTOMER_PROFILE("/customer/profile", UpdateProfileRequest.class, UpdateProfileResponse.class),
    CUSTOMER_ACCOUNTS("/customer/accounts", BaseModel.class, GetAccountsResponse.class),
    DEPOSIT("/accounts/deposit", DepositRequest.class, DepositResponse.class),
    TRANSFER("/accounts/transfer", TransferRequest.class, TransferResponse.class),
    ACCOUNT_TRANSACTIONS("/accounts/{accountId}/transactions", GetAccountTransactionsRequest.class, GetAccountTransactionsResponse.class);

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}
