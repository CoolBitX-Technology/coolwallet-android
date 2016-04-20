package com.coolbitx.coolwallet.entity;

import java.util.List;

/**
 * Created by wmgs_01 on 15/10/6.
 */
public class Account {

    private int id;
    private String name;
    private double totalBalance;//該帳戶下的總餘額
    private double blockAmount;//被凍結的餘額
    private List<Address> inputAddressList;//input地址列表
    private List<Address> outputAddressList;//output地址列表
    private int inputIndex;
    private int outputIndex;

    public int getInputIndex() {
        return inputIndex;
    }

    public void setInputIndex(int inputIndex) {
        this.inputIndex = inputIndex;
    }

    public int getOutputIndex() {
        return outputIndex;
    }

    public void setOutputIndex(int outputIndex) {
        this.outputIndex = outputIndex;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(double totalBalance) {
        this.totalBalance = totalBalance;
    }

    public double getBlockAmount() {
        return blockAmount;
    }

    public void setBlockAmount(double blockAmount) {
        this.blockAmount = blockAmount;
    }

    public List<Address> getInputAddressList() {
        return inputAddressList;
    }

    public void setInputAddressList(List<Address> inputAddressList) {
        this.inputAddressList = inputAddressList;
    }

    public List<Address> getOutputAddressList() {
        return outputAddressList;
    }

    public void setOutputAddressList(List<Address> outputAddressList) {
        this.outputAddressList = outputAddressList;
    }
}
