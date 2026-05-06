package api.models.accounts;

import api.models.BaseModel;
import lombok.*;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferResponseForFraud extends BaseModel {
  private String status;
  private String message;
  private Long transactionId;
  private Long senderAccountId;
  private Long receiverAccountId;
  private double amount;
  private double fraudRiskScore;
  private String fraudReason;
  private boolean requiresVerification;
  private boolean requiresManualReview;
}
