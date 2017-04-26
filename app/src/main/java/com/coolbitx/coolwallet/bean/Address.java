package com.coolbitx.coolwallet.bean;

/**
 * Created by wmgs_01 on 15/10/6.
 */
public class Address {

    private int accountId;
    private int keyChainId;//address的类别,属于input,output
    private int keyId;//该地址在不同类别里的索引值 input index,output index
    private String address;//具体地址内容
    private byte[] publickey;//地址对应的公钥
    private double balance;//该地址的余额
    private String BCAdress; //地址+餘額
    private String addLabel;

    public void setBCAdress(String BCAdress) {
        this.BCAdress = BCAdress;
    }

    public String getAddLabel() {
        return addLabel;
    }

    public void setAddLabel(String addLabel) {
        this.addLabel = addLabel;
    }

    public String getBCAdress() {
        return BCAdress;
    }

    public void setBCAddress(String BCAdress) {
        this.BCAdress = BCAdress;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getKeyChainId() {
        return keyChainId;
    }

    public void setKeyChainId(int keyChainId) {
        this.keyChainId = keyChainId;
    }

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

    public byte[] getPublickey() {
        return publickey;
    }

    public void setPublickey(byte[] publickey) {
        this.publickey = publickey;
    }
}
