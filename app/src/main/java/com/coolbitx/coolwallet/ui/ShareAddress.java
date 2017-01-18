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
import android.widget.Button;
import android.widget.TextView;

import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.callback.APIResultCallback;
import com.coolbitx.coolwallet.entity.Constant;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.httpRequest.AddressSharingAPI;
import com.coolbitx.coolwallet.ui.Fragment.FragMainActivity;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.LogUtil;

/**
 * Created by ShihYi on 2016/12/9.
 */
public class ShareAddress extends BaseActivity implements View.OnClickListener {

    private CmdManager cmdManager;
    private Context mContext;
    private Button btnSharing;
    private AddressSharingAPI mAddressSharingAPI;
    private byte[] loginChallenge;//登陸的特徵值
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_sharing);
        mContext = this;

        initViews();
        initValues();
        initToolbar();
        cmdManager = new CmdManager();
        mAddressSharingAPI = new AddressSharingAPI(this, cmdManager);
                   /* Initialize the progress dialog */
        mProgress = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_DARK);
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
        mProgress.setMessage("Start to Share address...");
    }

    private void initViews() {
        btnSharing = (Button) findViewById(R.id.btn_sharing);
        btnSharing.setOnClickListener(this);
    }

    private void initValues() {

    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar.setLogo(getResources().getDrawable(R.mipmap.cw_card));
        toolbar.setTitle("Exit");
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
        if (v == btnSharing) {

            mProgress.show();
            final String cwid = new String(PublicPun.hexStringToByteArray(PublicPun.card.getCardId()));
            mAddressSharingAPI.StartSharing(cwid, new APIResultCallback() {
                @Override
                public void success(String[] msg) {
                    LogUtil.d("API success:" + msg[0]);

                    int hostId = Integer.valueOf(msg[0]);
                    BindLogout();
                    BindLoginChllenge(cwid, hostId);
                }

                @Override
                public void fail(String msg) {
                    failedAlert("CWS access failed", msg);
                }
            });

        }
    }

    private void failedAlert(String title,String msg){
        if(mProgress.isShowing()){
            mProgress.dismiss();
        }
        PublicPun.showNoticeDialog(ShareAddress.this,title, msg);
    }

    private void BindLoginChllenge(final String cwid, final int hostId) {
        cmdManager.bindLoginChlng(hostId, new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    if (outputData != null) {
                        loginChallenge = outputData;//16LEN,用來當api Response的subUrl
                        String subAddr = "/" + cwid + "/" + LogUtil.byte2HexStringNoBlank(loginChallenge);
                        LogUtil.i("subAddr=" + subAddr);

                        mAddressSharingAPI.GenRspForChallenge(subAddr, new APIResultCallback() {
                            @Override
                            public void success(String[] msg) {
                                LogUtil.d("API success:" + msg[0]);
                                byte[] resp = PublicPun.hexStringToByteArray(msg[0]);
                                BindLogin(cwid, resp, hostId);
                            }

                            @Override
                            public void fail(String msg) {
                                failedAlert( "CWS access failed", msg);
                            }
                        });
                    }
                }else{
                    failedAlert( "CoolWallet exec failed", LogUtil.byte2HexString(outputData));
                }
            }
        });
    }


    private void BindLogin(final String cwid, byte[] Resp, final int hostId) {

        cmdManager.shareBindLogin(Resp, hostId, new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    LogUtil.d("shareBindLogin success.");
                    getAccountKeyInfo(cwid, Constant.CwHdwAccountKeyInfoAddress, Constant.CwAddressKeyChainExternal, 0, 0);
                }else{
                    failedAlert( "CoolWallet exec failed", LogUtil.byte2HexString(outputData));
                }
            }
        });
    }

    private void getAccountKeyInfo(final String cwid, final byte kInfoId, final byte kcId, final int accountId, final int kid) {
        FragMainActivity.cmdManager.hdwQueryAccountKeyInfo(kInfoId, kcId, accountId, kid,
                new CmdResultCallback() {
                    @Override
                    public void onSuccess(int status, byte[] outputData) {
                        if ((status + 65536) == 0x9000) {
                            if (outputData != null) {

                                String KeyInfo = LogUtil.byte2HexStringNoBlank(outputData);
                                LogUtil.d("getAccountKeyInfo success=" + KeyInfo);
                                mAddressSharingAPI.ShareAddress(cwid, PublicPun.card.getCardId(), KeyInfo, new APIResultCallback() {
                                    @Override
                                    public void success(String[] msg) {
                                        LogUtil.d("API  ShareAddress=" + msg[0]);
                                        mProgress.dismiss();
                                        LayoutInflater inflater = LayoutInflater.from(mContext);
                                        View alert_view = inflater.inflate(R.layout.share_edit_dialog, null);//alert為另外做給alert用的layout
                                        final TextView mDialogTitle = (TextView) alert_view.findViewById(R.id.dialog_title);
                                        final TextView mDialogMessageid=(TextView) alert_view.findViewById(R.id.dialog_msg_id);
                                        final TextView mDialogMessage = (TextView) alert_view.findViewById(R.id.dialog_message);
                                        final TextView mDialogMessageref = (TextView) alert_view.findViewById(R.id.dialog_msg_ref);

                                        mDialogMessageid.setText(cwid);
                                        mDialogMessage.setText("\nYour Reference Number is\n");
                                        mDialogMessageref.setText(msg[0]);
                                        //-----------產生輸入視窗--------
                                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                                        mDialogTitle.setText("Address sharing success");
                                        builder.setView(alert_view);
                                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {

                                            }
                                        });
                                        builder.show();
                                    }

                                    @Override
                                    public void fail(String msg) {
                                       failedAlert( "CWS access failed", msg);
                                    }
                                });

                            }
                        }else{
                            failedAlert( "CoolWallet exec failed", LogUtil.byte2HexString(outputData));
                        }
                    }
                });
    }

    private void BindLogout() {
        cmdManager.bindLogout(new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    if (outputData != null) {
                        LogUtil.d("Logout success.");
                    }
                }
            }
        });
    }
}
