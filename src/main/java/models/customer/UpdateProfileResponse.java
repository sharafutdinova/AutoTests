package models.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import models.BaseModel;
import models.Customer;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateProfileResponse extends BaseModel {
    private Customer customer;
    private String message;
}
