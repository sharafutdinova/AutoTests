package api.models.customer;

import api.models.admin.CreateUserResponse;
import lombok.*;
import api.models.BaseModel;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateProfileResponse extends BaseModel {
    private CreateUserResponse customer;
    private String message;
}
