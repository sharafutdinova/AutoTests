package requests.steps;

import models.Transaction;
import models.accounts.*;
import models.admin.CreateUserRequest;
import models.customer.GetUserResponse;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Comparator;

import static javax.swing.UIManager.get;

public class UserSteps {

    public static GetUserResponse getUserResponse(CreateUserRequest userRequest) {
        return new ValidatedCrudRequester<GetUserResponse>(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.GET_CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK()).get();
    }

    public static CreateAccountResponse createAccount(CreateUserRequest userRequest) {
        return new ValidatedCrudRequester<CreateAccountResponse>(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post();
    }

    public static void deposit(CreateUserRequest userRequest, long accountId, double amount) {
        DepositRequest depositRequest = DepositRequest.builder().id(accountId).balance(amount).build();

        new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK()).post(depositRequest).extract().body().as(DepositResponse.class);
    }

    public static GetAccountTransactionsResponse getAccountTransactions(CreateUserRequest userRequest, long accountId) {
        GetAccountTransactionsRequest getAccountTransactionsRequest = new GetAccountTransactionsRequest(accountId);
        return new ValidatedCrudRequester<GetAccountTransactionsResponse>(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNT_TRANSACTIONS,
                ResponseSpecs.requestReturnsOK()).get(getAccountTransactionsRequest.getParams());
    }

    public static Transaction getAccountLastTransactions(CreateUserRequest userRequest, long accountId) {
        GetAccountTransactionsRequest getAccountTransactionsRequest = new GetAccountTransactionsRequest(accountId);
        GetAccountTransactionsResponse accountTransactionsResponse = new ValidatedCrudRequester<GetAccountTransactionsResponse>(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNT_TRANSACTIONS,
                ResponseSpecs.requestReturnsOK()).get(getAccountTransactionsRequest.getParams());
        return accountTransactionsResponse.getTransactions().stream().max(Comparator.comparing(Transaction::getId)).get();
    }
}
