package com.coolbitx.coolwallet.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ShihYi on 2016/1/29.
 */
public class PasingWallet {
    public int n_tx;
    public int n_tx_filtered;
    public long total_received;
    public long total_sent;
    @SerializedName("final_balance")
    public long final_balance;

    public int getN_tx() {
        return n_tx;
    }

    public void setN_tx(int n_tx) {
        this.n_tx = n_tx;
    }

    public int getN_tx_filtered() {
        return n_tx_filtered;
    }

    public void setN_tx_filtered(int n_tx_filtered) {
        this.n_tx_filtered = n_tx_filtered;
    }

    public long getTotal_received() {
        return total_received;
    }

    public void setTotal_received(int total_received) {
        this.total_received = total_received;
    }

    public long getTotal_sent() {
        return total_sent;
    }

    public void setTotal_sent(int total_sent) {
        this.total_sent = total_sent;
    }

    public long getFinal_balance() {
        return final_balance;
    }

    public void setFinal_balance(long final_balance) {
        this.final_balance = final_balance;
    }
}
