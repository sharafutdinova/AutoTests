package api.models.admin;

import lombok.*;
import api.models.BaseModel;
import api.models.Customer;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetUsersResponse extends BaseModel {
    private List<Customer> users;
}
