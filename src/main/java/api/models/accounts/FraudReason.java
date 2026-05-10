package api.models.accounts;

public enum FraudReason {
    SERVER_ERROR("500 Server Error"),
    BAD_REQUEST("400 Bad Request"),
    UNEXPECTED_ERROR("Unexpected error during fraud check: %s: \"{<EOL>  \"status\": \"%s\",<EOL>  \"decision\": \"%s\",<EOL>  \"riskScore\": \"%s\",<EOL>  \"reason\": \"%s\",<EOL>  \"requiresManualReview\": %s,<EOL>  \"additionalVerificationRequired\": %s<EOL>}\"");

    private String description;

    FraudReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
