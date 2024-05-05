package com.example.testapp;

public class TransactionSection {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_TRANSACTION = 1;

    private int type;
    private String date;
    private Transaction transaction;

    public TransactionSection(int type, String date) {
        this.type = type;
        this.date = date;
    }

    public TransactionSection(int type, Transaction transaction) {
        this.type = type;
        this.transaction = transaction;
    }

    public int getType() {
        return type;
    }

    public String getDate() {
        return date;
    }

    public Transaction getTransaction() {
        return transaction;
    }
}
