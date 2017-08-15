package com.coolbitx.coolwallet.ui;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.general.AppPrefrence;
import com.coolbitx.coolwallet.ui.BaseActivity;
import com.snscity.egdwlib.utils.LogUtil;

public class ServiceProviderActivity extends BaseActivity implements  CompoundButton.OnCheckedChangeListener {

    Switch apiSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_provider);


        initView();
        initToolbar();

        apiSwitch.setChecked(AppPrefrence.getIsBlockrApi(this));

    }

    private void initView() {
        apiSwitch = (Switch) findViewById(R.id.switch_api);
        apiSwitch.setOnCheckedChangeListener(this);

    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Payment Service Provider");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.menu_3x);
        ActionBar actionBar = getSupportActionBar();
        // 打開 up button_up
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        AppPrefrence.saveIsBlockrApi(this,b);
        LogUtil.d(String.valueOf(AppPrefrence.getIsBlockrApi(this)));
    }
}
