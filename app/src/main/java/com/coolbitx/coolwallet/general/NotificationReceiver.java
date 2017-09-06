package com.coolbitx.coolwallet.general;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.coolbitx.coolwallet.DataBase.DatabaseHelper;
import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.Service.BTConfig;
import com.coolbitx.coolwallet.bean.dbAddress;
import com.coolbitx.coolwallet.bean.socketByAddress;
import com.coolbitx.coolwallet.callback.RefreshCallback;
import com.coolbitx.coolwallet.ui.BleActivity;
import com.coolbitx.coolwallet.ui.Fragment.BSConfig;
import com.coolbitx.coolwallet.ui.Fragment.TabFragment;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.ByteUtil;
import com.snscity.egdwlib.utils.LogUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class NotificationReceiver extends BroadcastReceiver {

    private Context mContext;
    private CmdManager mCmdManager;


    public NotificationReceiver(Context context, CmdManager cmdManager) {
        this.mContext = context;
        this.mCmdManager = cmdManager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        String action = intent.getAction();

        LogUtil.e("我聽到了喔:" + action);
        if (action.equals(BTConfig.SOCKET_ADDRESS_MSG)) {

            brocastMsgHandler.sendMessage(brocastMsgHandler.obtainMessage(BSConfig.HANDLER_SOCKET,
                    intent.getExtras().getSerializable("socketAddrMsg")));

        } else if (action.equals(BTConfig.DISCONN_NOTIFICATION)) {

            brocastMsgHandler.sendMessage(brocastMsgHandler.obtainMessage(BSConfig.HANDLER_DISCONN));

        } else if (action.equals(BTConfig.XCHS_NOTIFICATION)) {

            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("handlerMessage", intent.getExtras().getString("ExchangeMessage"));
            msg.setData(data);
            msg.what = BSConfig.HANDLER_XCHS;
            brocastMsgHandler.sendMessage(msg);
        }

    }

    private Handler brocastMsgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case BSConfig.HANDLER_SOCKET:
                    final socketByAddress socket = (socketByAddress) msg.obj;
                    final int mAccount = DatabaseHelper.queryAccountByAddress(mContext, socket.getAddress());
                    if (socket.getTx_type().equals("Received") && socket.getConfirmations() <= 1) {//
                        // do your work right here
                        String socketTitle = "BitCoin Received";
                        String socketMsg = "Account " + (mAccount + 1) + "\n"
                                + "Address:" + "\n"
                                + socket.getAddress() + "\n"
                                + socket.getTx_type() + " Amount:" + TabFragment.BtcFormatter.format(socket.getBtc_amount()) + " BTC" + "\n"
                                + "Confirmations: " + socket.getConfirmations();
                        PublicPun.showNoticeDialog(mContext, socketTitle, socketMsg);
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

                    ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                    ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                    int ind = cn.getShortClassName().lastIndexOf(".") + 1;//.ui.EraseActivity → EraseActivity
                    String act = cn.getShortClassName().substring(ind);

                    LogUtil.e("HANDLER_DISCONN activity=" + act);

                    if (!act.equals("BleActivity")) {

                        Intent intent = new Intent(mContext, BleActivity.class);
                        intent.putExtra("Disconnection_Notify", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mContext.startActivity(intent);
//                        PublicPun.showNoticeDialogToFinish(mContext, title, noteMsg);
                        //自動連線
//                        SharedPreferences settings = mContext.getSharedPreferences("Preference", 0);
//                        //取出name屬性的字串
//                        String address = settings.getString("connAddress", "");
//                        bleManager.connectBle(address);
                    }

                    break;

                case BSConfig.HANDLER_XCHS:
                    String message = msg.getData().getString("handlerMessage");
                    LogUtil.e("XCHS 廣播:" + message);
                    if (message != null) {
                        //Receive message from Exchange site
                        LogUtil.d("ExchangeMessage=" + message);

                        PublicPun.showNoticeDialog(mContext, "CoolWallet Exchange Message", message);
                    }

            }
            super.handleMessage(msg);
        }
    };


    public void RefreshSetAccInfo(int account) {

        LogUtil.e("RefreshSetAccInfo account:" + account);
        byte ByteAccId = (byte) account;

        //for card display

        mCmdManager.McuSetAccountState(ByteAccId, new CmdResultCallback() {
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
            LogUtil.d("hdwSetAccInfo:" + account + "的 " + setAcctInfoIndex + " =" + PublicPun.byte2HexString(accountInfo));
            mCmdManager.hdwSetAccInfo(PublicPun.user.getMacKey(), cwHdwAccountInfo[setAcctInfoIndex], account, accountInfo, new CmdResultCallback() {
                        @Override
                        public void onSuccess(int status, byte[] outputData) {
                            if ((status + 65536) == 0x9000) {


                            } else {
//                                PublicPun.toast(mContext, "Data synced failed!");
                            }
                        }
                    }
            );
        }
    }

    /**
     * show on Status Bar
     * foe btc recv
     */
    private void systemNotificationBTC(socketByAddress socket, int mAccount) {
        final int notifyID = mAccount; // 通知的識別號碼

        String socketTitle = "BitCoin Received";
        final NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE); // 取得系統的通知服務
        final Notification notification = new Notification.Builder(mContext)
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
}
