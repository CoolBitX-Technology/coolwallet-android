package com.coolbitx.coolwallet.Service;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.snscity.egdwlib.utils.LogUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by ShihYi on 2016/3/17.
 */
public class BlockSocketHandler {
    //Server IP address

    private static final int TIMEOUT = 10000;
    BlockWebSocketClient blockWebSocketClient;
    Context mContext;
    private PrintWriter out = null;
    private BufferedReader in = null;

    public BlockSocketHandler(Context context, String output) {
        LogUtil.i("websocket SocketHandler_temp in");
        this.mContext = context;

        SocketConnector socketConnector = new SocketConnector();
        socketConnector.sendString = output;
        socketConnector.execute();
    }

    public BlockSocketHandler() {
        LogUtil.i("websocket SocketHandler_temp in");
        SocketConnector socketConnector = new SocketConnector();
//        socketConnector.sendString = output;
        socketConnector.execute();
    }


    private class SocketConnector extends AsyncTask<Void, Void, Void> {
        private String sendString;

        @Override
        protected Void doInBackground(Void... voids) {
//                PersistentCookieStore cookieStore = SingletonPersistentCookieStore.getInstance(main);
//                final Cookie cookie = cookieStore.getCookies().get(0);
            try {
                LogUtil.i("websocket doInBackground");
                HashMap<String, String> cmap = new HashMap<String, String>();
//                    String cookieString = cookie.getName()+"="+cookie.getValue();
//                    cmap.put("cookie", cookieString);

//                URI uri = new URI(BtcUrl.SOCKET_BLOCK_IO);
//                blockWebSocketClient = new BlockWebSocketClient(mContext,uri, new Draft_17(), cmap, TIMEOUT);
                String SERVERIP = "wss://n.block.io";
                int SERVERPORT = 443;
                try {
                    InetAddress serverAddr = InetAddress.getByName(SERVERIP);
                    Log.e("TCP SI Client", "SI: Connecting...");
                    Socket socket = new Socket(serverAddr, SERVERPORT);


                    //send the message to the server
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                    Log.e("TCP SI Client", "SI: Sent.");

                    Log.e("TCP SI Client", "SI: Done.");

                    //receive the message which the server sends back
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                } catch (Exception e) {
                    LogUtil.i("websocket doInBackground error=" + e.getMessage());
                }
//                blockWebSocketClient = new BlockWebSocketClient(mContext,uri, new Draft_17());
                //This part is needed in case you are going to use self-signed certificates
//                TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
//                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//                        return new java.security.cert.X509Certificate[]{};
//                    }
//                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//                    }
//                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//                    }
//                }};
//
//                try {
//                    SSLContext sc = SSLContext.getInstance("TLS");
////                    sc.init(null, trustAllCerts, new java.security.SecureRandom());
////                    sc.getDefault();
//                    //Otherwise the line below is all that is needed.
//                    sc.init(null, null, null);
//                    blockWebSocketClient.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(sc));
//                } catch (Exception e) {
//                    LogUtil.i("websocket doInBackground error=" + e.getMessage());
//                    e.printStackTrace();
//                }
//                blockWebSocketClient.connectBlocking();
//
            } catch (Exception e) {
                LogUtil.i("websocket error=" + e.getMessage());
                e.printStackTrace();
            }finally{
//                blockWebSocketClient.close();
            }
                return null;
            }
        }

//    public boolean isSocketConnect() throws InterruptedException {
//        return  blockWebSocketClient.connectBlocking();
//    }
//
//    public void sendSocketMsg(String sendString){
//        blockWebSocketClient.send(sendString);
//    }
    }
