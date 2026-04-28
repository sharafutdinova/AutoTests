package api.models.accounts;

import lombok.*;
import api.models.BaseModel;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferRequest extends BaseModel {
    private long senderAccountId;
    private long receiverAccountId;
    private double amount;
}
