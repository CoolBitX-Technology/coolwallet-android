package com.coolbitx.coolwallet.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.bean.Constant;
import com.coolbitx.coolwallet.general.PublicPun;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.LogUtil;

/**
 * Created by ShihYi on 2015/11/30.
 */
public class InitialPairSuccessfulActivity extends BaseActivity implements View.OnClickListener {

    Button btnPair;
    private Context context;
    private CmdManager cmdManager;
    private byte[] loginChallenge;//登录的特征值
    private byte hostId;//主机id
    private String currentUuid = "";
    private String currentOptCode = "";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_pair_successful);
//        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//        getSupportActionBar().setCustomView(R.layout.custom_actionbar);
        context = this;
        cmdManager = new CmdManager();
        sharedPreferences = getSharedPreferences("card", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Bundle bundle = new Bundle();
        hostId = bundle.getByte("hostId");
        LogUtil.i("hostId:" + hostId);
        initViews();

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


    @Override
    public void onBackPressed() {
        try {
            BleActivity.bleManager.disConnectBle();
            finish();

            Intent intent = new Intent(this, BleActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    private void initViews() {

        btnPair = (Button) findViewById(R.id.btn_pair_next);
        btnPair.setOnClickListener(this);

        currentUuid = sharedPreferences.getString("uuid", "");
        currentOptCode = sharedPreferences.getString("optCode", "");
        LogUtil.i("uuid:" + currentUuid + ",optCode:" + currentOptCode);
        mProgress = new ProgressDialog(InitialPairSuccessfulActivity.this, ProgressDialog.THEME_HOLO_DARK);
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
    }

    @Override
    public void onClick(View v) {
        if (v == btnPair) {
            // Handle clicks for btnPair
            mProgress.setMessage(getString(R.string.login_host));
            mProgress.show();

            cmdManager.bindLoginChlng(hostId, new CmdResultCallback() {
                @Override
                public void onSuccess(int status, byte[] outputData) {
                    if ((status + 65536) == 0x9000) {
                        if (outputData != null) {
                            loginChallenge = outputData;//16
                            LogUtil.i("loginChallenge=" + PublicPun.printBytes(loginChallenge));
                            cmdManager.bindLogin(currentUuid, currentOptCode, loginChallenge, hostId, new CmdResultCallback() {
                                @Override
                                public void onSuccess(int status, byte[] outputData) {
                                    if ((status + 65536) == 0x9000) {
                                        //计算enckey和mackey
                                        StringBuilder sb = new StringBuilder();
                                        sb.append(currentUuid);//32
                                        sb.append(currentOptCode);//6
                                        String info = sb.toString();
                                        byte[] devKey = PublicPun.encryptSHA256(info.getBytes(Constant.UTF8));

//                               LogUtil.e("devKey=" + PublicPun.printBytes(devKey) + "\n"
//                                + "devKey长度=" + devKey.length);

                                        byte[] encKey = PublicPun.encryptSHA256(calcKey(devKey, "ENC"));
                                        byte[] macKey = PublicPun.encryptSHA256(calcKey(devKey, "MAC"));

                                        PublicPun.user.setUuid(currentUuid);
                                        PublicPun.user.setOtpCode(currentOptCode);
                                        PublicPun.user.setEncKey(encKey);
                                        PublicPun.user.setMacKey(macKey);

//                        LogUtil.e(PublicPun.printBytes(PublicPun.BIND_SENCK) + "长度=" + PublicPun.BIND_SENCK.length);
//                        LogUtil.e(PublicPun.printBytes(PublicPun.BIND_SMACK) + "长度=" + PublicPun.BIND_SMACK.length);

//                                        PublicPun.toast(context, "Login successful");


                                        cmdManager.getModeState(new CmdResultCallback() {
                                            @Override
                                            public void onSuccess(int status, byte[] outputData) {
                                                if ((status + 65536) == 0x9000) {//-28672//36864

                                                    PublicPun.card.setMode(PublicPun.selectMode(PublicPun.byte2HexString(outputData[0])));
                                                    PublicPun.card.setState(String.valueOf(outputData[1]));

                                                    PublicPun.modeState = PublicPun.selectMode(PublicPun.byte2HexString(outputData[0]));
//                                                    PublicPun.toast(getApplicationContext(), "Connected..\nMode=" + PublicPun.modeState + "\nState=" + outputData[1]);
                                                    LogUtil.i("after PairSuccessful  ModeState:" + "PAIRED \nMode=" + PublicPun.modeState + "\nState=" + outputData[1]);

                                                    if (PublicPun.modeState.equals("PERSO")) {
//                                                        Intent intent = new Intent(getApplicationContext(), InitialSecuritySettingActivity.class);
//                                                        startActivity(intent);
                                                        setPersoSecurity(false, true, false, false);
                                                    } else {
                                                        Intent intent = new Intent(getApplicationContext(), InitialCreateWalletIIActivity.class);
                                                        startActivity(intent);
                                                    }

                                                }
                                            }
                                        });

                                    }
                                }
                            });
                        }
                    }
                }
            });


        }
    }

    private byte[] calcKey(byte[] devKey, String s) {
        byte[] ss = s.getBytes(Constant.UTF8);
        int length1 = loginChallenge.length;
        int length2 = devKey.length;
        int length3 = ss.length;

        int length = length1 + length2 + length3;
        byte[] bytes = new byte[length];
        for (int i = 0; i < length1; i++) {
            bytes[i] = loginChallenge[i];
        }

        for (int i = 0; i < length2; i++) {
            bytes[i + length1] = devKey[i];
        }

        for (int i = 0; i < length3; i++) {
            bytes[i + length1 + length2] = ss[i];
        }

        LogUtil.e(PublicPun.printBytes(bytes));

        return bytes;
    }
}
