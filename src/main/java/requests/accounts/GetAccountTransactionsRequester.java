package requests.accounts;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import models.accounts.GetAccountTransactionsRequest;
import models.accounts.TransferRequest;
import requests.Request;

import static io.restassured.RestAssured.given;

public class GetAccountTransactionsRequester extends Request<GetAccountTransactionsRequest> {
    public GetAccountTransactionsRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(GetAccountTransactionsRequest model) {
        return given()
                .spec(requestSpecification)
                .pathParam("accountId", model.getAccountId())
                .get("/api/v1/accounts/{accountId}/transactions")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
