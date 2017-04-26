package com.coolbitx.coolwallet.bean;

/**
 * Created by wmgs_01 on 15/10/6.
 */
public class User {

    private String carID;
    private String uuid;
    private String otpCode;
    private byte [] macKey;
    private byte [] encKey;

    public String getMacID() {
        return carID;
    }

    public void settMacID(String carID) {
        this.carID = carID;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public byte[] getMacKey() {
        return macKey;
    }

    public void setMacKey(byte[] macKey) {
        this.macKey = macKey;
    }

    public byte[] getEncKey() {
        return encKey;
    }

    public void setEncKey(byte[] encKey) {
        this.encKey = encKey;
    }
}
