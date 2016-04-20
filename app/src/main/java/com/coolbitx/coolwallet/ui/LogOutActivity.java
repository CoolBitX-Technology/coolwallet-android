package com.coolbitx.coolwallet.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.general.PublicPun;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;

/**
 * Created by ShihYi on 2015/12/25.
 */
public class LogOutActivity extends BaseActivity implements View.OnClickListener {

    private CmdManager cmdManager;
    private Context mContext;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);
        mContext = this;

        initViews();
        initValues();
        initToolbar();
        cmdManager = new CmdManager();
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
        if (v == btnLogout) {

            cmdManager.bindLogout(new CmdResultCallback() {
                @Override
                public void onSuccess(int status, byte[] outputData) {
                    if ((status + 65536) == 0x9000) {
                        if (outputData != null) {
                            PublicPun.toast(mContext, "Logout success.");

                            BleActivity.bleManager.disConnectBle();
                            Intent intent = new Intent();
                            intent.setClass(LogOutActivity.this, BleActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//关掉所要到的界面中间的activity
                            startActivity(intent);

                        }
                    }
                }
            });

        }
    }
}
