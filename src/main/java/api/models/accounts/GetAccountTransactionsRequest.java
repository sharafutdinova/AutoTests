package api.models.accounts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import api.models.BaseModel;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetAccountTransactionsRequest extends BaseModel {
    private Map<String, Long> params = new HashMap<>();
    public GetAccountTransactionsRequest(Long accountId) {
        params.put("accountId", accountId);
    }
}
