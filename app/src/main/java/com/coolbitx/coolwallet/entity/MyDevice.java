package com.coolbitx.coolwallet.entity;

/**
 * Created by Dora chuang on 2015/9/30.
 */
public class MyDevice {

    private String name;
    private String address;
    private String rssi;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }
}
