package api.models.admin;

import api.configs.Config;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import api.models.BaseModel;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserRequest extends BaseModel {
    private String username;
    private String password;
    private String role;
    public static CreateUserRequest getAdmin(){
        return CreateUserRequest.builder()
                .username(Config.getProperty("admin.username"))
                .password(Config.getProperty("admin.password")).build();
    }
}
