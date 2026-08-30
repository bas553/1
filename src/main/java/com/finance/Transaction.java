package com.finance;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Transaction {

    private final IntegerProperty id;
    private final StringProperty date;
    private final StringProperty type;
    private final StringProperty account;
    private final StringProperty category;
    private final DoubleProperty amount;
    private final StringProperty description;

    public Transaction(
            int id,
            String date,
            String type,
            String account,
            String category,
            double amount,
            String description) {

        this.id = new SimpleIntegerProperty(id);
        this.date = new SimpleStringProperty(date);
        this.type = new SimpleStringProperty(type);
        this.account = new SimpleStringProperty(account);
        this.category = new SimpleStringProperty(category);
        this.amount = new SimpleDoubleProperty(amount);
        this.description = new SimpleStringProperty(description);
    }

    // =========================
    // ID
    // =========================

    public IntegerProperty idProperty() {
        return id;
    }

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    // =========================
    // Date
    // =========================

    public StringProperty dateProperty() {
        return date;
    }

    public String getDate() {
        return date.get();
    }

    public void setDate(String date) {
        this.date.set(date);
    }

    // =========================
    // Type
    // =========================

    public StringProperty typeProperty() {
        return type;
    }

    public String getType() {
        return type.get();
    }

    public void setType(String type) {
        this.type.set(type);
    }

    // =========================
    // Account
    // =========================

    public StringProperty accountProperty() {
        return account;
    }

    public String getAccount() {
        return account.get();
    }

    public void setAccount(String account) {
        this.account.set(account);
    }

    // =========================
    // Category
    // =========================

    public StringProperty categoryProperty() {
        return category;
    }

    public String getCategory() {
        return category.get();
    }

    public void setCategory(String category) {
        this.category.set(category);
    }

    // =========================
    // Amount
    // =========================

    public DoubleProperty amountProperty() {
        return amount;
    }

    public double getAmount() {
        return amount.get();
    }

    public void setAmount(double amount) {
        this.amount.set(amount);
    }

    // =========================
    // Description
    // =========================

    public StringProperty descriptionProperty() {
        return description;
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }
}