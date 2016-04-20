package com.coolbitx.coolwallet.callback;

/**
 * Created by ShihYi on 2016/3/11.
 */
public interface  RefreshCallback {
    public abstract void success();
    public abstract void fail(String msg);
    public abstract void exception(String msg);
}
