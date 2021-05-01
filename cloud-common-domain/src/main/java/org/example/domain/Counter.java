package org.example.domain;

/**
 * 'Action' event executes when count() called 'amount' times
 */
public class Counter {
    private int amount;
    private final VoidFunction action;

    public Counter(int amount, VoidFunction action) {
        assert amount > 0;
        assert action != null;

        this.amount = amount;
        this.action = action;
    }

    public void count() {
        if (amount <= 0) {
            throw new IllegalStateException("count exceeded amount");
        }
        amount--;
        if (amount == 0) {
            action.act();
        }
    }
}
