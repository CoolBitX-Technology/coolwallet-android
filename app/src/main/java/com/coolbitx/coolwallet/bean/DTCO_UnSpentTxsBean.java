package com.coolbitx.coolwallet.bean;

/**
 * Created by wmgs_01 on 15/10/7.
 */
public class DTCO_UnSpentTxsBean {

    private String address;
    private String txid;//txHash
    private int vout;//outputIndex
    private String ts;
    private String scriptPubKey;//script
    private int keyId;//地址在卡片里的索引
    private double amount;//value
    private int confirmations;//confirmations
    private boolean confirmationsFromCache;

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public int getVout() {
        return vout;
    }

    public void setVout(int vout) {
        this.vout = vout;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public String getScriptPubKey() {
        return scriptPubKey;
    }

    public void setScriptPubKey(String scriptPubKey) {
        this.scriptPubKey = scriptPubKey;
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

    public boolean isConfirmationsFromCache() {
        return confirmationsFromCache;
    }

    public void setConfirmationsFromCache(boolean confirmationsFromCache) {
        this.confirmationsFromCache = confirmationsFromCache;
    }
}
