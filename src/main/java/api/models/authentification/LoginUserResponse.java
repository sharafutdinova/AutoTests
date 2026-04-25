package api.models.authentification;

import lombok.*;
import api.models.BaseModel;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginUserResponse extends BaseModel {
    private String username;
    private String role;
}
