package api.models.accounts;

import api.models.BaseModel;
import api.models.Transaction;
import java.util.List;
import lombok.*;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepositResponse extends BaseModel {
  private long id;
  private String accountNumber;
  private double balance;
  private List<Transaction> transactions;
}
