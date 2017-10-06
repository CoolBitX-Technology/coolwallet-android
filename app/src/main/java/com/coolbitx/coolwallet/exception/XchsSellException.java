package com.coolbitx.coolwallet.exception;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;

import com.coolbitx.coolwallet.DataBase.DatabaseHelper;
import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.callback.SellExceptionCallback;
import com.coolbitx.coolwallet.general.BtcUrl;
import com.coolbitx.coolwallet.general.PublicPun;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.LogUtil;

import java.util.Random;

/**
 * Created by Dorac on 2017/9/20.
 */

public class XchsSellException {


    CmdManager cmdManager;
    Context mContext;
    SellExceptionCallback sellExceptionCallback;

    public XchsSellException(CmdManager cmdManager, Context context, SellExceptionCallback sellExceptionCallback) {

        this.cmdManager = cmdManager;
        this.mContext = context;
        this.sellExceptionCallback = sellExceptionCallback;

    }

    public void CancelBlock(byte[] cancelBlkInfo, final ProgressDialog mProgress) {

        cmdManager.XchsCancelBlock(cancelBlkInfo, new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {

                if ((status + 65536) == 0x9000) {//-28672//36864
                    LogUtil.d("SE CancelBlock success = " + PublicPun.byte2HexString(outputData));

                    sellExceptionCallback.onSuccess();
                    mProgress.dismiss();
                    ((Activity) mContext).finish();

                } else {
                    sellExceptionCallback.onException();
                    mProgress.dismiss();
                    LogUtil.d("SE CancelBlock fail");
                    PublicPun.showNoticeDialog(mContext, mContext.getString(R.string.unable_to_cancel_block), mContext.getString(R.string.error) + ":" + Integer.toHexString(status)
                            + "-" + PublicPun.byte2HexString(outputData));

                }
            }
        });
    }

    public void SignLogout(final byte[] trxHandle, final ProgressDialog mProgress) {

        byte[] nonce = new byte[16];
        new Random().nextBytes(nonce);
        mProgress.dismiss();

        cmdManager.XchsTrxsignLogout(trxHandle, nonce, new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    LogUtil.d("XchsTrxsignLogout 成功");
                }else{

                    PublicPun.showNoticeDialog(mContext, mContext.getString(R.string.error_msg),
                            mContext.getString(R.string.error) + ":" + Integer.toHexString(status)
                            + "-" + PublicPun.byte2HexString(outputData));

                }
            }
        });

    }

    public void RetrySignLogout(byte[] trxHandle, final ProgressDialog mProgress) {

//        SignLogout(trxHandle, mProgress);
    }
}
