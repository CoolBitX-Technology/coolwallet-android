package com.coolbitx.coolwallet.entity;

import java.io.Serializable;

/**
 * Created by ShihYi on 2016/1/28.
 */
public class CwBtcTxs implements Serializable {
    private String  WID;
    private int  Account_ID;
    private String Address;
    private String Txs_TransationID;
    private String Txs_Address;
    private long Txs_Result;
    private String Txs_Date;
    private int Txs_Confirmation;

    public String getWID() {
        return WID;
    }

    public void setWID(String WID) {
        this.WID = WID;
    }

    public int getAccount_ID() {
        return Account_ID;
    }

    public void setAccount_ID(int account_ID) {
        Account_ID = account_ID;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getTxs_Address() {
        return Txs_Address;
    }

    public void setTxs_Address(String txs_Address) {
        Txs_Address = txs_Address;
    }

    public long getTxs_Result() {
        return Txs_Result;
    }

    public void setTxs_Result(long txs_Result) {
        Txs_Result = txs_Result;
    }

    public String getTxs_TransationID() {
        return Txs_TransationID;
    }

    public void setTxs_TransationID(String txs_TransationID) {
        Txs_TransationID = txs_TransationID;
    }

    public String getTxs_Date() {
        return Txs_Date;
    }

    public void setTxs_Date(String txs_Date) {
        Txs_Date = txs_Date;
    }

    public int getTxs_Confirmation() {
        return Txs_Confirmation;
    }

    public void setTxs_Confirmation(int txs_Confirmation) {
        Txs_Confirmation = txs_Confirmation;
    }
}
