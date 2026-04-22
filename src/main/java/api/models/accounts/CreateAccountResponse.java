package api.models.accounts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import api.models.BaseModel;
import api.models.Transaction;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateAccountResponse extends BaseModel {
    private long id;
    private String accountNumber;
    private double balance;
    private List<Transaction> transactions;
}
