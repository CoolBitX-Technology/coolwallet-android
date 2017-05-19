package com.coolbitx.coolwallet.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.coolbitx.coolwallet.DataBase.DatabaseHelper;
import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.adapter.RateAdapter;
import com.coolbitx.coolwallet.general.AppPrefrence;
import com.coolbitx.coolwallet.ui.Fragment.TabFragment;
import com.snscity.egdwlib.CmdManager;

import java.util.ArrayList;

/**
 * Created by ShihYi on 2015/12/25.
 */
public class ExchangeRateActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private Context context;
    private LayoutInflater layoutInflater;
    private ListView rateList;
    private ArrayList<String> rateData;
    private RateAdapter adapter;
    private CmdManager cmdManager;
    private ProgressDialog mProgress;

    //    @Nullable
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_rate);

        context = getApplicationContext();
        layoutInflater = getLayoutInflater();
        initToolbar();

        mProgress = new ProgressDialog(ExchangeRateActivity.this, ProgressDialog.THEME_HOLO_DARK);
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);

        rateList = (ListView) findViewById(R.id.lv_rate);
        rateList.setOnItemClickListener(this);
        rateData = DatabaseHelper.queryExchangeRate(context);
        updataViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cmdManager == null) {
            cmdManager = new CmdManager();
        }
        //註冊監聽
        registerBroadcast(this, cmdManager);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unRegisterBroadcast(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (rateData != null && !rateData.isEmpty()) {
            AppPrefrence.saveCurrentCountry(context, rateData.get(position));
            TabFragment.ExchangeRate = DatabaseHelper.queryCurrent(context, AppPrefrence.getCurrentCountry(context));
            AppPrefrence.saveCurrentRate(context,  (float)TabFragment.ExchangeRate);
            SetCurrencyRate(mContext);
            finish();
        }
    }

    private void updataViews() {

        if (adapter == null) {
            adapter = new RateAdapter(rateData, context);
        } else {
            adapter.notifyDataSetChanged();
        }
        rateList.setAdapter(adapter);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Exchange Rate");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.menu_3x);
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

}
