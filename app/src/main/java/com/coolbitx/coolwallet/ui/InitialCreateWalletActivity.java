package com.coolbitx.coolwallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TableRow;

import com.coolbitx.coolwallet.R;

/**
 * Created by ShihYi on 2015/10/20.
 */
public class InitialCreateWalletActivity extends BaseActivity implements View.OnClickListener {

    private TableRow trCreate;
    private TableRow trRecovera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_createwallet);
//        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//        getSupportActionBar().setCustomView(R.layout.custom_actionbar);
//        ImageView imgView = (ImageView) findViewById(R.id.imageSecurity);
//        imgView.setBackground(null);
//        TextView textView = (TextView) findViewById(R.id.mytext);
//        textView.setText("Create Wallet");
        findViews();
        initToolbar();
        trCreate.setOnClickListener(this);
        trRecovera.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        BleActivity.bleManager.disConnectBle();
        finish(); // 離開程式

        Intent intent = new Intent(this, BleActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void findViews() {
        trCreate = (TableRow) findViewById(R.id.tr_create);
        trRecovera = (TableRow) findViewById(R.id.tr_recovera);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Create Wallet");
        setSupportActionBar(toolbar);
        // Navigation Icon設定在 setSupoortActionBar後才有作用,否則會出現 back button_up
        toolbar.setNavigationIcon(R.mipmap.menu_3x);
        toolbar.setNavigationOnClickListener(this);

        ActionBar actionBar = getSupportActionBar();
        // 打開 up button_up
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.tr_create:
//                Intent intentCreate = new Intent(getApplicationContext(), InitialCreateWalletIIActivity.class);
//                startActivity(intentCreate);

                Intent intentCreate = new Intent();
                intentCreate.setClass(getApplicationContext(), InitialCreateWalletIIActivity.class);
                intentCreate.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//关掉所要到的界面中间的activity
                startActivity(intentCreate);
                finish();

                break;

            case R.id.tr_recovera:
//                Intent intentRecover = new Intent(getApplicationContext(), RecovWalletActivity.class);
//                startActivity(intentRecover);

                Intent intentRecover = new Intent();
                intentRecover.setClass(getApplicationContext(), RecovWalletActivity.class);
                intentRecover.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//关掉所要到的界面中间的activity
                startActivity(intentRecover);
                finish();

                break;

            case -1:
                onBackPressed();
                break;
        }

    }
}
