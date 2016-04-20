package com.coolbitx.coolwallet.entity;

/**
 * Created by wmgs_01 on 15/10/6.
 */
public class Wallet {

    private String status;
    private String name;
    private int accountIndex;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAccountIndex() {
        return accountIndex;
    }

    public void setAccountIndex(int accountIndex) {
        this.accountIndex = accountIndex;
    }
}
