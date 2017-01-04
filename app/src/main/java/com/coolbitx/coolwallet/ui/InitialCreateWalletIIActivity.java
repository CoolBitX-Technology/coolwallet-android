package com.coolbitx.coolwallet.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.ui.Fragment.FragMainActivity;
import com.coolbitx.coolwallet.util.BIP39;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.LogUtil;
import com.coolbitx.coolwallet.util.ValidationException;

import java.util.Random;

/**
 * Created by ShihYi on 2015/10/20.
 */
public class InitialCreateWalletIIActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener,
        View.OnClickListener,
        AdapterView.OnItemSelectedListener {

    private SeekBar hdwSeedLength;
    private Button btnNext;
    private Spinner seedSpinner;
    private TextView edtHdWord;
    private TextView tvHdwSeedLength;
    private String[] strSeed = {"Card (numbers)", "App (words)"};
    private Button hdwConfirm;
    private LinearLayout hdwSumLin;
    private TableLayout tlCcreate;
    private EditText hdwSumEt;
    private boolean isSeedOn = true;
    private CmdManager cmdManager;
    private ArrayAdapter<String> listSeed;
    private byte[] activeCode;
    private int lastProgress = 0;
    private int newProgress = 0;
    private TextView tvSeedType;
    private ProgressDialog mProgress;
    Context context;
    String hdwSeed = "";

    private LinearLayout layoutWords;
    private TextView edtHdWord2;
    private TextView edtHdWord3;

    public boolean[] settingOptions = new boolean[4];
    private byte CwSecurityPolicyMaskOtp = 0x01;
    private byte CwSecurityPolicyMaskBtn = 0x02;
    private byte CwSecurityPolicyMaskWatchDog = 0x10;
    private byte CwSecurityPolicyMaskAddress = 0x20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_init_createwallet_ii);
        initViews();
        initToolbar();
        context = this;
        cmdManager = new CmdManager();
        mProgress = new ProgressDialog(InitialCreateWalletIIActivity.this);
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
        hdwSeedLength.setProgress(0);

        PublicPun.showNoticeDialog(this, "Reminder", "Please put the CoolWallet on CoolLink while setting up.");
        GetSecpo();
    }

    @Override
    public void onClick(View view) {

        final int length = Integer.valueOf(tvHdwSeedLength.getText().toString().trim()) * 6;
        final String name = "";

        if (view == btnNext) {
            // Handle clicks for btnCreateHDW
            if (isSeedOn) {//by card create seed (Numbers)
                hdwSumLin.setVisibility(View.VISIBLE);
                tlCcreate.setVisibility(View.GONE);

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
                        //處理程式寫在此

                        LogUtil.i("create wallet length:" + length);
                        cmdManager.hdwInitWalletGen(name, length, new CmdResultCallback() {
                            @Override
                            public void onSuccess(int status, byte[] outputData) {
                                if ((status + 65536) == 0x9000) {
                                    if (outputData != null) {
                                        LogUtil.e("outputData=" + PublicPun.byte2HexString(outputData));
                                        activeCode = new byte[4];
                                        int len = outputData.length;
                                        int sLen = length / 2;
                                        if (len >= sLen + 4) {
                                            activeCode[0] = outputData[sLen];
                                            activeCode[1] = outputData[sLen + 1];
                                            activeCode[2] = outputData[sLen + 2];
                                            activeCode[3] = outputData[sLen + 3];
                                        }
                                    }
                                    mProgress.dismiss();
                                } else {
                                    mProgress.dismiss();
                                    PublicPun.showNoticeDialogToFinish(context, "Error Message", "Error:" + Integer.toHexString(status));
                                }
                            }
                        });
                    }
                };
                thread.start();
            } else {//by soft create seed (Words)
                hdwSumLin.setVisibility(View.VISIBLE);
                tlCcreate.setVisibility(View.GONE);
                LogUtil.i("words hdwSeed=" + hdwSeed);

                Intent intent = new Intent();
                intent.setClass(context, InitialCreateWalletWords.class);
                Bundle bundle = new Bundle();
                bundle.putString("line1", BIP39.seedWprdsLine1);
                bundle.putString("line2", BIP39.seedWprdsLine2);
                bundle.putString("hdwSeed", hdwSeed);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }

        } else if (view == hdwConfirm) {
            //種子
            mProgress.setMessage("Creating wallet...");
            mProgress.show();
            String hdwSumStr = hdwSumEt.getText().toString().trim();
            LogUtil.d("hdwSumStr="+hdwSumStr);
            cmdManager.hdwInitWalletGenConfirm(activeCode, hdwSumStr, new CmdResultCallback() {
                @Override
                public void onSuccess(int status, byte[] outputData) {
                    if ((status + 65536) == 0x9000) {
                        PublicPun.toast(InitialCreateWalletIIActivity.this, "HDW Created");

                        Intent intent = new Intent(getApplicationContext(), FragMainActivity.class);
                        intent.putExtra("Parent", InitialCreateWalletIIActivity.class.getSimpleName());
                        startActivity(intent);

                    } else if ((status + 65536) == 0x16645) {
                        PublicPun.showNoticeDialogToFinish(context, "Checksum incorrect", "Please try again or generate seed again");
                    } else {
                        PublicPun.showNoticeDialogToFinish(context, "Error Message", "Error:" + Integer.toHexString(status));
                    }
                }
            });
        }
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        if (progress > newProgress + 10 || progress < newProgress - 10) {
            newProgress = lastProgress;
            hdwSeedLength.setProgress(lastProgress);
            return;
        }
        newProgress = progress;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        LogUtil.i("seekBar 移動");
        if (newProgress < 30) {
            lastProgress = 0;
            newProgress = 0;
            hdwSeedLength.setProgress(0);
            tvHdwSeedLength.setText(isSeedOn ? "8" : "12");

            changeLayout(128);

        } else if (newProgress > 70) {
            //设置lastProgress 要放在setProgress之前，否则可能导致执行多次onProgressChanged 改变了原值
            lastProgress = 100;
            newProgress = 100;
            hdwSeedLength.setProgress(100);
            tvHdwSeedLength.setText(isSeedOn ? "16" : "24");

            changeLayout(256);

        } else {
            lastProgress = 50;
            newProgress = 50;
            hdwSeedLength.setProgress(50);
            tvHdwSeedLength.setText(isSeedOn ? "12" : "18");

            changeLayout(192);

        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if (parent == seedSpinner) {
            if (position == 0) {
                //by card create seed
                isSeedOn = true;
                tvSeedType.setText("Card");
            } else {
                //by soft create seed
                isSeedOn = false;
                tvSeedType.setText("App");
            }

            if (newProgress < 30) {
                lastProgress = 0;
                newProgress = 0;
                hdwSeedLength.setProgress(0);
                tvHdwSeedLength.setText(isSeedOn ? "8" : "12");
                changeLayout(128);

            } else if (newProgress > 70) {
                //设置lastProgress 要放在setProgress之前，否则可能导致执行多次onProgressChanged 改变了原值
                lastProgress = 100;
                newProgress = 100;
                hdwSeedLength.setProgress(100);
                tvHdwSeedLength.setText(isSeedOn ? "16" : "24");
                changeLayout(256);

            } else {
                lastProgress = 50;
                newProgress = 50;
                hdwSeedLength.setProgress(50);
                tvHdwSeedLength.setText(isSeedOn ? "12" : "18");
                changeLayout(192);

            }
        }
    }

    private void changeLayout(int randomInt) {

        if (!isSeedOn) {
//            hdwSeed = BIP39.getMnemonic(BIP39.getRandomString(randomInt / 8).getBytes()).replace(" ", "");
//            a-z,0-9=36變化;改00~255=256種變化
            byte[] entropy = new byte[randomInt / 8];
            new Random().nextBytes(entropy);
            try {
                hdwSeed = BIP39.getMnemonic(entropy);
            } catch (ValidationException ve) {
                PublicPun.showNoticeDialog(this, "Error", ve.getMessage());
            }
            edtHdWord.setVisibility(View.GONE);
            layoutWords.setVisibility(View.VISIBLE);
            layoutWords.setBackgroundColor(this.getResources().getColor(R.color.md_white_1000));

            edtHdWord2.setText(BIP39.getSeedWords(1));
            edtHdWord2.setTextColor(Color.BLACK);
            edtHdWord2.setTextSize(16.0f);

            edtHdWord3.setText(BIP39.getSeedWords(2));
            edtHdWord3.setTextColor(Color.BLACK);
            edtHdWord3.setTextSize(16.0f);

        } else {
            edtHdWord.setVisibility(View.VISIBLE);
            layoutWords.setVisibility(View.GONE);

            edtHdWord.setText(this.getResources().getString(R.string.hdw_create_word));
            edtHdWord.setTextColor(Color.RED);
            edtHdWord.setBackgroundColor(Color.TRANSPARENT);
        }
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
                }
            }
        });
    }

    private void initViews() {
        hdwSeedLength = (SeekBar) findViewById(R.id.seekBar);
        btnNext = (Button) findViewById(R.id.btn_hdw_create);
        edtHdWord = (TextView) findViewById(R.id.hdw_word);
        seedSpinner = (Spinner) findViewById(R.id.seed_spinner);
        tvHdwSeedLength = (TextView) findViewById(R.id.hdw_seed_length_tv);
        hdwConfirm = (Button) findViewById(R.id.hdw_confirm);
        hdwSumLin = (LinearLayout) findViewById(R.id.hdw_sum_lin);
        tlCcreate = (TableLayout) findViewById(R.id.tl_create);
        hdwSumEt = (EditText) findViewById(R.id.hdw_sum_et);
        tvSeedType = (TextView) findViewById(R.id.tv_seed_type);

        layoutWords = (LinearLayout) findViewById(R.id.layoutWords);
        edtHdWord2 = (TextView) findViewById(R.id.hdw_word2);
        edtHdWord3 = (TextView) findViewById(R.id.hdw_word3);

        listSeed = new ArrayAdapter<String>(InitialCreateWalletIIActivity.this, android.R.layout.simple_spinner_item, strSeed);
        seedSpinner.setAdapter(listSeed);

        btnNext.setOnClickListener(this);
        hdwConfirm.setOnClickListener(this);
        seedSpinner.setOnItemSelectedListener(this);
        hdwSeedLength.setOnSeekBarChangeListener(this);

        //當介面再次顯示時，資料清空歸零
        isSeedOn = true;
        hdwSeedLength.setProgress(0);
        tvHdwSeedLength.setText(isSeedOn ? "48" : "12");
//        edtHdWord.setText(BIP39.getMnemonic(BIP39.getRandomString(128 / 8).getBytes()));
//        byte[] entropy = new byte[128 / 8];
//        new Random().nextBytes(entropy);
//        try {
//            edtHdWord.setText(BIP39.getMnemonic(entropy));
//        } catch (ValidationException ve) {
//            PublicPun.showNoticeDialog(this, "Error", ve.getMessage());
//        }
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
    public void onNothingSelected(AdapterView<?> parent) {

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
