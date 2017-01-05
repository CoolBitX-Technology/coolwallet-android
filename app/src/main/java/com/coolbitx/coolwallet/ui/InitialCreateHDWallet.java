package com.coolbitx.coolwallet.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.coolbitx.coolwallet.R;

/**
 * Created by ShihYi on 2015/10/20.
 */
public class InitialCreateHDWallet extends BaseActivity implements View.OnClickListener {

    private EditText edtCreateHD;
    private Button btnCreateWallet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_create_hd_wallet);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_actionbar);
        ImageView imgView = (ImageView) findViewById(R.id.imageSecurity);
        imgView.setBackground(null);
        TextView textView = (TextView) findViewById(R.id.mytext);
        textView.setText("Create HD Wallet");
        findViews();
        initToolbar();

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

    private void findViews() {
        edtCreateHD = (EditText)findViewById( R.id.edt_createHD );
        btnCreateWallet = (Button)findViewById( R.id.btn_create_wallet );

        btnCreateWallet.setOnClickListener(this);
    }
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Create Wallet");
        setSupportActionBar(toolbar);
        // Navigation Icon設定在 setSupoortActionBar後才有作用,否則會出現 back button_up
        toolbar.setNavigationIcon(R.mipmap.menu_3x);

        ActionBar actionBar = getSupportActionBar();
        // 打開 up button_up
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }
    @Override
    public void onClick(View v) {
        if ( v == btnCreateWallet ) {
            // Handle clicks for btnCreateWallet

        }
    }
}

