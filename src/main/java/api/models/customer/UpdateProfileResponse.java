package api.models.customer;

import api.models.BaseModel;
import api.models.admin.CreateUserResponse;
import lombok.*;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateProfileResponse extends BaseModel {
  private CreateUserResponse customer;
  private String message;
}
