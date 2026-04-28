package api.models.comparison;

import api.models.Transaction;
import api.models.TransactionTypes;
import api.models.accounts.DepositRequest;
import api.models.accounts.DepositResponse;
import api.models.accounts.*;

import java.util.Comparator;

public class TransactionsComparing {
    public static boolean validateDepositTransaction(DepositRequest depositRequest, DepositResponse depositResponse) {
        Transaction lastTransaction = depositResponse.getTransactions().stream().max(Comparator.comparing(Transaction::getId)).get();
        return depositRequest.getBalance() == depositResponse.getBalance()
                && lastTransaction.validateTransaction(TransactionTypes.TRANSACTION_TYPE_FOR_DEPOSIT, depositRequest.getBalance());
    }
}
