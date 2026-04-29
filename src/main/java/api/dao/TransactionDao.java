package api.dao;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class TransactionDao {
    private long id;
    private double amount;
    private String type;
    private String timestamp;
    private long accountId;
    private long relatedAccountId;
    private String createdAt;
}
