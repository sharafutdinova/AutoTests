package api.models.admin;

import lombok.*;
import api.models.BaseModel;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetUsersResponse extends BaseModel {
    private List<CreateUserResponse> users;
}
