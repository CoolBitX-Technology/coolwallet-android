package com.coolbitx.coolwallet.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.adapter.UnclarifyOrderAdapter;
import com.coolbitx.coolwallet.callback.APIResultCallback;
import com.coolbitx.coolwallet.bean.ExchangeOrder;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.util.ExchangeAPI;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.LogUtil;

import java.util.ArrayList;

/**
 * Created by ShihYi on 2015/12/25.
 */
public class ExchangeVerificationActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private CmdManager cmdManager;
    private Context mContext;
    private ExchangeAPI mExchangeAPI;
    private ListView listViewVerification;
    ArrayList<ExchangeOrder> listExchangeOrder;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_verification);
        mContext = this;

        initViews();
        initValues();
        initToolbar();
        cmdManager = new CmdManager();
        mExchangeAPI = new ExchangeAPI(mContext,
                cmdManager);
        GetUnclarifyOrder();
    }

    private void GetUnclarifyOrder() {
        mProgress.show();
        mExchangeAPI.getUnclarifyOrder(new APIResultCallback() {
            @Override
            public void success(String[] msg) {
                mProgress.dismiss();
                LogUtil.d("getUnclarifyOrder ok " + msg[0]);

                listExchangeOrder = new ArrayList<>();
                listExchangeOrder = PublicPun.jsonParserExchange(msg[0], "response", false);
                if (listExchangeOrder.size() != 0) {
                    listViewVerification.setAdapter(new UnclarifyOrderAdapter(mContext, listExchangeOrder));
                } else {
                    clickToFinish("Sell order verification", "No unverified orders found.");
                }
            }

            @Override
            public void fail(String msg) {
                mProgress.dismiss();
                LogUtil.d("getUnclarifyOrder failed:" + msg);
                clickToFinish("Unable to get Unverified Orders", "Error:" + msg);
            }
        });
    }


    private void clickToFinish(String title, String msg) {
        AlertDialog.Builder mBuilder =
                PublicPun.CustomNoticeDialog(mContext, title, msg);
        mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
            }
        }).show();
    }

    private void initViews() {
        listViewVerification = (ListView) findViewById(R.id.grid_verification);
        listViewVerification.setOnItemClickListener(this);
    }

    private void initValues() {
        mProgress = new ProgressDialog(this, ProgressDialog.THEME_HOLO_DARK);
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
        mProgress.setMessage("synchronizing...");
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Verification");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        // 打開 up button_up
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        finish();
        return null;
    }

    @Override
    public void onClick(View v) {
//        if (v == sellVerification) {
//            PublicPun.toast(mContext,"press sell verification");
//        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int infoid = 1;
        final int itemPos = position;
        cmdManager.XchsGetOtp(infoid, new CmdResultCallback() {
            byte[] handle;

            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    XchsVerifyOtp(itemPos);
                    //16進制的9000在10進制是36864;6645是26181;status=91717
                } else if ((status + 65536) == 0x16645) {
                    PublicPun.showNoticeDialog(mContext, "Unable to generate OTP", "Please try again.");
                } else {
//                    LogUtil.i("status="+String.valueOf(status));
                    PublicPun.showNoticeDialog(mContext, "Unable to generate OTP", "Error:" + Integer.toHexString(status));
                }
            }
        });
    }

    EditText editText;
    byte[] cancelBlkInfo = new byte[72];
    byte[] okTkn = new byte[4];
    byte[] unblockTkn = new byte[16];
    byte[] OrderBlock;


    private void XchsVerifyOtp(final int item) {
        final String orderID = listExchangeOrder.get(item).getOrderId();
        final View view = LayoutInflater.from(mContext).inflate(R.layout.alert_dialog_otp_input, null);
        AlertDialog.Builder otp_dialog = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT);
        editText = (EditText) view.findViewById(R.id.alert_editotp);
        otp_dialog.setView(view);
        otp_dialog.setCancelable(false);
//        verify OTP
        otp_dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mProgress.setMessage("Begin to block order...");
                mProgress.show();
                //1.transit to server

                String blockOtp = editText.getText().toString();
                LogUtil.d("orderID=" + listExchangeOrder.get(item).getOrderId() + ";blockOtp=" + blockOtp);


                mExchangeAPI.getExRequestOrderBlock(orderID, blockOtp, new APIResultCallback() {
                    @Override
                    public void success(String[] msg) {
                        LogUtil.d("getExRequestOrderBlock ok " + msg[0]);//orderId(4B)/accId(4B)/amount(8B)/mac1(32B)/nonce(16B)

                        //orderId/okTkn/encUblkTkn/mac1/nonce
                        OrderBlock = PublicPun.hexStringToByteArray(msg[0]);

                        // 2.SE cmd (F9)
                        byte[] svrResp = PublicPun.hexStringToByteArray(msg[0]);

                        cmdManager.XchsBlockBtc(svrResp, new CmdResultCallback() {
                            @Override
                            public void onSuccess(int status, byte[] outputData) {

                                if ((status + 65536) == 0x9000) {//-28672//36864
                                    LogUtil.d("XchsBlockBtc  success = " + PublicPun.byte2HexStringNoBlank(outputData));

                                    System.arraycopy(outputData, 32, okTkn, 0, 4);
                                    System.arraycopy(outputData, 36, unblockTkn, 0, 16);

                                    mExchangeAPI.doExWriteOKToken(orderID, PublicPun.byte2HexStringNoBlank(okTkn), PublicPun.byte2HexStringNoBlank(unblockTkn),
                                            new APIResultCallback() {
                                                @Override
                                                public void success(String[] msg) {
                                                    LogUtil.d("ExWriteOKToken " + msg[0]);//orderId(4B)/accId(4B)/amount(8B)/mac1(32B)/nonce(16B)
//                                                    finish();
                                                    GetUnclarifyOrder();
                                                    mProgress.dismiss();
                                                }

                                                @Override
                                                public void fail(String msg) {
                                                    LogUtil.d("ExWriteOKToken failed:" + msg);
                                                    mProgress.dismiss();
                                                    PublicPun.showNoticeDialog(mContext, "Unable to Block", "WriteOKToken failed:" + msg);
                                                }
                                            });

                                } else {
                                    LogUtil.d("XchsBlockBtc fail");
                                    mProgress.dismiss();
                                    PublicPun.showNoticeDialog(mContext, "Unable to Block", "WriteOKToken failed:" + Integer.toHexString(status));
                                }
                            }
                        });
                    }

                    @Override
                    public void fail(String msg) {
                        LogUtil.d("getExRequestOrderBlock failed:" + msg);
                        mProgress.dismiss();
                        PublicPun.showNoticeDialog(mContext, "Unable to Block", msg);

                    }
                });
            }
        }).setNegativeButton(R.string.strCancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();


            }
        });
        otp_dialog.show();
    }
}
