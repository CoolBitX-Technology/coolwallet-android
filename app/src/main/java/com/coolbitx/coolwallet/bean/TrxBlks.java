package com.coolbitx.coolwallet.bean;

/**
 * Created by ShihYi on 2017/3/3.
 */

public class TrxBlks {
    byte[] trxHandle;
    int inputId;
    int accid;
    int kcid;
    int kid;
    byte[] out1Addr;
    byte[] out2Addr;
    byte[] sigmtrl;
    byte[] publickey;//公鑰
    int changeKid;

    public int getChangeKid() {
        return changeKid;
    }

    public void setChangeKid(int changeKid) {
        this.changeKid = changeKid;
    }

    public byte[] getPublickey() {
        return publickey;
    }

    public void setPublickey(byte[] publickey) {
        this.publickey = publickey;
    }

    public byte[] getTrxHandle() {
        return trxHandle;
    }

    public void setTrxHandle(byte[] trxHandle) {
        this.trxHandle = trxHandle;
    }

    public byte[] getOut1Addr() {
        return out1Addr;
    }

    public void setOut1Addr(byte[] out1Addr) {
        this.out1Addr = out1Addr;
    }

    public byte[] getOut2Addr() {
        return out2Addr;
    }

    public void setOut2Addr(byte[] out2Addr) {
        this.out2Addr = out2Addr;
    }

    public int getInputId() {
        return inputId;
    }

    public void setInputId(int inputId) {
        this.inputId = inputId;
    }

    public int getKcid() {
        return kcid;
    }

    public void setKcid(int kcid) {
        this.kcid = kcid;
    }

    public int getAccid() {
        return accid;
    }

    public void setAccid(int accid) {
        this.accid = accid;
    }

    public int getKid() {
        return kid;
    }

    public void setKid(int kid) {
        this.kid = kid;
    }

//    public long getBalanc() {
//        return balanc;
//    }
//
//    public void setBalanc(long balanc) {
//        this.balanc = balanc;
//    }

    public byte[] getSigmtrl() {
        return sigmtrl;
    }

    public void setSigmtrl(byte[] sigmtrl) {
        this.sigmtrl = sigmtrl;
    }
}
