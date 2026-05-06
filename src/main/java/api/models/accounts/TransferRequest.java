package api.models.accounts;

import api.models.BaseModel;
import lombok.*;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferRequest extends BaseModel {
  private long senderAccountId;
  private long receiverAccountId;
  private double amount;
  private String description;
}
