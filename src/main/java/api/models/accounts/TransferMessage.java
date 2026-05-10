package api.models.accounts;

public enum TransferMessage {
    TRANSFER_APPROVED_AND_PROCESSED_IMMEDIATELY("Transfer approved and processed immediately"),
    ADDITIONAL_VERIFICATION_REQUIRED("Additional verification required"),
    TRANSFER_REQUIRES_MANUAL_REVIEW("Transfer requires manual review");

    private String description;

    TransferMessage(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
