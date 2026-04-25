package api.models.customer;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import api.models.Account;
import api.models.BaseModel;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
public class GetAccountsResponse extends BaseModel {
    private List<Account> accounts;

    @JsonCreator
    public GetAccountsResponse(List<Account> accounts) {
        this.accounts = accounts;
    }
}
