package api.models.accounts;

public enum TransferStatus {
    APPROVED("APPROVED"),
    VERIFICATION_REQUIRED("VERIFICATION_REQUIRED"),
    MANUAL_REVIEW_REQUIRED("MANUAL_REVIEW_REQUIRED");

    private String description;

    TransferStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
