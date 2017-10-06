package com.coolbitx.coolwallet.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.callback.APIResultCallback;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.general.ExchangeAPI;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.utils.LogUtil;

/**
 * Created by ShihYi on 2017/2/17.
 */

public class ExchangeLogin extends BaseActivity implements View.OnClickListener {

    Button btn_SignIn, btn_SignUp;
    private ExchangeAPI mExchangeAPI;
    private CmdManager cmdManager;
    private ProgressDialog mProgress;

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
        if (v == btn_SignIn) {
            mProgress.show();
            ExchangeLogin();
        } else if (v == btn_SignUp) {
            String strURL = getString(R.string.url_exchange_site);
            Intent ie = new Intent(Intent.ACTION_VIEW, Uri.parse(strURL));
            startActivity(ie);
        }
    }

    private void ExchangeLogin() {

        mExchangeAPI.exchangeLogin(new APIResultCallback() {
            @Override
            public void success(String[] msg) {
                LogUtil.e("Exchange Login success");

                mProgress.dismiss();

                Intent intent;
                intent = new Intent(ExchangeLogin.this, ExchangeActivity.class);
                startActivityForResult(intent, 0);

            }

            @Override
            public void fail(String msg) {
                mProgress.dismiss();
                PublicPun.showNoticeDialog(mContext,getString(R.string.login_failed), msg + "\nPlease check your Exchange Account Verification and try again later.");
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
        btn_SignIn = (Button) findViewById(R.id.btn_ex_sign_in);
        btn_SignIn.setOnClickListener(this);
        btn_SignUp = (Button) findViewById(R.id.btn_ex_sign_up);
        btn_SignUp.setOnClickListener(this);
    }

    private void initValues() {

        mProgress = new ProgressDialog(this, ProgressDialog.THEME_HOLO_DARK);
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
        mProgress.setMessage(getString(R.string.login));
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(getResources().getDrawable(R.drawable.exchange));
        toolbar.setTitle(getString(R.string.exchange));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        // 打開 up button_up
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        unRegisterBroadcast(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public Intent getSupportParentActivityIntent() {
        finish();
        return null;
    }
}
