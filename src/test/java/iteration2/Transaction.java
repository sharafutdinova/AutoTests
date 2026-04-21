package iteration2;

import java.util.Objects;

public class Transaction {
    private int id;
    private double amount;
    private String type;
    private String timestamp;
    private int relatedAccountId;

    public Transaction(int id, double amount, String type, String timestamp, int relatedAccountId) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
        this.relatedAccountId = relatedAccountId;
    }

    public double getAmount() {
        return amount;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return id == that.id && Double.compare(amount, that.amount) == 0 && relatedAccountId == that.relatedAccountId && Objects.equals(type, that.type) && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount, type, timestamp, relatedAccountId);
    }
}
