package api.models.accounts;

import api.models.BaseModel;
import lombok.*;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FraudCheckResponse extends BaseModel {
  private String transactionId;
  private String status;
  private String message;
  private String fraudCheckStatus;
  private String details;
}
