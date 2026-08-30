package com.finance;

public class Transaction {
    private int id;
    private String date;
    private String type;
    private String account;
    private String category;
    private double amount;
    private String description;

    public Transaction(int id, String date, String type, String account, String category, double amount, String description) {
        this.id = id;
        this.date = date;
        this.type = type;
        this.account = account;
        this.category = category;
        this.amount = amount;
        this.description = description;
    }

    public int getId() { return id; }
    public String getDate() { return date; }
    public String getType() { return type; }
    public String getAccount() { return account; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
}