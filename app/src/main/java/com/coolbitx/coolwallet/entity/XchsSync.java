package com.coolbitx.coolwallet.entity;

/**
 * Created by ShihYi on 2016/8/16.
 */
public class XchsSync {

    private int accID;
    private int keyPointer;
    private int addNum;
    private String accPub;
    private String accChain;

    public int getAddNum() {
        return addNum;
    }

    public void setAddNum(int addNum) {
        this.addNum = addNum;
    }

    public int getAccID() {
        return accID;
    }

    public void setAccID(int accID) {
        this.accID = accID;
    }

    public int getKeyPointer() {
        return keyPointer;
    }

    public void setKeyPointer(int keyPointer) {
        this.keyPointer = keyPointer;
    }

    public String getAccPub() {
        return accPub;
    }

    public void setAccPub(String accPub) {
        this.accPub = accPub;
    }

    public String getAccChain() {
        return accChain;
    }

    public void setAccChain(String accChain) {
        this.accChain = accChain;
    }
}
