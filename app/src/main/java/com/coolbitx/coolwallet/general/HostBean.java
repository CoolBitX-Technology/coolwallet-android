package com.coolbitx.coolwallet.general;

/**
 * Created by MyPC on 2015/9/17.
 */
public class HostBean {

    private int hostId;//设备id
    private byte bindStatus;//设备绑定状态
    private String hostDesc;//设备描述

    public int getHostId() {
        return hostId;
    }

    public void setHostId(int hostId) {
        this.hostId = hostId;
    }


    public byte getBindStatus() {
        return bindStatus;
    }

    public void setBindStatus(byte bindStatus) {
        this.bindStatus = bindStatus;
    }

    public String getHostDesc() {
        return hostDesc;
    }

    public void setHostDesc(String hostDesc) {
        this.hostDesc = hostDesc;
    }
}
