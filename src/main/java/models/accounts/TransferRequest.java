package models.accounts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import models.BaseModel;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferRequest extends BaseModel {
    private long senderAccountId;
    private long receiverAccountId;
    private double amount;
}
