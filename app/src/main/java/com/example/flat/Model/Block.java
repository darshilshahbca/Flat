package com.example.flat.Model;

public class Block {
    private String  owner, amount, ocontact, renter, rcontact;
    boolean inuse, onrent;

    public Block() {
    }

    public Block(String owner, String amount, String ocontact, String renter, String rcontact, boolean inuse, boolean onrent) {
        this.owner = owner;
        this.amount = amount;
        this.ocontact = ocontact;
        this.renter = renter;
        this.rcontact = rcontact;
        this.inuse = inuse;
        this.onrent = onrent;
    }

    public boolean isOnrent() {
        return onrent;
    }

    public void setOnrent(boolean onrent) {
        this.onrent = onrent;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getOcontact() {
        return ocontact;
    }

    public void setOcontact(String ocontact) {
        this.ocontact = ocontact;
    }

    public String getRenter() {
        return renter;
    }

    public void setRenter(String renter) {
        this.renter = renter;
    }

    public String getRcontact() {
        return rcontact;
    }

    public void setRcontact(String rcontact) {
        this.rcontact = rcontact;
    }

    public boolean isInuse() {
        return inuse;
    }

    public void setInuse(boolean inuse) {
        this.inuse = inuse;
    }
}
