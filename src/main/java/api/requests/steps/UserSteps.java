package api.requests.steps;

import api.models.Account;
import api.models.Transaction;
import api.models.accounts.*;
import api.models.admin.CreateUserRequest;
import api.models.customer.GetUserResponse;
import api.models.customer.GetAccountsResponse;
import api.models.customer.UpdateProfileRequest;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.Comparator;

public class UserSteps {
    private String username;
    private String password;

    public UserSteps(CreateUserRequest userRequest) {
        this.username = userRequest.getUsername();
        this.password = userRequest.getPassword();
    }

    public GetUserResponse getUserResponse() {
        return new ValidatedCrudRequester<GetUserResponse>(RequestSpecs.authAsUser(this.username, this.password),
                Endpoint.GET_CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK()).get();
    }

    public CreateAccountResponse createAccount() {
        return new ValidatedCrudRequester<CreateAccountResponse>(RequestSpecs.authAsUser(this.username, this.password),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post();
    }

    public void deposit(long accountId, double amount) {
        DepositRequest depositRequest = DepositRequest.builder().id(accountId).balance(amount).build();

        new CrudRequester(RequestSpecs.authAsUser(this.username, this.password),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK()).post(depositRequest).extract().body().as(DepositResponse.class);
    }

    public GetAccountTransactionsResponse getAccountTransactions(long accountId) {
        GetAccountTransactionsRequest getAccountTransactionsRequest = new GetAccountTransactionsRequest(accountId);
        return new ValidatedCrudRequester<GetAccountTransactionsResponse>(RequestSpecs.authAsUser(this.username, this.password),
                Endpoint.ACCOUNT_TRANSACTIONS,
                ResponseSpecs.requestReturnsOK()).get(getAccountTransactionsRequest.getParams());
    }

    public Transaction getAccountLastTransactions(long accountId) {
        GetAccountTransactionsRequest getAccountTransactionsRequest = new GetAccountTransactionsRequest(accountId);
        GetAccountTransactionsResponse accountTransactionsResponse = new ValidatedCrudRequester<GetAccountTransactionsResponse>(RequestSpecs.authAsUser(this.username, this.password),
                Endpoint.ACCOUNT_TRANSACTIONS,
                ResponseSpecs.requestReturnsOK()).get(getAccountTransactionsRequest.getParams());
        return accountTransactionsResponse.getTransactions().stream().max(Comparator.comparing(Transaction::getId)).get();
    }

    public GetAccountsResponse getCustomerAccounts() {
        GetAccountsResponse getAccountsResponse = new ValidatedCrudRequester<GetAccountsResponse>(RequestSpecs.authAsUser(this.username, this.password),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnsOK()).get();
        return getAccountsResponse;
    }

    public Account getCustomerAccount(long id) {
        GetAccountsResponse getAccountsResponse = getCustomerAccounts();
        return getAccountsResponse.getAccounts().stream().filter(account1 -> account1.getId() == id).findFirst().get();
    }

    public void changeName(String newName) {
        UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder()
                .name(newName)
                .build();
        new CrudRequester(RequestSpecs.authAsUser(this.username, this.password),
                Endpoint.UPDATE_CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .update(updateProfileRequest);
    }
}