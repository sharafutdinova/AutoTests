package api.models;

public enum Messages {
  PROFILE_UPDATED_SUCCESSFULLY("Profile updated successfully"),
  PROFILE_UPDATE_ERROR("Name must contain two words with letters only"),
  TRANSFER_SUCCESSFUL("Transfer successful"),
  TRANSFER_INVALID("Invalid transfer: insufficient funds or invalid accounts"),
  UNAUTHORIZED_ERROR("Invalid username or password"),
  FORBIDDEN_ERROR("Unauthorized access to account"),
  USER_DELETED("User with ID %s deleted successfully."),
  USER_NOT_FOUND("Error: User with ID %s not found.");

  private String message;

  Messages(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
