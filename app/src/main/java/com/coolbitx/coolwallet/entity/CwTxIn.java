package com.coolbitx.coolwallet.entity;

import java.sql.Date;

/**
 * Created by ShihYi on 2015/12/30.
 */
public class CwTxIn {

    private Date tid;
    private int n;
    private int accId;
    private int kcId;
    private int kId;
    private String addr;
    private Date pubKey;
    private Date hashForSign;
    private Date signature;
    private Date scriptPub;
    private Boolean sendToCard;

    public Date getTid() {
        return tid;
    }

    public void setTid(Date tid) {
        this.tid = tid;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public int getAccId() {
        return accId;
    }

    public void setAccId(int accId) {
        this.accId = accId;
    }

    public int getKcId() {
        return kcId;
    }

    public void setKcId(int kcId) {
        this.kcId = kcId;
    }

    public int getkId() {
        return kId;
    }

    public void setkId(int kId) {
        this.kId = kId;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public Date getPubKey() {
        return pubKey;
    }

    public void setPubKey(Date pubKey) {
        this.pubKey = pubKey;
    }

    public Date getHashForSign() {
        return hashForSign;
    }

    public void setHashForSign(Date hashForSign) {
        this.hashForSign = hashForSign;
    }

    public Date getSignature() {
        return signature;
    }

    public void setSignature(Date signature) {
        this.signature = signature;
    }

    public Date getScriptPub() {
        return scriptPub;
    }

    public void setScriptPub(Date scriptPub) {
        this.scriptPub = scriptPub;
    }

    public Boolean getSendToCard() {
        return sendToCard;
    }

    public void setSendToCard(Boolean sendToCard) {
        this.sendToCard = sendToCard;
    }
}
