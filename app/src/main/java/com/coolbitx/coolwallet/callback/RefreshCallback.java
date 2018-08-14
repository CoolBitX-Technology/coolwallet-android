package com.coolbitx.coolwallet.callback;

/**
 * Created by ShihYi on 2016/3/11.
 */
public interface  RefreshCallback {
    public abstract void onSuccess();
    public abstract void onFailure(String msg);
}
