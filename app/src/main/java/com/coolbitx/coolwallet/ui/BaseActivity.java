package com.coolbitx.coolwallet.ui;

import android.app.ActivityManager;
import android.app.Application;
import android.app.ApplicationErrorReport;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.NetworkOnMainThreadException;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.coolbitx.coolwallet.Service.BTConfig;
import com.coolbitx.coolwallet.bean.Constant;
import com.coolbitx.coolwallet.bean.Host;
import com.coolbitx.coolwallet.general.AppPrefrence;
import com.coolbitx.coolwallet.general.BtcUrl;
import com.coolbitx.coolwallet.general.NotificationReceiver;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.httpRequest.CwBtcNetWork;
import com.crashlytics.android.Crashlytics;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.LogUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by ShihYi on 2016/4/8.
 * 所有activity需要做的事
 */
public class BaseActivity extends AppCompatActivity{//AppCompatActivity {

    public static CmdManager cmdManager;
    Context mContext;
    public static boolean[] settingOptions = new boolean[4];
    String act;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.e("lifeCycle BaseActivity onCreate");
        mContext = this;
//        Fabric.with(this, new Crashlytics());

        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        int ind = cn.getShortClassName().lastIndexOf(".") + 1;//.ui.EraseActivity → EraseActivity
        act = cn.getShortClassName().substring(ind);

    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.e("lifeCycle BaseActivity onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.e("lifeCycle BaseActivity onResume");

        if (cmdManager == null) {
            cmdManager = new CmdManager();
        }
//        //註冊監聽
//        registerBroadcast(mContext,cmdManager);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.e("lifeCycle BaseActivity onPause");
//        unRegisterBroadcast(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.e("lifeCycle BaseActivity onStop");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.e("BaseActivity onDestroy");

    }

    NotificationReceiver brocastNR;

//    /**
//     * for all activity register
//     * @param context
//     * @param cmdManager
//     */

    public void registerBroadcast(Context context, CmdManager cmdManager) {
        LogUtil.e("registerBroadcast:"+act);
        //註冊監聽
        LocalBroadcastManager mLocalBroadcastManager;
//        if (brocastNR == null) {
            brocastNR = new NotificationReceiver(context,cmdManager);
//            brocastNR = new NotificationReceiver(Context context, CmdManager cmdManager);
            //註冊廣播
            mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
            mLocalBroadcastManager.registerReceiver(brocastNR, new IntentFilter(BTConfig.SOCKET_ADDRESS_MSG));
            mLocalBroadcastManager.registerReceiver(brocastNR, new IntentFilter(BTConfig.DISCONN_NOTIFICATION));
            mLocalBroadcastManager.registerReceiver(brocastNR, new IntentFilter(BTConfig.XCHS_NOTIFICATION));
//        }
    }

    public void unRegisterBroadcast(Context context) {
        try {


            if (brocastNR != null)
            {
                LogUtil.e("unRegisterBroadcast:"+act);
                LocalBroadcastManager.getInstance(context).unregisterReceiver(brocastNR);
                brocastNR = null;
            }

        } catch (Exception e) {
            brocastNR = null;
            LogUtil.e("error:" + e.getMessage());
        }
    }

