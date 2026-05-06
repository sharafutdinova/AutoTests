package api.models.admin;

import api.models.BaseModel;
import api.models.accounts.CreateAccountResponse;
import java.util.List;
import lombok.*;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserResponse extends BaseModel {
  private long id;
  private String username;
  private String password;
  private String name;
  private String role;
  private List<CreateAccountResponse> accounts;
}
