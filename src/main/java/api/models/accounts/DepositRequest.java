package api.models.accounts;

import lombok.*;
import api.models.BaseModel;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepositRequest extends BaseModel {
    private long id;
    private double balance;
}
