package api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {
    private long id;
    private double amount;
    private String type;
    private String timestamp;
    private long relatedAccountId;

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
