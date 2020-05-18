package com.example.flat.Model;

public class Receipt {
    String name, receipt_name, status, payment;
    int amount,flatamount;
    boolean inuse;

    public Receipt() {
    }

    public Receipt(String name, String receipt_name, String status, String payment, int amount, int flatamount, boolean inuse) {
        this.name = name;
        this.receipt_name = receipt_name;
        this.status = status;
        this.payment = payment;
        this.amount = amount;
        this.flatamount = flatamount;
        this.inuse = inuse;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getFlatamount() {
        return flatamount;
    }

    public void setFlatamount(int flatamount) {
        this.flatamount = flatamount;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReceipt_name() {
        return receipt_name;
    }

    public void setReceipt_name(String receipt_name) {
        this.receipt_name = receipt_name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isInuse() {
        return inuse;
    }

    public void setInuse(boolean inuse) {
        this.inuse = inuse;
    }
}
