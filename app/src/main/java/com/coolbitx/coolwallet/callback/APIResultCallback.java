package com.coolbitx.coolwallet.callback;

/**
 * Created by ShihYi on 2016/6/7.
 */
public interface APIResultCallback {

    public abstract void success(String[] msg);
    public abstract void fail(String msg);

}
