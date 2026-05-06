package api.models.comparison;

import api.models.Messages;
import api.models.customer.UpdateProfileRequest;
import api.models.customer.UpdateProfileResponse;

public class UserNameComparing {
  public static boolean validateUpdateProfileResponse(
      UpdateProfileRequest updateProfileRequest, UpdateProfileResponse updateProfileResponse) {
    return updateProfileRequest.getName().equals(updateProfileResponse.getCustomer().getName())
        && Messages.PROFILE_UPDATED_SUCCESSFULLY
            .getMessage()
            .equals(updateProfileResponse.getMessage());
  }
}
