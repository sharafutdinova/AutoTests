package api.models.accounts;

import api.models.BaseModel;
import lombok.*;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepositRequestForFraud extends BaseModel {
    private long accountId;
    private double amount;
    private String description;
}
