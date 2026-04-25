package api.models.customer;

import lombok.*;
import api.models.Account;
import api.models.BaseModel;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetUserResponse extends BaseModel {
    private long id;
    private String username;
    private String password;
    private String name;
    private String role;
    private List<Account> accounts;
}
