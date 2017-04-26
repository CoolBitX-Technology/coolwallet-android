package com.coolbitx.coolwallet.callback;

import com.coolbitx.coolwallet.util.ValidationException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by ShihYi on 2017/4/17.
 */

public interface GetExchangeAddrCallback {
    void onSuccess(String addr) throws NoSuchAlgorithmException, IOException, ValidationException;
    void onFailed(String msg);
}
