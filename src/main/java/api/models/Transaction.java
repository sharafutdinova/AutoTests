package api.models;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Transaction extends BaseModel {
    private long id;
    private double amount;
    private String type;
    private String timestamp;
    private String timestampAsString;
    private long relatedAccountId;
    private double amountAsDouble;

    public boolean validateTransaction(TransactionTypes transactionTypes, double amount) {
        boolean isTypeValid = switch (transactionTypes) {
            case TRANSACTION_TYPE_FOR_DEPOSIT->
                this.type.equals(TransactionTypes.TRANSACTION_TYPE_FOR_DEPOSIT.getDescription());
            case TRANSACTION_TYPE_FOR_TRANSFER_IN->
                this.type.equals(TransactionTypes.TRANSACTION_TYPE_FOR_TRANSFER_IN.getDescription());
            case TRANSACTION_TYPE_FOR_TRANSFER_OUT->
                this.type.equals(TransactionTypes.TRANSACTION_TYPE_FOR_TRANSFER_OUT.getDescription());
        };
        return isTypeValid && this.amount == amount;
    }
}
