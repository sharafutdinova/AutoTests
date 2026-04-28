package ui.pages;

import lombok.Getter;

@Getter
public enum BankAlert {
    USER_CREATED_SUCCESSFULLY("✅ User created successfully!"),
    USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS("Username must be between 3 and 15 characters"),
    NEW_ACCOUNT_CREATED("✅ New Account Created! Account Number: "),
    NAME_UPDATED_SUCCESSFULLY("✅ Name updated successfully!"),
    NAME_MUST_CONTAIN_TWO_WORDS("Name must contain two words with letters only"),
    ENTER_VALID_NAME("❌ Please enter a valid name."),
    SUCCESSFULLY_DEPOSITED("✅ Successfully deposited $%s to account %s!"),
    SUCCESSFULLY_TRANSFERRED("✅ Successfully transferred $%s to account %s!"),//
    DEPOSIT_LESS_OR_EQUAL_5000("❌ Please deposit less or equal to 5000$."),
    ENTER_A_VALID_AMOUNT("❌ Please enter a valid amount."),
    SELECT_AN_ACCOUNT("❌ Please select an account."),
    RECIPIENT_NAME_DOES_NOT_MATCH_THE_REGISTERED_NAME("❌ The recipient name does not match the registered name."),
    ERROR_INVALID_TRANSFER("❌ Error: Invalid transfer: insufficient funds or invalid accounts"),
    FILL_ALL_FIELDS_AND_CONFIRM("❌ Please fill all fields and confirm.");

    private final String message;

    BankAlert(String message) {
        this.message = message;
    }
}
