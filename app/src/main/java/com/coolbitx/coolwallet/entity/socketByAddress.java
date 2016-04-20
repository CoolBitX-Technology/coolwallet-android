package com.coolbitx.coolwallet.entity;

import java.io.Serializable;

/**
 * Created by wmgs_01 on 15/10/6.
 */
public class socketByAddress implements Serializable {

    String type;
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    String network;
    String address;
    String balance_change;
    String amount_sent;
    String amount_received;
    String txid;
    int confirmations;
    String tx_type;
    boolean is_green;
    double btc_amount;

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBalance_change() {
        return balance_change;
    }

    public void setBalance_change(String balance_change) {
        this.balance_change = balance_change;
    }

    public String getAmount_sent() {
        return amount_sent;
    }

    public void setAmount_sent(String amount_sent) {
        this.amount_sent = amount_sent;
    }

    public String getAmount_received() {
        return amount_received;
    }

    public void setAmount_received(String amount_received) {
        this.amount_received = amount_received;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(int confirmations) {
        this.confirmations = confirmations;
    }

    public String getTx_type() {
        return tx_type;
    }

    public void setTx_type(String tx_type) {
        this.tx_type = tx_type;
    }

    public boolean is_green() {
        return is_green;
    }

    public void setIs_green(boolean is_green) {
        this.is_green = is_green;
    }

    public double getBtc_amount() {
        return btc_amount;
    }

    public void setBtc_amount(double btc_amount) {
        this.btc_amount = btc_amount;
    }
}
