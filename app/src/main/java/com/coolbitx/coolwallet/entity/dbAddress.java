package com.coolbitx.coolwallet.entity;

/**
 * Created by ShihYi on 2016/2/19.
 */
public class dbAddress {
    String wid;
    int accountID;
    int kcid;
    int kid;
    String address;
    int n_tx;
    long balance;
    private String addLabel;
    private String BCAddress;

    public int getKid() {
        return kid;
    }

    public void setKid(int kid) {
        this.kid = kid;
    }

    public void setBCAddress(String BCAdress) {
        this.BCAddress = BCAdress;
    }

    public String getBCAddress() {
        return BCAddress;
    }

    public String getAddLabel() {
        return addLabel;
    }

    public void setAddLabel(String addLabel) {
        this.addLabel = addLabel;
    }

    public String getWid() {
        return wid;
    }

    public void setWid(String wid) {
        this.wid = wid;
    }

    public int getAccountID() {
        return accountID;
    }

    public void setAccountID(int accountID) {
        this.accountID = accountID;
    }

    public int getKcid() {
        return kcid;
    }

    public void setKcid(int kcid) {
        this.kcid = kcid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getN_tx() {
        return n_tx;
    }

    public void setN_tx(int n_tx) {
        this.n_tx = n_tx;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }
}
