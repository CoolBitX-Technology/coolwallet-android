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
import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.general.CSVReadWrite;
import com.coolbitx.coolwallet.general.DbName;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.ui.Fragment.FragMainActivity;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.LogUtil;

/**
 * Created by ShihYi on 2015/10/2.
 */
public class ConfirmOtpActivity extends BaseActivity implements View.OnClickListener {

    private CmdManager cmdManager;
    private byte[] handle;//
    private byte[] loginChallenge;//登录的特征值
    private byte hostId = -1;//主机id
    private byte hostStatus;
    Button btnPair;
    private String currentUuid = "";
    private String currentOptCode = "";
    private final static String CHARSETNAME = "UTF-8";
    private EditText editotp;
    private byte[] challenge;//特征值
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    boolean isFirst = false;
    String description;
    boolean isConfirm;
    private CSVReadWrite mLoginCsv;
    Context context;
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

        mProgress = new ProgressDialog(ConfirmOtpActivity.this);
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
        mProgress.setMessage("Pairing...");
        //睡300毫秒
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        getRegInfo();

        //通過當前狀態 判断是否是第一次注册
        isFirst = true;

        if (PublicPun.card.getMode().equals("NOHOST")) {
            isFirst = true;
        } else if (PublicPun.card.getMode().equals("DISCONN")) {
            isFirst = false;

        }
        LogUtil.i("是否第一次註冊:" + isFirst);


        boolean isRegistered = getIntent().getBooleanExtra("isRegistered", false);

        if (isRegistered) {
            DatabaseHelper.deleteTable(getApplicationContext(), DbName.DATA_BASE_ADDR);
            DatabaseHelper.deleteTable(getApplicationContext(), DbName.DATA_BASE_TXS);
            PublicPun.ClickFunctionToFinish(context, "Waiting for authorization from paired device", "");
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
                        LogUtil.i("bindRegInit 9000 = " + LogUtil.byte2HexString(bs));
                    }
                    //16進制的9000在10進制是36864;6645是26181;status=91717
                } else if ((status + 65536) == 0x16645) {
                    PublicPun.ClickFunctionToFinish(context, "Unable to pair", "maximum number of 3 hosts have been paired with this card");
                } else {
//                    LogUtil.i("status="+String.valueOf(status));
                    PublicPun.ClickFunctionToFinish(context, "Unable to pair", "Error:" + Integer.toHexString(status));
                }
            }
        });
    }

    private void setPersoSecurity(boolean otp, boolean pressBtn, boolean switchAddress, boolean watchDog) {
        boolean[] settingOptions = new boolean[4];
        settingOptions[0] = otp;
        settingOptions[1] = pressBtn;
        settingOptions[2] = switchAddress;
        settingOptions[3] = watchDog;

        cmdManager.persoSetData(PublicPun.user.getMacKey(), settingOptions, new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    cmdManager.persoConfirm(new CmdResultCallback() {
                        @Override
                        public void onSuccess(int status, byte[] outputData) {
                            if ((status + 65536) == 0x9000) {
                                Intent intent = new Intent(getApplicationContext(), InitialCreateWalletActivity.class);
                                startActivity(intent);
                            }
                        }
                    });
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
                                PublicPun.toast(getApplicationContext(), "Approve success");

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
                if ((int) hostId >= 0) {
                    LogUtil.i("已綁定 hostId >= 0");
                } else {
                    LogUtil.i("No hostId");

                    if (editotp.getText().length() != 0) {
                        currentOptCode = editotp.getText().toString().trim();

                        LogUtil.i("OTP:" + currentOptCode);

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
                                                        //注册成功
                                                        //登录按钮变为可用状态

                                                        //取得当前注册设备的hostId
                                                        hostId = outputData[0];
                                                        LogUtil.e(PublicPun.byte2HexString(hostId));

                                                        //取得认证状态
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
//                                                        try {
//                                                            mLoginCsv.setSaveFileName(PublicPun.csvFilename, true);
//                                                            mLoginCsv.saveLoginToCSV(PublicPun.user);
                                                        DatabaseHelper.insertLogin(ConfirmOtpActivity.this, currentUuid, currentOptCode);
//                                                        } catch (IOException e) {
//                                                            LogUtil.i("saveLoginToCSV錯誤:" + e.getMessage());
//                                                            e.printStackTrace();
//                                                        }

                                                        if (isConfirm) {
                                                            Intent intent = new Intent(getApplicationContext(), InitialPairSuccessfulActivity.class);
                                                            Bundle mBundle = new Bundle();
                                                            mBundle.putByte("hostId", hostId);
                                                            intent.putExtras(mBundle);
                                                            startActivity(intent);
                                                        } else {
                                                            //show alert 等授權
                                                            LogUtil.i("bindRegFinish2 isConfirm:" + isConfirm + ", uuid:" + currentUuid + ",hostId" + hostId);
                                                            DatabaseHelper.deleteTable(getApplicationContext(), DbName.DATA_BASE_ADDR);
                                                            DatabaseHelper.deleteTable(getApplicationContext(), DbName.DATA_BASE_TXS);
                                                            PublicPun.ClickFunctionToFinish(context, "Waiting for authorization from paired device", "");
                                                        }
                                                    }
                                                } else if ((status + 65536) == 0x16648) {
                                                    mProgress.dismiss();
                                                    PublicPun.ClickFunction(context, "Unable to pair", "Incorrect OTP, Please try again.");
                                                    editotp.setText("");
                                                    genOTP();
                                                } else {
                                                    mProgress.dismiss();
                                                    PublicPun.ClickFunctionToFinish(context, "Unable to pair", "Error:" + Integer.toHexString(status));
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        });
                    } else {
                        mProgress.dismiss();
                        PublicPun.ClickFunction(ConfirmOtpActivity.this, "Unable to pair error", "Please entry OPT");
                    }
                }
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
        LogUtil.i("uuid=" + currentUuid + ";optCode=" + currentOptCode);
        btnPair.setOnClickListener(this);

        Button cancelButton = (Button) findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(this);
    }
}
