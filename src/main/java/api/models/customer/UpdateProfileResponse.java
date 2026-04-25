package api.models.customer;

import lombok.*;
import api.models.BaseModel;
import api.models.Customer;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateProfileResponse extends BaseModel {
    private Customer customer;
    private String message;
}
