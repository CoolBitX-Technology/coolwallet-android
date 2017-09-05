package com.coolbitx.coolwallet.ui;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.Service.BTConfig;
import com.coolbitx.coolwallet.general.NotificationReceiver;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.LogUtil;

import org.bouncycastle.jcajce.provider.symmetric.ARC4;

/**
 * Created by ShihYi on 2015/12/25.
 */
public class LogOutActivity extends BaseActivity implements View.OnClickListener {

    private CmdManager cmdManager;
    private Context mContext;
    private Button btnLogout;
    private ProgressDialog mProgress;
    private int getWallteStatus = 0x00;
    static NotificationReceiver brocastNR;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);
        mContext = this;

        initViews();
        initValues();
        initToolbar();
        cmdManager = new CmdManager();
        mProgress = new ProgressDialog(LogOutActivity.this, ProgressDialog.THEME_HOLO_DARK);
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cmdManager == null) {
            cmdManager = new CmdManager();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStart() {
        super.onStart();
        //註冊監聽
        registerBroadcast(this, cmdManager);
//        LocalBroadcastManager mLocalBroadcastManager;
//        if (brocastNR == null) {
//            brocastNR = new NotificationReceiver(this, cmdManager);
//            //註冊廣播
//            mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
//            mLocalBroadcastManager.registerReceiver(brocastNR, new IntentFilter(BTConfig.SOCKET_ADDRESS_MSG));
//            mLocalBroadcastManager.registerReceiver(brocastNR, new IntentFilter(BTConfig.DISCONN_NOTIFICATION));
//            mLocalBroadcastManager.registerReceiver(brocastNR, new IntentFilter(BTConfig.XCHS_NOTIFICATION));
//        }
    }
    public void unRegisterBroadcast(Context context) {
        try {
            ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
            int ind = cn.getShortClassName().lastIndexOf(".") + 1;//.ui.EraseActivity → EraseActivity
            String act = cn.getShortClassName().substring(ind);

            if (brocastNR != null)
            {
                LogUtil.e("unRegisterBroadcast:"+act);
                LocalBroadcastManager.getInstance(context).unregisterReceiver(brocastNR);
                brocastNR = null;
            }

        } catch (Exception e) {
            brocastNR = null;
            LogUtil.e("error:" + e.getMessage());
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        unRegisterBroadcast(this);
    }

    private void initViews() {
        btnLogout = (Button) findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(this);
    }

    private void initValues() {

    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar.setLogo(getResources().getDrawable(R.mipmap.cw_card));
        toolbar.setTitle(getString(R.string.exit));
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
        if (v == btnLogout) {
            mProgress.setMessage(getString(R.string.logout)+"...");
            mProgress.show();
//            cmdManager.hdwQryWaInfo(getWallteStatus, new CmdResultCallback() {
//                @Override
//                public void onSuccess(int status, byte[] outputData) {
//                    if ((status + 65536) == 0x9000) {
//                        if (outputData != null) {
//                            String statusName = "";
//                            if (outputData[0] == 0x00) {
//                                statusName = "INACTIVE";
//                            } else if (outputData[0] == 0x01) {
//                                statusName = "WAITACTV";
//                            } else if (outputData[0] == 0x02) {
//                                statusName = "ACTIVE";
//                            }
////                            PublicPun.toast(mContext, "HDW Status:" + statusName);
//                            LogUtil.i("HDW Status:" + statusName);
//
//                        }
//                    }
//                }
//            });

//            cmdManager.getModeState(new CmdResultCallback() {
//                @Override
//                public void onSuccess(int status, byte[] outputData) {
//                    if ((status + 65536) == 0x9000) {//-28672//36864
//                        PublicPun.card.setMode(PublicPun.selectMode(PublicPun.byte2HexString(outputData[0])));
//                        PublicPun.card.setState(String.valueOf(outputData[1]));
//                        LogUtil.i("Logout ModeState:" + "\nMode=" + PublicPun.card.getMode() + "\nState=" + outputData[1]);

            cmdManager.bindLogout(new CmdResultCallback() {
                @Override
                public void onSuccess(int status, byte[] outputData) {
                    if ((status + 65536) == 0x9000) {
                        if (outputData != null) {
                            mProgress.dismiss();
                            BleActivity.bleManager.disConnectBle();
                            Intent intent = new Intent();
                            intent.setClass(LogOutActivity.this, BleActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//关掉所要到的界面中间的activity
                            startActivity(intent);
//
                        }
                    }
                }
            });
//                    }
//                }
//            });
        }
    }
}
