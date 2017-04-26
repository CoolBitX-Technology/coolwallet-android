package com.coolbitx.coolwallet.bean;

/**
 * Created by ShihYi on 2015/12/30.
 */
public class CWAccountKeyInfo {


    private int accId;
    private int kcid;
    private String publicKey;
    private String chainCode;
    private int extKeyPointer;
    private int intKeyPointer;

    public int getAccId() {
        return accId;
    }

    public void setAccId(int accId) {
        this.accId = accId;
    }

    public int getKcid() {
        return kcid;
    }

    public void setKcid(int kcid) {
        this.kcid = kcid;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getChainCode() {
        return chainCode;
    }

    public void setChainCode(String chainCode) {
        this.chainCode = chainCode;
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
}
