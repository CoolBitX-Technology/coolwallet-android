package com.coolbitx.coolwallet.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.general.AppPrefrence;

import java.text.DecimalFormat;

/**
 * Created by ShihYi on 2016/2/3.
 */

public class TxsActivity extends BaseActivity {
    private TextView tvTitle;
    private TextView tvAddress;
    private TextView tvBtc;
    private TextView tvUsd;
    private TextView tvDate;
    private TextView tvConfirmation;
    private TextView tvTransantionID;
    private Button btnViewBlockchain;
    String mTid = null;
    private DecimalFormat mFormatter = new DecimalFormat("#.##");
    private DecimalFormat mFormatterBTC = new DecimalFormat("#.########");
    double currRate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_txs);

        findViews();
        initToolbar();


        String mAddr = getIntent().getStringExtra("addr");
        double mBtc = getIntent().getDoubleExtra("btc", 0.0);
        currRate = mBtc * AppPrefrence.getCurrentRate(TxsActivity.this);
//        double mUsd = getIntent().getDoubleExtra("usd", 0.00);
        String mDate = getIntent().getStringExtra("date");
        String mCfm = getIntent().getStringExtra("confirmation");
        mTid = getIntent().getStringExtra("tid");

        if (mBtc > 0) {
            tvTitle.setText("Received from");
        } else {
            tvTitle.setText("Send to");
        }

        tvAddress.setText(mAddr);
        tvBtc.setText(mFormatterBTC.format(mBtc));
        tvUsd.setText(mFormatter.format(currRate)+" "+AppPrefrence.getCurrentCountry(TxsActivity.this));
        tvDate.setText(mDate);
        tvConfirmation.setText(mCfm);
        tvTransantionID.setText(mTid);

        btnViewBlockchain.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String strURL = "https://blockchain.info/tx/";
                Intent ie = new Intent(Intent.ACTION_VIEW, Uri.parse(strURL + mTid));
                startActivity(ie);
            }
        });
    }

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2016-02-03 17:25:42 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvAddress = (TextView) findViewById(R.id.tvAddress);
        tvBtc = (TextView) findViewById(R.id.tvBtc);
        tvUsd = (TextView) findViewById(R.id.tvUsd);
        tvDate = (TextView) findViewById(R.id.tvDate);
        tvConfirmation = (TextView) findViewById(R.id.tvConfirmation);
        tvTransantionID = (TextView) findViewById(R.id.tvTransantionID);
        btnViewBlockchain = (Button) findViewById(R.id.btn_view_blockchain);

    }

    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        toolbar.setLogo(getResources().getDrawable(R.mipmap.logo2));
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.menu_3x);
        // Navigation Icon設定在 setSupoortActionBar後才有作用,否則會出現 back button_up
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
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onPause() {
        super.onPause();

    }

}
