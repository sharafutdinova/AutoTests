package api.models;

public enum TransactionTypes {
  TRANSACTION_TYPE_FOR_DEPOSIT("DEPOSIT"),
  TRANSACTION_TYPE_FOR_TRANSFER_IN("TRANSFER_IN"),
  TRANSACTION_TYPE_FOR_TRANSFER_OUT("TRANSFER_OUT");

  private String description;

  TransactionTypes(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
