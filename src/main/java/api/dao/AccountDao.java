package api.dao;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class AccountDao {
    private Long id;
    private String accountNumber;
    private Double balance;
    private Long customerId;
}