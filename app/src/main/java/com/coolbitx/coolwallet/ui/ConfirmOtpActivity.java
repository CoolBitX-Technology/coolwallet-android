package com.coolbitx.coolwallet.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.coolbitx.coolwallet.DataBase.DatabaseHelper;
import com.coolbitx.coolwallet.DataBase.DbName;
import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.general.CSVReadWrite;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.ui.Fragment.FragMainActivity;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.LogUtil;

/**
 * Created by ShihYi on 2015/10/2.
 */
public class ConfirmOtpActivity extends BaseActivity implements View.OnClickListener {

    Button btnPair;
    boolean isFirst = false;
    String description;
    boolean isConfirm;
    Context context;
    private CmdManager cmdManager;
    private byte[] handle;//
    private byte[] loginChallenge;//登录的特征值
    private byte hostId = -1;//主机id
    private byte hostStatus;
    private String currentUuid = "";
    private String currentOptCode = "";
    private EditText editotp;
    private byte[] challenge;//特征值
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private CSVReadWrite mLoginCsv;
    private ProgressDialog mProgress;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_confirmotp);
        context = this;
        cmdManager = new CmdManager();
        mLoginCsv = new CSVReadWrite(ConfirmOtpActivity.this);
        sharedPreferences = getSharedPreferences("card", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        description = Build.DEVICE;
        initViews();

        mProgress = new ProgressDialog(ConfirmOtpActivity.this, ProgressDialog.THEME_HOLO_DARK);
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
        mProgress.setMessage(getString(R.string.paring));


        //通過當前狀態 判断是否是第一次注册
        isFirst = true;
        if (PublicPun.card.getMode().equals("NOHOST")) {
            isFirst = true;
        } else if (PublicPun.card.getMode().equals("DISCONN")) {
            isFirst = false;
        }
        boolean isRegistered = getIntent().getBooleanExtra("isRegistered", false);
        hostId = (byte) getIntent().getIntExtra("hostID", -1);
        LogUtil.i("是否曾註冊過卡片:" + isRegistered);


        if (isRegistered) {
            DatabaseHelper.deleteTable(getApplicationContext(), DbName.DB_TABLE_ADDR);
            DatabaseHelper.deleteTable(getApplicationContext(), DbName.DB_TABLE_TXS);
            PublicPun.showNoticeDialogToFinish(context, getString(R.string.wait_authorization), "");
        } else {
            genOTP();
        }
    }

    private void genOTP() {
        //generate otp
        cmdManager.bindRegInit(currentUuid, description, (isFirst ? 0x01 : 0x00), new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    if (outputData != null && outputData.length == 10) {
                        byte[] bs = new byte[4];
                        //取返回的handle的前4个字节,后6个字节是otp(新卡全是0，老卡带屏幕上的otp值)
                        for (int i = 0; i < 4; i++) {
                            bs[i] = outputData[i];
                        }
                        handle = bs;
                        LogUtil.d("bindRegInit 9000 = " + LogUtil.byte2HexString(bs));
                    }
                    //16進制的9000在10進制是36864;6645是26181;status=91717
                } else if ((status + 65536) == 0x16645) {
                    PublicPun.showNoticeDialogToFinish(context, getString(R.string.unable_to_pair), getString(R.string.maximum_paired_limitation));
                } else {
//                    LogUtil.i("status="+String.valueOf(status));
                    PublicPun.showNoticeDialogToFinish(context, getString(R.string.unable_to_pair), getString(R.string.error)+":" + Integer.toHexString(status));
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_cancel:
                BleActivity.bleManager.disConnectBle();
                finish();
                break;

            case R.id.btn_pair:
                mProgress.show();
                if (PublicPun.modeState.equals("PERSO") || PublicPun.modeState.equals("NORMAL") || PublicPun.modeState.equals("AUTH")) {
                    cmdManager.bindRegApprove(hostId, new CmdResultCallback() {
                        @Override
                        public void onSuccess(int status, byte[] outputData) {
                            if ((status + 65536) == 0x9000) {
                                LogUtil.i("OTP:" + "bindRegApprove success");
                                PublicPun.toast(getApplicationContext(), getString(R.string.approve_success));

                                if (PublicPun.modeState.equals("PERSO")) {
                                    //default security setting
                                    setPersoSecurity(false, true, false, false);
                                } else {
                                    Intent intent = new Intent(getApplicationContext(), FragMainActivity.class);
                                    intent.putExtra("Parent", ConfirmOtpActivity.class.getSimpleName());
                                    startActivity(intent);
                                }
                            }
                        }
                    });
                }

//                if((int) hostId >= 0){
//                    LogUtil.d("已綁定hostId ="+hostId);
//                } else {
//                LogUtil.d("No hostId");

                if (editotp.getText().length() != 0) {
                    currentOptCode = editotp.getText().toString().trim();

                    LogUtil.d("OTP:" + currentOptCode);

                    cmdManager.bindRegChlng(handle, new CmdResultCallback() {
                        @Override
                        public void onSuccess(int status, byte[] outputData) {
                            if ((status + 65536) == 0x9000) { //36864,status=-28672
                                if (outputData != null) {
                                    challenge = outputData;
                                    LogUtil.i("bindRegChlng success");

                                    cmdManager.bindRegFinish(handle, currentUuid, currentOptCode, challenge, new CmdResultCallback() {
                                        @Override
                                        public void onSuccess(int status, byte[] outputData) {
                                            if ((status + 65536) == 0x9000) {
                                                if (outputData != null && outputData.length == 2) {
                                                    LogUtil.i("PAIR成功");
                                                    mProgress.dismiss();
                                                    //註冊成功
                                                    //取得當前註冊設備的hostID
                                                    hostId = outputData[0];
                                                    LogUtil.e(PublicPun.byte2HexString(hostId));

                                                    //取得認證狀態
                                                    isConfirm = false;
                                                    if (outputData[1] == 0x00) {
                                                        isConfirm = true;
                                                    } else if (outputData[1] == 0x01) {
                                                        isConfirm = false;
                                                    }

                                                    LogUtil.i("bindRegFinish isConfirm:" + isConfirm + ", uuid:" + currentUuid + ",hostId" + hostId);

                                                    editor.putString("uuid", currentUuid);
                                                    editor.putString("optCode", currentOptCode);
                                                    editor.commit();

                                                    PublicPun.user.setUuid(currentUuid);
                                                    PublicPun.user.setOtpCode(currentOptCode);

                                                    DatabaseHelper.insertLogin(ConfirmOtpActivity.this, currentUuid, currentOptCode);

                                                    if (isConfirm) {
                                                        Intent intent = new Intent(getApplicationContext(), InitialPairSuccessfulActivity.class);
                                                        Bundle mBundle = new Bundle();
                                                        mBundle.putByte("hostId", hostId);
                                                        intent.putExtras(mBundle);
                                                        startActivity(intent);
                                                    } else {
                                                        //show alert 等授權
                                                        LogUtil.i("bindRegFinish2 isConfirm:" + isConfirm + ", uuid:" + currentUuid + ",hostId" + hostId);
                                                        DatabaseHelper.deleteTable(getApplicationContext(), DbName.DB_TABLE_ADDR);
                                                        DatabaseHelper.deleteTable(getApplicationContext(), DbName.DB_TABLE_TXS);
                                                        PublicPun.showNoticeDialogToFinish(context, getString(R.string.wait_authorization), "");
                                                    }
                                                }
                                            } else if ((status + 65536) == 0x16648) {
                                                mProgress.dismiss();
                                                PublicPun.showNoticeDialog(context, getString(R.string.unable_to_pair), getString(R.string.incorrect_otp_try_again));
                                                editotp.setText("");
                                                genOTP();
                                            } else {
                                                mProgress.dismiss();
                                                PublicPun.showNoticeDialogToFinish(context, getString(R.string.unable_to_pair), getString(R.string.error)+":" + Integer.toHexString(status));
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
                } else {
                    mProgress.dismiss();
                    PublicPun.showNoticeDialog(ConfirmOtpActivity.this, "Unable to pair error", "Please entry OTP");
                }
//                }
                break;
        }
    }

    private void getRegInfo() {
        byte first = 0x00;
        byte second = 0x01;
        byte third = 0x02;

        cmdManager.bindRegInfo(first, new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    if (outputData != null) {
//                        regInfoHost0.setText("第一个:" + dealRegInfo(outputData));
                    }
                }
            }
        });

        cmdManager.bindRegInfo(second, new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    if (outputData != null) {
//                        regInfoHost1.setText("第二个:" + dealRegInfo(outputData));
                    }
                }
            }
        });
        cmdManager.bindRegInfo(third, new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    if (outputData != null) {
//                        regInfoHost2.setText("第三个:" + dealRegInfo(outputData));
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                cmdManager.getModeState(new CmdResultCallback() {
                    @Override
                    public void onSuccess(int status, byte[] outputData) {
                        if ((status + 65536) == 0x9000) {//-28672//36864
                            PublicPun.card.setMode(PublicPun.selectMode(PublicPun.byte2HexString(outputData[0])));
                            PublicPun.card.setState(String.valueOf(outputData[1]));
                        }
                    }
                });

                getRegInfo();

                editor.putString("uuid", "");
                editor.putString("optCode", "");
                editor.commit();

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initViews() {

        btnPair = (Button) findViewById(R.id.btn_pair);
        editotp = (EditText) findViewById(R.id.editotp);

        currentUuid = sharedPreferences.getString("uuid", "");
        currentOptCode = sharedPreferences.getString("optCode", "");
        LogUtil.d("uuid=" + currentUuid + ";optCode=" + currentOptCode);
        btnPair.setOnClickListener(this);

        Button cancelButton = (Button) findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(this);
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



}
