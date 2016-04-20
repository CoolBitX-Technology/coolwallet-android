package com.coolbitx.coolwallet.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import com.coolbitx.coolwallet.DataBase.DatabaseHelper;
import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.Service.BTConfig;
import com.coolbitx.coolwallet.callback.RefreshCallback;
import com.coolbitx.coolwallet.entity.dbAddress;
import com.coolbitx.coolwallet.entity.socketByAddress;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.general.RefreshBlockChainInfo;
import com.coolbitx.coolwallet.ui.Fragment.BSConfig;
import com.coolbitx.coolwallet.ui.Fragment.TabFragment;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.ByteUtil;
import com.snscity.egdwlib.utils.LogUtil;

import java.util.ArrayList;

/**
 * Created by ShihYi on 2016/4/8.
 * 所有activity需要做的事
 * for disconn 廣播
 */
public class BaseActivity extends AppCompatActivity {
    Context mContext;
    public static disconnNotificationReceiver brocastNR = null;
    public static CmdManager cmdManager;
    public  boolean isShowDisconnAlert = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
    }

    @Override
    protected void onResume() {
        super.onResume();

        //註冊監聽
        if (brocastNR == null) {
            brocastNR = new disconnNotificationReceiver();
            //註冊廣播
            registerReceiver(brocastNR, new IntentFilter(BTConfig.SOCKET_ADDRESS_MSG));
            registerReceiver(brocastNR, new IntentFilter(BTConfig.DISCONN_NOTIFICATION));
        }
    }

    @Override
    protected void onPause() {
        if (brocastNR != null) {
            unregisterReceiver(brocastNR);
            brocastNR = null;
        }
        super.onPause();
    }

    //建立廣播接收socket訊息
    public class disconnNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            LogUtil.i("baseActivity broadcast recv!");
            if (action.equals(BTConfig.SOCKET_ADDRESS_MSG)) {
                brocastMsgHandler.sendMessage(brocastMsgHandler.obtainMessage(BSConfig.HANDLER_SOCKET,
                        intent.getExtras().getSerializable("socketAddrMsg")));
            } else if (action.equals(BTConfig.DISCONN_NOTIFICATION)) {
                if(isShowDisconnAlert) {
                    brocastMsgHandler.sendMessage(brocastMsgHandler.obtainMessage(BSConfig.HANDLER_DISCONN));
                }
            }
        }
    }

    private Handler brocastMsgHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub

            switch (msg.what) {
                case BSConfig.HANDLER_SOCKET:
                    LogUtil.i("BaseActivity HANDLER_SOCKET");
                    socketByAddress socket = (socketByAddress) msg.obj;
                    final int mAccount = DatabaseHelper.queryAccountByAddress(mContext, socket.getAddress());
                    if (socket.getTx_type().equals("Received")) {
                        PublicPun.ClickFunction(mContext, "Bitcoin Received",
                                "Account " + (mAccount + 1) + "\n" //卡片從0開始;page從1開始
                                        + "Address:" + "\n"
                                        + socket.getAddress() + "\n"
                                        + socket.getTx_type() + " Amount:" + TabFragment.BtcFormatter.format(socket.getBtc_amount()) + " BTC" + "\n"
                                        + "Confirmations: " + socket.getConfirmations());
                    }
                    if (mAccount >= 0) {
                        RefreshBlockChainInfo refreshBlockChainInfo = new RefreshBlockChainInfo(mContext, mAccount);
                        refreshBlockChainInfo.callTxsRunnable(new RefreshCallback() {
                            @Override
                            public void success() {
                                RefreshSetAccInfo(mAccount);
                            }

                            @Override
                            public void fail(String msg) {
                                PublicPun.ClickFunction(mContext, "Unstable internet connection", msg);
                            }

                            @Override
                            public void exception(String msg) {

                            }
                        });
                    }
                    break;
                case BSConfig.HANDLER_DISCONN:
                    LogUtil.i("BaseActivity HANDLER_DISCONN");
                    PublicPun.ClickFunctionToFinish(mContext, "Notification", PublicPun.card.getCardName() + " Disconnected");
                    systemNotification();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     * show on Status Bar
     */
    private void systemNotification() {
        final int notifyID = 1; // 通知的識別號碼
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE); // 取得系統的通知服務
        final Notification notification = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("CoolWallet Disconnected")
                .setContentText(PublicPun.card.getCardName() + " Disconnected").build(); // 建立通知
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
        int TotalBalance = 0;
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
                    byte[] newBalanceBytes = ByteUtil.intToByteLittle(TotalBalance, 8);
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
}