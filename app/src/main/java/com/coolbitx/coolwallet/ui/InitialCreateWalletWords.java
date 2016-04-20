package com.coolbitx.coolwallet.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
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
public class InitialCreateWalletWords extends BaseActivity implements View.OnClickListener {

    private Button btnNext;
    private TextView tvHdWord;
    private Button btnConfirm;
    private Button btnPrevious;
    private Context context;
    private CmdManager cmdManager;
    private ProgressDialog mProgress;
    private String hdwSeed;
    private LinearLayout linConfirm;
    private String[] mBIP39Word;
    private String[] confirmWords;
    private int mWprdsPaging;
    private int mPage = 0;
    public  boolean[] settingOptions = new boolean[4];
    private byte CwSecurityPolicyMaskOtp = 0x01;
    private byte CwSecurityPolicyMaskBtn = 0x02;
    private byte CwSecurityPolicyMaskWatchDog = 0x10;
    private byte CwSecurityPolicyMaskAddress = 0x20;
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
        mBIP39Word = getIntent().getStringArrayExtra("bip39Word");
        hdwSeed = getIntent().getStringExtra("hdwSeed");

        showBIP39();

        tvHdWord.setText(confirmWords[0]);
        btnNext.setVisibility(View.VISIBLE);
        linConfirm.setVisibility(View.GONE);
        GetSecpo();
    }

    private void GetSecpo() {
        cmdManager.getSecpo(new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    if (outputData != null && outputData.length > 0) {
                        if ((outputData[0] & CwSecurityPolicyMaskOtp) >= 1) {
                            settingOptions[0] = true;
                        } else {
                            settingOptions[0] = false;
                        }

                        if ((outputData[0] & CwSecurityPolicyMaskBtn) >= 1) {
                            settingOptions[1] = true;
                        } else {
                            settingOptions[1] = false;
                        }

                        if ((outputData[0] & CwSecurityPolicyMaskAddress) >= 1) {
                            settingOptions[2] = true;
                        } else {
                            settingOptions[2] = false;
                        }

                        if ((outputData[0] & CwSecurityPolicyMaskWatchDog) >= 1) {
                            settingOptions[3] = true;
                        } else {
                            settingOptions[3] = false;
                        }

                        LogUtil.i("get安全設置:otp=" + settingOptions[0] + ";button_up=" + settingOptions[1] +
                                ";address" + settingOptions[2] + ";dog=" + settingOptions[3]);

                    }
                    SetSecpo(true);
                }
            }
        });
    }



    private void showBIP39() {
        int mWordsSize = mBIP39Word.length;
        mWprdsPaging = mWordsSize / 6;
        confirmWords = new String[mWprdsPaging];

        LogUtil.i("mBIP39Word.length=" + String.valueOf(mWordsSize) + ";mWprdsPaging=" + String.valueOf(mWprdsPaging));
        StringBuffer SeedWords = new StringBuffer();

        for (int j = 1; j <= mWprdsPaging; j++) {

            for (int i = (j-1)*6; i < j * 6; i++) {
                SeedWords.append(mBIP39Word[i] + "\n");
            }

            confirmWords[j - 1] = SeedWords.toString();
            SeedWords = new StringBuffer();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.btn_seedWords_next:
                if (mPage < mWprdsPaging - 1) {
                    mPage++;
                }
                LogUtil.i("next page=" + String.valueOf(mPage) + ";mWprdsPaging=" + String.valueOf(mWprdsPaging));
                if (mPage == mWprdsPaging-1) {
                    btnNext.setVisibility(View.GONE);
                    linConfirm.setVisibility(View.VISIBLE);
                } else {
                    btnNext.setVisibility(View.VISIBLE);
                    linConfirm.setVisibility(View.GONE);
                }
                tvHdWord.setText(confirmWords[mPage]);

                break;
            case R.id.seedWords_previous:

                mPage=0;

                LogUtil.i("previous page=" + String.valueOf(mPage) + ";mWprdsPaging=" + String.valueOf(mWprdsPaging));
                btnNext.setVisibility(View.VISIBLE);
                linConfirm.setVisibility(View.GONE);
                tvHdWord.setText(confirmWords[mPage]);

                break;
            case R.id.seedWords_confirm:

                LogUtil.i("by soft create seed:" + hdwSeed + "\n" + "EncKey:" + PublicPun.user.getEncKey() + ",MacKey:" + PublicPun.user.getMacKey());
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                                       /* Show the progress. */
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
//                                mProgress.setMessage("Generating seed...");
//                                mProgress.show();
                                mProgress.setMessage("Creating wallet...");
                                mProgress.show();
                            }
                        });
                        final String name = "";
                        //處理程式寫在此
                        cmdManager.hdwInitWallet(name, hdwSeed, PublicPun.user.getEncKey(), PublicPun.user.getMacKey(), new CmdResultCallback() {
                            @Override
                            public void onSuccess(int status, byte[] outputData) {
                                if ((status + 65536) == 0x9000) {
//                                    PublicPun.toast(InitialCreateWalletWords.this, "HDW Created");
//                                    mProgress.dismiss();
                                    Intent intent = new Intent(InitialCreateWalletWords.this, FragMainActivity.class);
                                    intent.putExtra("Parent", InitialCreateWalletIIActivity.class.getSimpleName());
                                    startActivity(intent);
                                    finish();
                                } else {
                                    mProgress.dismiss();
                                    PublicPun.ClickFunctionToFinish(context, "Erro Message", "Error:" + Integer.toHexString(status));
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
        btnConfirm.setOnClickListener(this);
        btnPrevious.setOnClickListener(this);
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

    //初始化安全设置
    private void SetSecpo(boolean setWatchDog) {
//        settingOptions[0] = switchOtp.isChecked();
//        settingOptions[1] = switchEnablePressButton.isChecked();
//        settingOptions[2] = switchAddress.isChecked();

        settingOptions[3] = setWatchDog;

        LogUtil.i("set安全設置:otp=" + settingOptions[0] + ";button_up=" + settingOptions[1] +
                ";address" + settingOptions[2] + ";dog=" + settingOptions[3]);

        cmdManager.setSecpo(settingOptions, new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
//                    PublicPun.toast(mContext, "CW Security Policy Set");
                }
            }
        });
    }
    @Override
    public Intent getSupportParentActivityIntent() {
        finish();
        return null;
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
        if(settingOptions[3]){
            SetSecpo(false);
        }
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

