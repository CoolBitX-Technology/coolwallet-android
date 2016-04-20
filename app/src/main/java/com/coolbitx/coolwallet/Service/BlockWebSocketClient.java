package com.coolbitx.coolwallet.Service;

import android.content.Context;
import android.content.Intent;

import com.coolbitx.coolwallet.entity.socketByAddress;
import com.coolbitx.coolwallet.general.PublicPun;
import com.snscity.egdwlib.utils.LogUtil;

import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;

/**
 * Created by ShihYi on 2016/3/15.
 */
public class BlockWebSocketClient extends org.java_websocket.client.WebSocketClient  {

    Context mContext;

    public BlockWebSocketClient(URI serverUri, Draft draft, HashMap<String, String> headers, int timeout) {
        super(serverUri, draft, headers, timeout);
//        LogUtil.i("websocket create");
    }

    public BlockWebSocketClient(Context context, URI serverUri, Draft draft, HashMap<String, String> headers, int timeout) {
        super(serverUri, draft, headers, timeout);
        LogUtil.i("websocket create");
        this.mContext = context;
    }

    public BlockWebSocketClient(Context context,URI serverUri, Draft draft) {
        super(serverUri,draft);
        LogUtil.i("TEST websocket create");
        this.mContext = context;
    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {

        LogUtil.i("websocket open You are connected to BLOCK_IO Server: " + getURI() + "\n");
    }

    /**
     * 接收信息
     *
     * @param message
     */
    @Override
    public void onMessage(String message) {
        final String msg = message;
        String mResult = null;
        //Handle this message
        //json pasing
        LogUtil.i("websocket msg="+msg);
//        if (msg != null) {
//            //準備資料
//            String type = "address";
//            String network = "BTC";
//            String address = "146yrUyxskvjg2ePwyXeg5hBS9soNKAexn";
//            String balance_change = "0.00010000";
//            String amount_sent = "0.00000000";
//            String amount_received = "0.00010000";
//            String txid = "9adaa08c9405ed55f3460852f7cadcd59323eebae4927ad139dae7647bd8bb15";
//            int confirmations = 1;
//            boolean is_green = false;
//
//            //開始拼接字串
//            StringBuilder sb = new StringBuilder();
//            sb.append("{");
//            sb.append("\"type\":\"" + type + "\",");
//            sb.append("\"data\":{");
//            sb.append("\"network\":\"" + network + "\",");
//            sb.append("\"address\":\"" + address + "\",");
//            sb.append("\"balance_change\":\"" + balance_change + "\",");
//            sb.append("\"amount_sent\":\"" + amount_sent + "\",");
//            sb.append("\"amount_received\":\"" + amount_received + "\",");
//            sb.append("\"txid\":\"" + txid + "\",");
//            sb.append("\"confirmations\":" + confirmations+ ",");
//            sb.append("\"is_green\":" + is_green);
//            sb.append("}");
//            sb.append("}");
//            mResult = sb.toString();
//        }
        socketByAddress socketAddress=null;
        if(msg.contains("address")){
            socketAddress = PublicPun.jsonParserSocketAddress(msg);
        }

        if (socketAddress != null ) {
            //都要廣播 for refresh data,but show only received msg.
//            if(socketAddress.getTx_type().equals("Received")) {
                broadCast(socketAddress);
//            }
        }
    }



    @Override
    public void onClose(int code, String reason, boolean remote) {
        LogUtil.i("websocket closed");
    }

    @Override
    public void onError(Exception ex) {
        LogUtil.i("websocket ERROR=" + ex.getMessage());
        ex.printStackTrace();
    }




    /**
     * 發送廣播訊息
     *
     * @param socketAddress
     */
    private void broadCast(socketByAddress socketAddress) {
        Intent SocketIntent = new Intent(BTConfig.SOCKET_ADDRESS_MSG);
        LogUtil.i("websocket broadcast=" + socketAddress.getAddress());
        SocketIntent.putExtra("socketAddrMsg", socketAddress);
        mContext.sendBroadcast(SocketIntent);
    }
}
