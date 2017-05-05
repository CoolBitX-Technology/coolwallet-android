package com.coolbitx.coolwallet.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Created by wmgs_01 on 15/10/7.
 */
public class UnSpentTxsBean {
    /**
     * API change from BLOCKR to BLOCKCHAIN.INFO,2017/04/16.
     */

//    private String tx_hash_big_endian;
//    private
//    private int tx_output_n;


    /**
     *  for blockr
     */

    private String tx;
    private double amount;//value
    private int n;//outputIndex
    private int confirmations;//confirmations
    private String script;//script
    private String address;
//    private String publickey;

//    public String getPublickey() {
//        return publickey;
//    }
//
//    public void setPublickey(String publickey) {
//        this.publickey = publickey;
//    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getTx() {
        return tx;
    }

    public void setTx(String tx) {
        this.tx = tx;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(int confirmations) {
        this.confirmations = confirmations;
    }

}
