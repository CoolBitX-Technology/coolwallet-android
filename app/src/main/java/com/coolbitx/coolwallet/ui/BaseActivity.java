package com.coolbitx.coolwallet.ui;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
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
import android.widget.Toast;

import com.coolbitx.coolwallet.DataBase.DatabaseHelper;
import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.Service.BTConfig;
import com.coolbitx.coolwallet.callback.RefreshCallback;
import com.coolbitx.coolwallet.entity.Constant;
import com.coolbitx.coolwallet.entity.Host;
import com.coolbitx.coolwallet.entity.dbAddress;
import com.coolbitx.coolwallet.entity.socketByAddress;
import com.coolbitx.coolwallet.general.AppPrefrence;
import com.coolbitx.coolwallet.general.BtcUrl;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.general.RefreshBlockChainInfo;
import com.coolbitx.coolwallet.httpRequest.CwBtcNetWork;
import com.coolbitx.coolwallet.ui.Fragment.BSConfig;
import com.coolbitx.coolwallet.ui.Fragment.TabFragment;
import com.crashlytics.android.Crashlytics;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.ByteUtil;
import com.snscity.egdwlib.utils.LogUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * Created by ShihYi on 2016/4/8.
 * 所有activity需要做的事
 * for disconn 廣播
 */
public class BaseActivity extends AppCompatActivity {
    public static disconnNotificationReceiver brocastNR = null;
    public static CmdManager cmdManager;
    public boolean isShowDisconnAlert = true;
    Context mContext;
    public static boolean[] settingOptions = new boolean[4];
    LocalBroadcastManager mLocalBroadcastManager = null;
    //for Exchange test.
    BroadcastReceiver ExchangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("ExchangeMessage");
            String data = intent.getStringExtra("ExchangeData");
            if (message != null) {
                //Receive message from Exchange site
                LogUtil.d("ExchangeMessage=" + message);

                if (data.contains("matchOrder")) {
                    PublicPun.showNoticeDialog(mContext, "Exchange Message",
                            "Congrats!! Your sell order has a match. Please connect with CoolWallet CW000522 to complete the trade.");
                } else {
                    PublicPun.showNoticeDialog(mContext, "Exchange Message",
                            "You placed a sell order on CoolBitX. Please connect with CoolWallet <cwid>, look at the OTP on card, type it in the app to confirm the order.");
                }
                //complete order 尚未完成
            }
        }
    };
    private Handler brocastMsgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case BSConfig.HANDLER_SOCKET:
                    final socketByAddress socket = (socketByAddress) msg.obj;
                    final int mAccount = DatabaseHelper.queryAccountByAddress(mContext, socket.getAddress());
                    if (socket.getTx_type().equals("Received") && socket.getConfirmations() == 1) {//
                        // do your work right here
                        String socketTitle = "BitCoin Received";
                        String socketMsg = "Account " + (mAccount + 1) + "\n"
                                + "Address:" + "\n"
                                + socket.getAddress() + "\n"
                                + socket.getTx_type() + " Amount:" + TabFragment.BtcFormatter.format(socket.getBtc_amount()) + " BTC" + "\n"
                                + "Confirmations: " + socket.getConfirmations();
//                        PublicPun.showNoticeDialog(mContext, socketTitle, socketMsg);
                        systemNotificationBTC(socket, mAccount);
                    }

                    //refresh the transaction data when balance updated.
                    if (mAccount >= 0) {
                        RefreshBlockChainInfo refreshBlockChainInfo = new RefreshBlockChainInfo(mContext, mAccount);
                        refreshBlockChainInfo.callTxsRunnable(new RefreshCallback() {
                            @Override
                            public void success() {
                                RefreshSetAccInfo(mAccount);
                            }

                            @Override
                            public void fail(String msg) {
                                Toast.makeText(mContext, "Unstable internet connection", Toast.LENGTH_LONG);
                            }
                        });
                    }
                    break;

                case BSConfig.HANDLER_DISCONN:

                    String title = "CoolWallet Disconnected";
                    ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                    int ind = cn.getShortClassName().lastIndexOf(".")+1;//.ui.EraseActivity → EraseActivity
                    String act = cn.getShortClassName().substring(ind);

                    LogUtil.e("BaseActivity HANDLER_DISCONN actitvity="+act);
