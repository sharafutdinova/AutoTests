package api.models.accounts;

import lombok.*;
import api.models.BaseModel;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferResponse extends BaseModel {
    private long senderAccountId;
    private long receiverAccountId;
    private double amount;
    private String message;
}
