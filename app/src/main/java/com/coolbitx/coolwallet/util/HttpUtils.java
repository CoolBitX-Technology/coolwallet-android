package com.coolbitx.coolwallet.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import com.coolbitx.coolwallet.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class HttpUtils {

    public static final String TAG = "HttpUtils";

    public static boolean isNetworkAvailable(Context context) {

        boolean isConnect = false;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo == null) {
            return false;
        }

        String type = networkInfo.getTypeName();
        String state = networkInfo.getState().name();
        boolean isAvailable = networkInfo.isAvailable();
        boolean isConnected = networkInfo.isConnected();
        boolean isConnectedOrConnecting = networkInfo.isConnectedOrConnecting();
        boolean isFailover = networkInfo.isFailover();
        boolean isRoaming = networkInfo.isRoaming();

        Log.w(TAG, "type: " + type); // 目前以何種方式連線
        Log.w(TAG, "state: " + state); // 目前連線狀態
        Log.w(TAG, "isAvailable: " + isAvailable); // 目前網路是否可使用
        Log.w(TAG, "isConnected: " + isConnected); // 網路是否已連接
        Log.w(TAG, "isConnectedOrConnecting: " + isConnectedOrConnecting); // 網路是否已連接或連線中
        Log.w(TAG, "isFailover: " + isFailover); // 網路目前是否有問題
        Log.w(TAG, "isRoaming: " + isRoaming); // 網路目前是否在漫遊中

        if(isConnected) {
            isConnect = true;
        }

        return isConnect;
    }

    // Restful  - 產生出JSON字串
    public static String getStringFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream os = null;
        String content = null;
        try {
            os = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            content = os.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        }
        return content;
    }


}
