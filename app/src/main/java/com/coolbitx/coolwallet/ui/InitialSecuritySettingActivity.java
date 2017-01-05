package com.coolbitx.coolwallet.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.general.PublicPun;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.LogUtil;

/**
 * Created by ShihYi on 2015/10/20.
 */
public class InitialSecuritySettingActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {
    private Switch switchOtp;
    private Switch switchEnablePressButton;
    private Switch switchDog;
    private SeekBar seekBar;
    private Switch switchAddress;
    private Button btnPair;
    //    private AppPrefrence mAppPrefrence = null;
    private Context mContext = null;
    private CmdManager cmdManager;

    public static boolean[] settingOptions = new boolean[4];
    private byte CwSecurityPolicyMaskOtp = 0x01;
    private byte CwSecurityPolicyMaskBtn = 0x02;
    private byte CwSecurityPolicyMaskWatchDog = 0x10;
    private byte CwSecurityPolicyMaskAddress = 0x20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_securitysetting);

        initToolbar();

        mContext = InitialSecuritySettingActivity.this;
        cmdManager = new CmdManager();

        initViews();

        switchOtp.setOnCheckedChangeListener(this);
        switchEnablePressButton.setOnCheckedChangeListener(this);
        switchDog.setOnCheckedChangeListener(this);
        switchAddress.setOnCheckedChangeListener(this);

        switchEnablePressButton.setChecked(true);

        btnPair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //switch setting
                settingOptions[0] = switchOtp.isChecked();
                settingOptions[1] = switchEnablePressButton.isChecked();
                settingOptions[2] = switchAddress.isChecked();
                settingOptions[3] = switchDog.isChecked();

                LogUtil.i("switchOtp:" + switchOtp.isChecked() + ",switchEnablePressButton:" + switchEnablePressButton.isChecked() +
                        ",switchAddress:" + switchAddress.isChecked() + ",switchDog:" + switchDog.isChecked());

                if (PublicPun.card.getMode().equals("NORMAL")) {
                    cmdManager.setSecpo(settingOptions, new CmdResultCallback() {
                        @Override
                        public void onSuccess(int status, byte[] outputData) {
                            if ((status + 65536) == 0x9000) {
                                PublicPun.showNoticeDialog(mContext, "Security policy set","");
                            }
                        }
                    });

                } else if (PublicPun.card.getMode().equals("PERSO")) {
                    cmdManager.persoSetData(PublicPun.user.getMacKey(), settingOptions, new CmdResultCallback() {
                        @Override
                        public void onSuccess(int status, byte[] outputData) {
                            if ((status + 65536) == 0x9000) {
                                cmdManager.persoConfirm(new CmdResultCallback() {
                                    @Override
                                    public void onSuccess(int status, byte[] outputData) {
                                        if ((status + 65536) == 0x9000) {
                                            PublicPun.showNoticeDialog(mContext, "Security policy set", "");
                                            Intent intent = new Intent(getApplicationContext(), InitialCreateWalletActivity.class);
                                            startActivity(intent);
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

        if (PublicPun.card.getMode().equals("NORMAL")) {
            getSecpo();
        } else {
            btnPair.performClick();
        }

    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(getResources().getDrawable(R.mipmap.security));
        toolbar.setTitle("Security");
        setSupportActionBar(toolbar);
        // Navigation Icon設定在 setSupoortActionBar後才有作用,否則會出現 back button_up
        toolbar.setNavigationIcon(R.mipmap.menu_3x);

        ActionBar actionBar = getSupportActionBar();
        // 打開 up button_up
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }
    @Override
    public Intent getSupportParentActivityIntent() {
        finish();
        return null;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        LogUtil.i("onCheckedChanged");
        int id = buttonView.getId();
        switch (id) {
            case R.id.switchOtp:
                if (isChecked) {
//                    switchEnablePressButton.setChecked(false);
                } else {
                    switchEnablePressButton.setChecked(true);
                }
//                mAppPrefrence.setSecurity_setting_OTP(mContext, isChecked);
                break;
            case R.id.switch_enable_press_button:
                if (isChecked) {
//                    switchOtp.setChecked(false);
                } else {
                    switchOtp.setChecked(true);
                }
                break;
            case R.id.switch_dog:
                break;
            case R.id.switchAddress:
                break;
        }
        LogUtil.i("switch:" + id + " isChecked:" + isChecked);
    }

    private void getSecpo() {

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

                            LogUtil.i("安全設置:otp="+settingOptions[0]+";button_up="+settingOptions[1]+";address"+settingOptions[2]+";dog="+settingOptions[3]);
                            //初始化安全设置
                            switchOtp.setChecked(settingOptions[0]);
                            switchEnablePressButton.setChecked(settingOptions[1]);
                            switchAddress.setChecked(settingOptions[2]);
                            switchDog.setChecked(settingOptions[3]);
                        }
                    }
                }
            });
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

    private void initViews() {
        switchOtp = (Switch) findViewById(R.id.switchOtp);
        switchEnablePressButton = (Switch) findViewById(R.id.switch_enable_press_button);
        switchDog = (Switch) findViewById(R.id.switch_dog);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        switchAddress = (Switch) findViewById(R.id.switchAddress);
        btnPair = (Button) findViewById(R.id.btn_update_sec);

    }

}
