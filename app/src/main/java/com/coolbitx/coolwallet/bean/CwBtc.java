package com.coolbitx.coolwallet.bean;

/**
 * Created by ShihYi on 2015/12/30.
 */
public class CwBtc {

    private double currAmount;
    private double currRate;
    private double satoshi;
    private double muBTC;
    private double mBTC;
    private double BTC;
    private String getBTCDisplayFromUnit;

    public double getCurrAmount() {
        return currAmount;
    }

    public void setCurrAmount(double currAmount) {
        this.currAmount = currAmount;
    }

    public double getCurrRate() {
        return currRate;
    }

    public void setCurrRate(double currRate) {
        this.currRate = currRate;
    }

    public double getSatoshi() {
        return satoshi;
    }

    public void setSatoshi(double satoshi) {
        this.satoshi = satoshi;
    }

    public double getMuBTC() {
        return muBTC;
    }

    public void setMuBTC(double muBTC) {
        this.muBTC = muBTC;
    }

    public double getmBTC() {
        return mBTC;
    }

    public void setmBTC(double mBTC) {
        this.mBTC = mBTC;
    }

    public double getBTC() {
        return BTC;
    }

    public void setBTC(double BTC) {
        this.BTC = BTC;
    }

    public String getGetBTCDisplayFromUnit() {
        return getBTCDisplayFromUnit;
    }

    public void setGetBTCDisplayFromUnit(String getBTCDisplayFromUnit) {
        this.getBTCDisplayFromUnit = getBTCDisplayFromUnit;
    }
}
