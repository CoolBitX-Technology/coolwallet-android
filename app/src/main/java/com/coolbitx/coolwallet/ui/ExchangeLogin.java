package com.coolbitx.coolwallet.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.callback.APIResultCallback;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.util.ExchangeAPI;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.utils.LogUtil;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ShihYi on 2017/2/17.
 */

public class ExchangeLogin extends BaseActivity implements View.OnClickListener {

    Button btn_login;
    private ExchangeAPI mExchangeAPI;
    private CmdManager cmdManager;
    private ProgressDialog mProgress;
    Timer mTimer;
    private boolean isLoginSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ex_login);
        mContext = this;

        initViews();
        initValues();
        initToolbar();

        cmdManager = new CmdManager();
        mExchangeAPI = new ExchangeAPI(mContext, cmdManager);

    }

    @Override
    public void onClick(View v) {
        if (v == btn_login) {
            mProgress.show();
            ExchangeLogin();

        }
    }

    private void ExchangeLogin() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!isLoginSuccess){
                            mProgress.dismiss();
                            PublicPun.showNoticeDialog(mContext, "Notification", "Exchange Login failed.");
                            ExchangeLogout();
                            mTimer.cancel();
                        }
                    }
                });
            }
        }, 40000);

        mExchangeAPI.exchangeLogOut(new APIResultCallback() {
            @Override
            public void success(String[] msg) {
                LogUtil.e("Exchange Logout success");
                mExchangeAPI.exchangeLogin(new APIResultCallback() {
                    @Override
                    public void success(String[] msg) {
                        LogUtil.e("Exchange Login success");
                        isLoginSuccess=true;
                        mProgress.dismiss();
                        startActivity(new Intent(getApplicationContext(), ExchangeActivity.class));

                    }

                    @Override
                    public void fail(String msg) {
                        isLoginSuccess=true;
                        PublicPun.showNoticeDialog(mContext, "Notification", "Exchange Login failed.");
                        ExchangeLogout();
                    }
                });
            }

            @Override
            public void fail(String msg) {
                isLoginSuccess=true;
                PublicPun.showNoticeDialog(mContext, "Notification", "Exchange Login failed.");
                ExchangeLogout();
            }
        });
    }

    private void ExchangeLogout() {
        mExchangeAPI.exchangeLogOut(new APIResultCallback() {
            @Override
            public void success(String[] msg) {
                LogUtil.d("Logout success.");
            }

            @Override
            public void fail(String msg) {

            }
        });
    }

    private void initViews() {
        btn_login = (Button) findViewById(R.id.btn_ex_login);
        btn_login.setOnClickListener(this);
    }

    private void initValues() {

        mProgress = new ProgressDialog(this, ProgressDialog.THEME_HOLO_DARK);
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
        mProgress.setMessage("Login...");
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(getResources().getDrawable(R.drawable.exchange));
        toolbar.setTitle("Exchange");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        // 打開 up button_up
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
