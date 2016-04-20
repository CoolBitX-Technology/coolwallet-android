package com.coolbitx.coolwallet.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.snscity.egdwlib.utils.LogUtil;

/**
 * Created by ShihYi on 2016/3/15.
 */
public class socketService extends Service {
    private Handler handler = new Handler();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
//        handler.postDelayed(socket_server, 1000);
        //建立Thread
        Thread fst = new Thread(socket_server);
        //啟動Thread
        fst.start();
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(socket_server);
        super.onDestroy();
    }

    private Runnable socket_server = new Runnable() {
        public void run() {
            //log目前時間
//            listenBlockChain();

            try {
                String addr="146yrUyxskvjg2ePwyXeg5hBS9soNKAexn";
                //jSonGen(addr)
//                new SocketHandler_temp(FragMainActivity.this,jSonGen(addr));
            } catch (Exception e) {
                LogUtil.i("socket_server error=" + e.getMessage());
            }
//            handler.postDelayed(this, 60000); //
        }
    };

//    private Runnable socket_server = new Runnable(){
//        public void run(){
//            handler.post(new Runnable() {
//                public void run() {
//                    listenBlockChain();
//                    handler.postDelayed(this, 60000); //
//                }
//                });
//            }};


    private String jSonGen(String address) {
        String mResult = null;
        //準備資料
        String strNetwork = "BTC";
        String type = "address";

        //開始拼接字串
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"network\":\"" + strNetwork + "\",");
        sb.append("\"type\":\"" + type + "\",");
        sb.append("\"address\":\"" + address + "\"");
        sb.append("}");
        mResult = sb.toString();

        LogUtil.i("socket send msg="+mResult);

        return mResult;
    }

}
