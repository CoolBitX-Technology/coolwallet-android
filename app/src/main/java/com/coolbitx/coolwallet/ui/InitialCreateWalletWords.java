package com.coolbitx.coolwallet.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.ui.Fragment.FragMainActivity;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.LogUtil;

/**
 * Created by ShihYi on 2015/10/20.
 */
public class InitialCreateWalletWords extends AppCompatActivity implements View.OnClickListener {

    private Button btnNext;
    private TextView tvHdWord;
    private Button btnConfirm;
    private Button btnPrevious;
    private Context context;
    private CmdManager cmdManager;
    private ProgressDialog mProgress;
    private String strLine1;
    private String strLine2;
    private String hdwSeed;
    private LinearLayout linConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_init_createwallet_words);
        initViews();
        initToolbar();
        context = this;
        cmdManager = new CmdManager();
        mProgress = new ProgressDialog(InitialCreateWalletWords.this);
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);

        strLine1 = getIntent().getStringExtra("line1");
        strLine2 = getIntent().getStringExtra("line2");
        hdwSeed = getIntent().getStringExtra("hdwSeed");
        tvHdWord.setText(strLine1);

        btnNext.setVisibility(View.VISIBLE);
        linConfirm.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.btn_seedWords_next:

                tvHdWord.setText(strLine2);
                btnNext.setVisibility(View.GONE);
                linConfirm.setVisibility(View.VISIBLE);

                break;
            case R.id.seedWords_previous:

                tvHdWord.setText(strLine1);
                btnNext.setVisibility(View.VISIBLE);
                linConfirm.setVisibility(View.GONE);

                break;
            case R.id.seedWords_confirm:

                mProgress.setMessage("Creating wallet...");
                mProgress.show();

                LogUtil.i("by soft create seed:" + hdwSeed + "\n" + "EncKey:" + PublicPun.user.getEncKey() + ",MacKey:" + PublicPun.user.getMacKey());
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                                       /* Show the progress. */
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                mProgress.setMessage("Generating seed...");
                                mProgress.show();
                            }
                        });
                        final String name = "";
                        //處理程式寫在此
                        cmdManager.hdwInitWallet(name, hdwSeed, PublicPun.user.getEncKey(), PublicPun.user.getMacKey(), new CmdResultCallback() {
                            @Override
                            public void onSuccess(int status, byte[] outputData) {
                                if ((status + 65536) == 0x9000) {
                                    PublicPun.toast(InitialCreateWalletWords.this, "HDW Created");
                                    mProgress.dismiss();
                                    Intent intent = new Intent(getApplicationContext(), FragMainActivity.class);
                                    intent.putExtra("Parent", InitialCreateWalletIIActivity.class.getSimpleName());
                                    startActivity(intent);
                                    finish();

                                } else {
                                    mProgress.dismiss();
                                    PublicPun.showNoticeDialogToFinish(context, "Erro Message", "Error:" + Integer.toHexString(status));
                                }
                            }
                        });
                    }
                };
                thread.start();

                break;
        }
    }


    private void initViews() {

        tvHdWord = (TextView) findViewById(R.id.tv_seed_words);
        btnNext = (Button) findViewById(R.id.btn_seedWords_next);
        btnConfirm = (Button) findViewById(R.id.seedWords_confirm);
        btnPrevious = (Button) findViewById(R.id.seedWords_previous);
        linConfirm = (LinearLayout) findViewById(R.id.lin_confirm);

        btnNext.setOnClickListener(this);
        btnPrevious.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Create HD Wallet");
        setSupportActionBar(toolbar);
        // Navigation Icon設定在 setSupoortActionBar後才有作用,否則會出現 back button_up
        toolbar.setNavigationIcon(R.mipmap.menu_3x);

        ActionBar actionBar = getSupportActionBar();
        // 打開 up button_up
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        return  false;
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
}

