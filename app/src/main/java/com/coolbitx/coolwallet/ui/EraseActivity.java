package com.coolbitx.coolwallet.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.coolbitx.coolwallet.DataBase.DatabaseHelper;
import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.general.CSVReadWrite;
import com.coolbitx.coolwallet.general.DbName;
import com.coolbitx.coolwallet.general.PublicPun;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.LogUtil;

/**
 * Created by MyPC on 2015/8/28.
 */
public class EraseActivity extends AppCompatActivity implements View.OnClickListener {

    private Context context;
    private EditText editOTP;
    private Button btnErase;
    private Button btnCancel;

    private CmdManager cmdManager;
    private byte[] pinChllenge;
    private byte currentHostId = 0x01;//当前手机设备uuid对应的hostid
    private String currentUuid;//当前手机设备uuid
    private SharedPreferences sharedPreferences;
    private ProgressDialog mProgress;
    private CSVReadWrite mLoginCsv;
    boolean isNewCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_erase);
        context = this;

        cmdManager = new CmdManager();
        mLoginCsv = new CSVReadWrite(EraseActivity.this);
        mProgress = new ProgressDialog(EraseActivity.this);
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);

        //睡毫秒
        mProgress.setMessage("Generating Reset OTP...");
        mProgress.show();
        genOTP();

        sharedPreferences = getSharedPreferences("card", Context.MODE_PRIVATE);
        currentUuid = sharedPreferences.getString("uuid", "");
        initViews();

    }


    private void genOTP(){
        //新版reset,如果出6601代表是舊版
        cmdManager.genResetOTP(new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    isNewCard = true;
                    mProgress.dismiss();
                } else if ((status + 65536) == 0x16601) {
                    isNewCard = false;
                    editOTP.setVisibility(View.GONE);
                    mProgress.dismiss();
                }
            }
        });
    }

    private void initViews() {

        editOTP = (EditText) findViewById(R.id.editotp);
        btnErase = (Button) findViewById(R.id.btn_reset);
        btnCancel = (Button) findViewById(R.id.btn_cancel);

        btnErase.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        //工程版使用
        if (v == btnCancel) {
//            PublicPun.toast(context, "CoolWallet has been reset.");
            setResult(RESULT_OK);
            finish();

        } else if (v == btnErase) {
            //clear txs data;

            DatabaseHelper.deleteTable(EraseActivity.this, DbName.DATA_BASE_TXS);
            DatabaseHelper.deleteTable(EraseActivity.this, DbName.DATA_BASE_ADDR);
            DatabaseHelper.deleteTable(context, DbName.DATA_BASE_LOGIN);

            LogUtil.i("Erase mode=" + PublicPun.card.getMode());
            if (PublicPun.card.getMode().equals("NOHOST")) {
                PublicPun.toast(context, "CoolWallet has been reset.");
                BleActivity.bleManager.disConnectBle();
                finish();
                System.exit(0);
            } else {
                final String optCode = editOTP.getText().toString().trim();

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                                       /* Show the progress. */
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                mProgress.setMessage("Resetting...");
                                mProgress.show();
                            }
                        });
                        //處理程式寫在此
                        if(isNewCard) {
                            cmdManager.verivyResetOTP(optCode, new CmdResultCallback() {
                                @Override
                                public void onSuccess(int status, byte[] outputData) {
                                    if ((status + 65536) == 0x9000) {
                                        resetCard();
                                    } else if ((status + 65536) == 0x16606) {
                                        mProgress.dismiss();
//                                        ClickFunction("Erro Message", "Error:" + "OTP incorrect !");
                                        PublicPun.ClickFunction(context,"Incorrect OTP ", "Please try again");
                                        editOTP.setText("");
                                        genOTP();
                                    } else {
                                        mProgress.dismiss();
                                        PublicPun.ClickFunctionToFinish(context,"Error Message", "Error:" + Integer.toHexString(status));
                                    }
                                }
                            });
                        }else{
                            resetCard();
                        }
                    }
                };
                thread.start();
            }
        }
    }

    private void resetCard() {
        //獲取pin碼的特征值
        cmdManager.pinChlng(new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    if (outputData != null && outputData.length > 0) {
                        pinChllenge = outputData;

                        cmdManager.bindBackNoHost(PublicPun.card.getMode(), pinChllenge,
                                PublicPun.oldPin,
                                PublicPun.newPin,
                                new CmdResultCallback() {
                                    @Override
                                    public void onSuccess(int status, byte[] outputData) {
                                        if ((status + 65536) == 0x9000) {
                                            mProgress.dismiss();
                                            PublicPun.toast(context, "CoolWallet has been reset.");

                                            setResult(RESULT_OK);
                                            finish();
                                        }else{
                                            mProgress.dismiss();
                                            PublicPun.ClickFunctionToFinish(context,"Error Message", "Error:" + Integer.toHexString(status));
                                        }
                                    }
                                });
                    }
                }
            }
        });
    }

}
