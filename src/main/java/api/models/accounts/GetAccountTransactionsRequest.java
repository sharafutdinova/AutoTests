package api.models.accounts;

import api.models.BaseModel;
import java.util.HashMap;
import java.util.Map;
import lombok.*;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetAccountTransactionsRequest extends BaseModel {
  private Map<String, Long> params = new HashMap<>();

  public GetAccountTransactionsRequest(Long accountId) {
    params.put("accountId", accountId);
  }
}
