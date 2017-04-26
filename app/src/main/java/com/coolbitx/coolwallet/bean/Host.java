package com.coolbitx.coolwallet.bean;
/**
 * Created by wmgs_01 on 15/10/6.
 */
public class Host{

    private int id;
    private byte bindStatus;
    private String desc;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte getBindStatus() {
        return bindStatus;
    }

    public void setBindStatus(byte bindStatus) {
        this.bindStatus = bindStatus;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}
