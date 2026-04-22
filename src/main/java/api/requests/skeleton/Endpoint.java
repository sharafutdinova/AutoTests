package api.requests.skeleton;

import api.models.accounts.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import api.models.BaseModel;
import api.models.admin.CreateUserRequest;
import api.models.admin.CreateUserResponse;
import api.models.authentification.LoginUserRequest;
import api.models.authentification.LoginUserResponse;
import api.models.customer.UpdateProfileRequest;
import api.models.customer.UpdateProfileResponse;
import api.models.customer.GetAccountsResponse;
import api.models.customer.GetUserResponse;

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
