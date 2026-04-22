package api.models.accounts;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import api.models.BaseModel;
import api.models.Transaction;

import java.util.List;

@Data
@NoArgsConstructor
@Builder
public class GetAccountTransactionsResponse extends BaseModel {
    private List<Transaction> transactions;
    @JsonCreator
    public GetAccountTransactionsResponse(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}
