package com.coolbitx.coolwallet.entity;

import java.sql.Date;

/**
 * Created by ShihYi on 2015/12/30.
 */
public class CWAccount {

    private int accId;
    private String accName;
    private double balance; //shatoshi (sum of address balance)
    private double blockAmount;
    private int extKeyPointer;
    private int intKeyPointer;

    private   String[] extKeys = {
    };
    private   String[] intKeys = {
    };

    private Date lastUpdate;

    public int getAccId() {
        return accId;
    }

    public void setAccId(int accId) {
        this.accId = accId;
    }

    public String getAccName() {
        return accName;
    }

    public void setAccName(String accName) {
        this.accName = accName;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getBlockAmount() {
        return blockAmount;
    }

    public void setBlockAmount(double blockAmount) {
        this.blockAmount = blockAmount;
    }

    public int getExtKeyPointer() {
        return extKeyPointer;
    }

    public void setExtKeyPointer(int extKeyPointer) {
        this.extKeyPointer = extKeyPointer;
    }

    public int getIntKeyPointer() {
        return intKeyPointer;
    }

    public void setIntKeyPointer(int intKeyPointer) {
        this.intKeyPointer = intKeyPointer;
    }

    public String[] getExtKeys() {
        return extKeys;
    }

    public void setExtKeys(String[] extKeys) {
        this.extKeys = extKeys;
    }

    public String[] getIntKeys() {
        return intKeys;
    }

    public void setIntKeys(String[] intKeys) {
        this.intKeys = intKeys;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