//                    if(act.equals("BleActivity")){
//                        SharedPreferences settings = getSharedPreferences("Preference", 0);
//                        //取出name屬性的字串
//                        String address = settings.getString("connAddress", "");
//                        bleManager.connectBle(address);
//
//                    }else{
//                        LogUtil.e("not BleActivity");
//                        String noteMsg;
//                        if (PublicPun.card.getCardName() == null) {
//                            noteMsg = "CoolWallet Disconnected";
//                        } else {
//                            noteMsg = PublicPun.card.getCardName() + " Disconnected";
//                        }
//                        PublicPun.showNoticeDialogToFinish(mContext, title, noteMsg);
//                        systemNotification(title, noteMsg);
//                    }
                    String noteMsg;
                    if (PublicPun.card.getCardName() == null) {
                        noteMsg = "CoolWallet Disconnected";
                    } else {
                        noteMsg = PublicPun.card.getCardName() + " Disconnected";
                    }
                    PublicPun.showNoticeDialogToFinish(mContext, title, noteMsg);
                    systemNotification(title, noteMsg);

                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.d("BaseActivity onCreate");
        mContext = this;

    }

    @Override
    protected void onResume() {
        super.onResume();
        //註冊監聽
        if (brocastNR == null) {
            brocastNR = new disconnNotificationReceiver();
            //註冊廣播
            LogUtil.d("BaseActivity registerReceiver brocastNR");
            registerReceiver(brocastNR, new IntentFilter(BTConfig.SOCKET_ADDRESS_MSG));
            registerReceiver(brocastNR, new IntentFilter(BTConfig.DISCONN_NOTIFICATION));
        }
        if (mLocalBroadcastManager == null) {
            mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
            mLocalBroadcastManager.registerReceiver(ExchangeReceiver,
                    new IntentFilter("ExchangeSiteReceiver"));
        }

        if (cmdManager == null) {
            cmdManager = new CmdManager();
        }
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.e("BaseActivity onDestroy");
        try {
            if (brocastNR != null) {
                unregisterReceiver(brocastNR);
                brocastNR = null;
            }
            if (ExchangeReceiver != null) {
                mLocalBroadcastManager.unregisterReceiver(ExchangeReceiver);
                ExchangeReceiver = null;
            }
        } catch (Exception e) {
//            Crashlytics.logException(e);
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
                if ((status + 65536) == 0x9000) {
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
     * show on Status Bar
     * foe btc recv
     */
    private void systemNotificationBTC(socketByAddress socket, int mAccount) {
        final int notifyID = mAccount; // 通知的識別號碼

        String socketTitle = "BitCoin Received";
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE); // 取得系統的通知服務
        final Notification notification = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher_cw)
                .setStyle(new Notification.InboxStyle()
                        .setBigContentTitle(socketTitle)
                        .addLine("Account " + (mAccount + 1))
                        .addLine("Address:")
                        .addLine(socket.getAddress())
                        .addLine(socket.getTx_type() + " Amount:" + TabFragment.BtcFormatter.format(socket.getBtc_amount()) + " BTC")
                        .addLine("Confirmations: " + socket.getConfirmations())).build();
//                .setContentIntent(PendingIntent.getActivity(getApplicationContext(), notifyID, new Intent(getApplicationContext(), BleActivity.class), PendingIntent.FLAG_UPDATE_CURRENT)).build();
        notificationManager.notify(notifyID, notification); // 發送通知
    }

    /**
     * show on Status Bar
     * foe ble disconn
     */
    private void systemNotification(String title, String msg) {
        final int notifyID = 999; // 通知的識別號碼
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE); // 取得系統的通知服務
        final Notification notification = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher_cw)
                .setContentTitle(title)
                .setContentText(msg) // 建立通知
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(), notifyID, new Intent(getApplicationContext(), BleActivity.class), PendingIntent.FLAG_UPDATE_CURRENT)).build();
        notificationManager.notify(notifyID, notification); // 發送通知
    }

    public void RefreshSetAccInfo(int account) {

        LogUtil.e("這是Main FunhdwSetAccInfo=" + account);
        byte ByteAccId = (byte) account;

        //for card display

        cmdManager.McuSetAccountState(ByteAccId, new CmdResultCallback() {
                    @Override
                    public void onSuccess(int status, byte[] outputData) {
                        if ((status + 65536) == 0x9000) {
                        }
                    }
                }
        );

        //set CW Card
        final byte cwHdwAccountInfoName = 0x00;
        final byte cwHdwAccountInfoBalance = 0x01;
        final byte cwHdwAccountInfoExtKeyPtr = 0x02;
        final byte cwHdwAccountInfoIntKeyPtr = 0x03;

        byte[] accountInfo = new byte[32];
        long TotalBalance = 0;
        int extKey = 0;
        int intKey = 0;
        ArrayList<dbAddress> listAddress = new ArrayList<dbAddress>();
        listAddress = DatabaseHelper.queryAddress(mContext, account, -1);//ext+int

        LogUtil.i("before set socket account=" + PublicPun.accountSocketReg[account]);

        for (int i = 0; i < listAddress.size(); i++) {
            TotalBalance += listAddress.get(i).getBalance();
            if (listAddress.get(i).getKcid() == 0) {
                extKey++;
            }
            if (listAddress.get(i).getKcid() == 1) {
                intKey++;
            }
        }

        final boolean[] flag = new boolean[4];

        final byte[] cwHdwAccountInfo = new byte[]{cwHdwAccountInfoName, cwHdwAccountInfoBalance,
                cwHdwAccountInfoExtKeyPtr, cwHdwAccountInfoIntKeyPtr};

        for (int i = 0; i < cwHdwAccountInfo.length; i++) {
            final int setAcctInfoIndex = i;
            LogUtil.i("SWITCH=" + cwHdwAccountInfo[setAcctInfoIndex]);
            switch (cwHdwAccountInfo[setAcctInfoIndex]) {
                case cwHdwAccountInfoName:
                    //00h: Account name (32 bytes)
                    accountInfo = new byte[32];
                    break;

                case cwHdwAccountInfoBalance:
                    accountInfo = new byte[8];
                    //204E000000000000
//                    byte[] newBalanceBytes = ByteUtil.intToByteLittle(TotalBalance, 8);
                    byte[] newBalanceBytes =
                            ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong(TotalBalance).array();
                    accountInfo = newBalanceBytes;
                    break;

                case cwHdwAccountInfoExtKeyPtr:
                    accountInfo = new byte[4];
                    accountInfo = ByteUtil.intToByteLittle(extKey, 4);
//                    accountInfo = ByteUtil.intToByteLittle(5, 4);
                    break;

                case cwHdwAccountInfoIntKeyPtr:
                    accountInfo = new byte[4];
                    accountInfo = ByteUtil.intToByteLittle(intKey, 4);
//                    accountInfo = ByteUtil.intToByteLittle(10, 4);
                    break;
            }
            LogUtil.i("hdwSetAccInfo:" + account + "的 " + setAcctInfoIndex + " =" + PublicPun.byte2HexString(accountInfo));
            cmdManager.hdwSetAccInfo(PublicPun.user.getMacKey(), cwHdwAccountInfo[setAcctInfoIndex], account, accountInfo, new CmdResultCallback() {
                        @Override
                        public void onSuccess(int status, byte[] outputData) {
                            if ((status + 65536) == 0x9000) {
                                flag[setAcctInfoIndex] = true;

                                if (flag[0] && flag[1] && flag[2] && flag[3]) {
//                                    PublicPun.toast(mContext, "Data synced.");
                                }

                            } else {
//                                PublicPun.toast(mContext, "Data synced failed!");
                            }
                        }
                    }
            );
        }
    }

    //建立廣播接收socket訊息
    public class disconnNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();

            if (action.equals(BTConfig.SOCKET_ADDRESS_MSG)) {
                LogUtil.i("webSocket BaseActivity broadcast recv!");
                brocastMsgHandler.sendMessage(brocastMsgHandler.obtainMessage(BSConfig.HANDLER_SOCKET,
                        intent.getExtras().getSerializable("socketAddrMsg")));
            } else if (action.equals(BTConfig.DISCONN_NOTIFICATION)) {
                if (isShowDisconnAlert) {
                    brocastMsgHandler.sendMessage(brocastMsgHandler.obtainMessage(BSConfig.HANDLER_DISCONN));
                }
            }
        }
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