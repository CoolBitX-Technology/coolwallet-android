package com.coolbitx.coolwallet.entity;

import java.sql.Date;

/**
 * Created by ShihYi on 2015/12/30.
 */
public class CwTx {

    private String tx;
    private Date historyTime_utc;
    private int confirmations;
    private int amount_btc;
    private int amount_multisig;
    private Boolean isCompleted;
    private int historyAmount;

    public int getHistoryAmount() {
        return historyAmount;
    }

    public void setHistoryAmount(int historyAmount) {
        this.historyAmount = historyAmount;
    }

    public String getTx() {
        return tx;
    }

    public void setTx(String tx) {
        this.tx = tx;
    }

    public Date getHistoryTime_utc() {
        return historyTime_utc;
    }

    public void setHistoryTime_utc(Date historyTime_utc) {
        this.historyTime_utc = historyTime_utc;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(int confirmations) {
        this.confirmations = confirmations;
    }

    public int getAmount_btc() {
        return amount_btc;
    }

    public void setAmount_btc(int amount_btc) {
        this.amount_btc = amount_btc;
    }

    public int getAmount_multisig() {
        return amount_multisig;
    }

    public void setAmount_multisig(int amount_multisig) {
        this.amount_multisig = amount_multisig;
    }

    public Boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }
}
