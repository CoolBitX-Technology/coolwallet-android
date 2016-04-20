package com.coolbitx.coolwallet.Service;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import com.coolbitx.coolwallet.general.BtcUrl;
import com.snscity.egdwlib.utils.LogUtil;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.drafts.Draft_17;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by ShihYi on 2016/3/17.
 */
public class SocketHandler {
    //Server IP address

    private static final int TIMEOUT = 10000;
    BlockWebSocketClient blockWebSocketClient;
    private Set<SocketConnector> taskCollection;
    Context mContext;
    SSLContext SSLContext;

    public SocketHandler(Context context) {
        LogUtil.i("websocket SocketHandler in");
        this.mContext = context;
        try {
            HashMap<String, String> cmap = new HashMap<String, String>();
            taskCollection = new HashSet<SocketConnector>();
            URI uri = new URI(BtcUrl.SOCKET_BLOCK_IO);
            WebSocketImpl.DEBUG = true;

            blockWebSocketClient = new BlockWebSocketClient(mContext, uri, new Draft_17(), cmap, TIMEOUT);

            //This part is needed in case you are going to use self-signed certificates
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[]{};
                }

                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }
            }};

            try {
                SSLContext = SSLContext.getInstance("TLS");
                SSLContext.init(null, trustAllCerts, new java.security.SecureRandom());

                /** for test */
//                SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket("n.block.io", 443);
//                socket.startHandshake();
//                SSLSession session = socket.getSession();
//                LogUtil.i("socket.Info =" + session.getProtocol() + " ; " + session.getCipherSuite());

                if ("wss".equals(uri.getScheme()) && SSLContext != null) {

                    LogUtil.i("sdk 當前=" + Build.VERSION.SDK_INT + " ; " + Build.VERSION_CODES.LOLLIPOP); //5.0
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        blockWebSocketClient.setWebSocketFactory(new org.java_websocket.client.DefaultSSLWebSocketClientFactory(SSLContext));
                    } else {
                        blockWebSocketClient.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(SSLContext));
                    }
                }
                //websocket onOpen
                boolean isConn = blockWebSocketClient.connectBlocking();
                LogUtil.i("websocket isConn=" + isConn);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void Connect() {
        blockWebSocketClient.connect();
    }

    public void SendMessage(String msg) {

        SocketConnector task = new SocketConnector();
        task.sendString = msg;
        taskCollection.add(task);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, blockWebSocketClient);
    }

    public void cancelALLTasks() {
        if (taskCollection != null) {
            for (SocketConnector task : taskCollection) {
                task.cancel(false);
            }
        }
        try {
            if (blockWebSocketClient != null)
                blockWebSocketClient.close();
        } catch (Exception e) {

        }
    }

    private class SocketConnector extends AsyncTask<BlockWebSocketClient, Void, Boolean> {
        private String sendString;

        @Override
        protected Boolean doInBackground(BlockWebSocketClient... params) {
            try {
                LogUtil.i("websocket doInBackground:semd msg=" + sendString);

                params[0].send(sendString);

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            super.onPostExecute(isSuccess);
            if (!isSuccess) {

            }
            taskCollection.remove(this);
        }
    }
}
