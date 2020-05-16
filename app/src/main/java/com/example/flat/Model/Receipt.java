package com.example.flat.Model;

public class Receipt {
    String name, receipt_name, status;
    boolean inuse;

    public Receipt() {
    }

    public Receipt(String name, String receipt_name, String status, boolean inuse) {
        this.name = name;
        this.receipt_name = receipt_name;
        this.status = status;
        this.inuse = inuse;
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
