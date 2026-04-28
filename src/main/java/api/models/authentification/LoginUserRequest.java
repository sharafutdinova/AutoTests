package api.models.authentification;

import lombok.*;
import api.models.BaseModel;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginUserRequest extends BaseModel {
    private String username;
    private String password;
}