    public void getSecpo() {
        final byte CwSecurityPolicyMaskOtp = 0x01;
        final byte CwSecurityPolicyMaskBtn = 0x02;
        final byte CwSecurityPolicyMaskWatchDog = 0x10;
        final byte CwSecurityPolicyMaskAddress = 0x20;

        cmdManager.getSecpo(new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
//                if ((status + 65536) == 0x9000) {
                    if (outputData != null && outputData.length > 0) {
                        settingOptions[0] = (outputData[0] & CwSecurityPolicyMaskOtp) >= 1;

                        settingOptions[1] = (outputData[0] & CwSecurityPolicyMaskBtn) >= 1;

                        settingOptions[2] = (outputData[0] & CwSecurityPolicyMaskAddress) >= 1;

                        settingOptions[3] = (outputData[0] & CwSecurityPolicyMaskWatchDog) >= 1;
                        LogUtil.i("安全設置:otp=" + InitialSecuritySettingActivity.settingOptions[0] +
                                ";button_up=" + InitialSecuritySettingActivity.settingOptions[1] +
                                ";address" + InitialSecuritySettingActivity.settingOptions[2] +
                                ";dog=" + InitialSecuritySettingActivity.settingOptions[3]);
//                        SetSecpo(true);
                    }
//                }
            }
        });
    }

    public void qryBlockBalance(int account) {

//        final byte cwHdwAllAccountInfo = 0x05;
        final byte cwHdwAccountInfoBlockAmount = 0x04;
        //[B5]
        cmdManager.hdwQueryAccountInfo(cwHdwAccountInfoBlockAmount, account, new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                LogUtil.e("帳戶block金額：" + PublicPun.byte2HexStringNoBlank(outputData));
            }
        });
    }

    public void FunTrxFinish() {
        cmdManager.trxFinish(new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    LogUtil.i("trxFinish成功");
                }
            }
        });
    }

    public void getUniqueId() {
        cmdManager.getUniqueId(new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    PublicPun.uid = PublicPun.byte2HexString(outputData).replace(" ", "");
                    LogUtil.i("getUniqueId:" + PublicPun.uid);
                }
            }
        });
    }

    public void getFwVersion() {
        cmdManager.getFwVersion(new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    PublicPun.fwVersion = PublicPun.byte2HexString(outputData).replace(" ", "");
                    LogUtil.i("getFwVersion:" + PublicPun.fwVersion);
                }
            }
        });
    }

    public void getModeState() {
        cmdManager.getModeState(new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {//-28672//36864
                    PublicPun.card.setMode(PublicPun.selectMode(PublicPun.byte2HexString(outputData[0])));
                    PublicPun.card.setState(String.valueOf(outputData[1]));

                    LogUtil.i("Main getModeState:" + " \nMode=" + PublicPun.card.getMode() + "\nState=" + PublicPun.card.getState());
                }
            }
        });
    }


    public void SetCurrencyRate(Context mContext) {
        int currRate = (int) (AppPrefrence.getCurrentRate(mContext) * 100);
        byte[] BigcurrData = ByteBuffer.allocate(4).putInt(currRate).order(ByteOrder.BIG_ENDIAN).array();

        byte[] currData = new byte[5];
        currData[0] = 0;
        for (int i = 0; i < BigcurrData.length; i++) {
            currData[i + 1] = BigcurrData[i];
        }
        cmdManager.SetCurrencyRate(currData, new CmdResultCallback() {
                    @Override
                    public void onSuccess(int status, byte[] outputData) {
                        if ((status + 65536) == 0x9000) {
                            LogUtil.d("Set CurrencyRate success.");
                        }
                    }
                }
        );
    }


    public void getHosts() {
        if (!PublicPun.hostList.isEmpty()) {
            PublicPun.hostList.clear();
        }
        final byte first = 0x00;
        final byte second = 0x01;
        final byte third = 0x02;
        cmdManager.bindRegInfo(first, new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    if (outputData != null && outputData.length > 0) {
                        addHostList(outputData, first);
                    }
                }
            }
        });
        cmdManager.bindRegInfo(second, new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    if (outputData != null && outputData.length > 0) {
                        addHostList(outputData, second);
                    }
                }
            }
        });
        cmdManager.bindRegInfo(third, new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    if (outputData != null && outputData.length > 0) {
                        addHostList(outputData, third);
                    }
                }
            }
        });
    }

    public void setPersoSecurity(boolean otp, boolean pressBtn, boolean switchAddress, boolean watchDog) {
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

    public void addHostList(byte[] outputData, byte hostId) {
        try {
            Host bean = new Host();
            byte bindStatus = outputData[0];
            int length = outputData.length - 1;
            byte[] desc = new byte[length];

            if (length > 0) {
                for (int i = 0; i < length; i++) {
                    desc[i] = outputData[i + 1];
                }
                bean.setBindStatus(bindStatus);
                bean.setId(hostId);
                bean.setDesc(new String(desc, Constant.UTF8).toString().trim());
                LogUtil.i("卡片描述 id=" + hostId + ";status" + bindStatus + ";" + ";desc=" + new String(desc, Constant.UTF8).toString().trim());
                PublicPun.hostList.add(bean);
            }
        } catch (Exception e) {
            LogUtil.e("addHostList error=" + e.getMessage());
            Crashlytics.log("addHostList error=" + e.getMessage());
        }

    }

    public void getCardId() {
        cmdManager.getCardId(new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {//8byte cardId
                    if (outputData != null) {
                        PublicPun.card.setCardId(PublicPun.byte2HexString(outputData).replace(" ", ""));
                        LogUtil.i("卡片id:" + PublicPun.card.getCardId());
                    }
                }
            }
        });
    }

    public void getCardName() {
        cmdManager.getCardName(new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {//32byte cardName
                    if (outputData != null) {
                        PublicPun.card.setCardName(new String(outputData, Constant.UTF8).trim());
                        LogUtil.i("卡片name:" + PublicPun.card.getCardName());
                        AppPrefrence.saveCardName(mContext, new String(outputData, Constant.UTF8).trim());
                    }
                }
            }
        });
    }



    /**
     * used to refresh rate by interval
     */
    public class MyRunnable implements Runnable {
        ContentValues cv;
        int what;
        Handler handler;
        int interval;
        int identify;
        String extraUrl;
        CwBtcNetWork cwBtcNetWork;

        public MyRunnable(Handler handler, ContentValues cv, String extraUrl, int what, int interval, int identify, CwBtcNetWork cwBtcNetWork) {
            this.cv = cv;
            this.what = what;
            this.handler = handler;
            this.interval = interval;
            this.identify = identify;
            this.extraUrl = extraUrl;
            this.cwBtcNetWork = cwBtcNetWork;
        }

        @Override
        public void run() {
            String result = "";
            try {
                result = cwBtcNetWork.doGet(cv, extraUrl, null);
                if (extraUrl.equals(BtcUrl.RECOMMENDED_TRANSACTION_FEES)) {
                    //feesRate
                    if (result != null) {
                        PublicPun.jsonParsingFeeaRate(mContext, result);
                    }
                } else {
                    //exchangeRate
                    if (result != null) {
                        PublicPun.jsonParserBlockChainRate(mContext, result);
                    }
                }
                Message msg = new Message();
                msg.what = what;
                Bundle data = new Bundle();
                data.putString("identify", extraUrl);
                data.putString("result", result);
                msg.setData(data);
                handler.sendMessage(msg);
            } catch (NetworkOnMainThreadException e) {
                LogUtil.i("doGet 錯誤:" + e.toString());
                Crashlytics.logException(e);
            }


            if (interval > 0) {
//                下面這段會造成NetWorkOnMainThreadException:
//                handler.postDelayed(this, interval);
//                Handler.postDelayed() executes the Runnable in the Thread in which the Handler was created.
//                In your case you create it in your Activity on the UI Thread. So the first time,
//                the Runnable gets executed in a separate Thread, but the second time in the UI Thread.
                handler.postDelayed(new Runnable() {
                    public void run() {
//                        new Thread(new MyRunnable(mHandler, cv, BtcUrl.URL_BLOCKR_EXCHANGE_RATE, 0, 60000 * 60, 0)).start();//1hr
                        new Thread(new MyRunnable(handler, cv, extraUrl, what, interval, identify, cwBtcNetWork)).start();
                    }
                }, interval);
            }
        }
    }

}