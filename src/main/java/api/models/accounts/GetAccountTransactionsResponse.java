package api.models.accounts;

import api.models.BaseModel;
import api.models.Transaction;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = false)
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
