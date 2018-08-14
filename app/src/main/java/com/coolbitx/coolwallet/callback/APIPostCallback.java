package com.coolbitx.coolwallet.callback;

/**
 * Created by Dorac on 2017/10/26.
 */

public interface APIPostCallback {

    void onSuccess();
    void onFailure(int errorCode);

}
