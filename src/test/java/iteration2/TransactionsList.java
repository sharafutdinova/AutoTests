package iteration2;

import java.util.List;
import java.util.Objects;

public class TransactionsList {
    private int id;
    private String accountNumber;
    private double balance;
    private List<Transaction> transactions;

    public TransactionsList(int id, String accountNumber, double balance, List<Transaction> transactions) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.transactions = transactions;
    }

    public double getBalance() {
        return balance;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TransactionsList that = (TransactionsList) o;
        return id == that.id && Double.compare(balance, that.balance) == 0 && Objects.equals(accountNumber, that.accountNumber) && Objects.equals(transactions, that.transactions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accountNumber, balance, transactions);
    }
}
