package com.coolbitx.coolwallet.entity;

/**
 * Created by ShihYi on 2016/2/3.
 */
public class ExchangeRate {
    private double BTC;
    private double USD;

    public double getBTC() {
        return BTC;
    }

    public void setBTC(double BTC) {
        this.BTC = BTC;
    }

    public double getUSD() {
        return USD;
    }

    public void setUSD(double USD) {
        this.USD = USD;
    }
}
