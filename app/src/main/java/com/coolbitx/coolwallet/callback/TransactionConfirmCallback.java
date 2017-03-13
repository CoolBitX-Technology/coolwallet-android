package com.coolbitx.coolwallet.callback;

/**
 * Created by ShihYi on 2016/12/20.
 */
public interface TransactionConfirmCallback {
    void TransactionConfirm(String outAddr,String changeAddr,long spendAmount);
    void TransactionCancel();
}

