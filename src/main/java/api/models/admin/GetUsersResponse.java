package api.models.admin;

import api.models.BaseModel;
import java.util.List;
import lombok.*;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetUsersResponse extends BaseModel {
  private List<CreateUserResponse> users;
}
