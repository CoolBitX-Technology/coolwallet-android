package com.coolbitx.coolwallet.bean;

import java.io.Serializable;

/**
 * Created by ShihYi on 2016/6/29.
 */
public class ExchangeOrder implements Serializable {

    String orderId;
    String cworderId;
    String addr;
    double amount; //BTC
    int account;
    double price;   //美金
    String expiration;
    String type ;
    boolean submitted;

    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCworderId() {
        return cworderId;
    }

    public void setCworderId(String cworderId) {
        this.cworderId = cworderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getAccount() {
        return account;
    }

    public void setAccount(int account) {
        this.account = account;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }
}
