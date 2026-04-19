package models.comparison;

import models.Messages;
import models.customer.UpdateProfileRequest;
import models.customer.UpdateProfileResponse;

public class UserNameComparing {
    public static boolean validateUpdateProfileResponse(UpdateProfileRequest updateProfileRequest, UpdateProfileResponse updateProfileResponse){
        return updateProfileRequest.getName().equals(updateProfileResponse.getCustomer().getName())
                && Messages.PROFILE_UPDATED_SUCCESSFULLY.getMessage().equals(updateProfileResponse.getMessage());
    }
}
