package api.models.accounts;

import api.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DepositResponseForFraud extends BaseModel {
  private long id;
  private String accountNumber;
  private double balance;
  //    private double depositAmount;
  private long transactionId;
}
