package models.customer;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import models.Account;
import models.BaseModel;

import java.util.List;

@Data
@NoArgsConstructor
@Builder
public class GetAccountsResponse extends BaseModel {
    private List<Account> accounts;

    @JsonCreator
    public GetAccountsResponse(List<Account> accounts) {
        this.accounts = accounts;
    }
}
