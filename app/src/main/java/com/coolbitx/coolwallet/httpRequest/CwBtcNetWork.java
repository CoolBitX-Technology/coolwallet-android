package com.coolbitx.coolwallet.httpRequest;

import android.content.ContentValues;

import com.coolbitx.coolwallet.general.BtcUrl;
import com.crashlytics.android.Crashlytics;
import com.snscity.egdwlib.utils.LogUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by wmgs_01 on 15/9/25.
 */
public class CwBtcNetWork {

    private int httpTimeOut = 30000;

//    private byte[] hexStringToBytes(String hexStr) {
//        return null;
//    }
//
//    private String bytesToHexString(byte[] bytes) {
//        return null;
//    }
//
//    private byte[] HTTPRequestUsingGETMethodFrom() {
//        return null;
//    }
//
//    /**
//     * 获取当前汇率
//     *
//     * @return
//     */
//    public double getCurrRate() {
//        return 0;
//    }
//
//    public void getTransactionByAccount(int accountId) {
//
//    }
//
//    public void registerNotifyByAccount(int accountId) {
//
//    }


    public String doGet(ContentValues cv, String extraUrl, String params) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        String resultString = "";
        String url="";
        try {
            if (extraUrl.equals(BtcUrl.URL_BLOCKR_UNSPENT)) {
                url = BtcUrl.URL_BLOCKR_SERVER_SITE + extraUrl + cv.getAsString("addresses");
//                url="http://btc.blockr.io/api/v1/address/unspent/16PGKxt3H96TzKPHSWFDSiqxWPPxm4p6G5";
            } else if (extraUrl.equals(BtcUrl.URL_BLICKCHAIN_EXCHANGE_RATE)) {
                url = BtcUrl.URL_BLICKCHAIN_SERVER_SITE + extraUrl;
            } else if (extraUrl.equals(BtcUrl.RECOMMENDED_TRANSACTION_FEES)) {
                url = extraUrl;
            } else if (extraUrl.equals(BtcUrl.URL_BLICKCHAIN_TXS_MULTIADDR)) {
                if (params == null) {
                    url = BtcUrl.URL_BLICKCHAIN_SERVER_SITE + extraUrl + cv.getAsString("addresses");
                } else {
                    url = BtcUrl.URL_BLICKCHAIN_SERVER_SITE + "multiaddr?offset=" + params + "&active=" + cv.getAsString("addresses");
                }
            } else {
                url = extraUrl;
            }

            LogUtil.i(extraUrl + ":URL地址 = " + url);
            Crashlytics.log("doGet url=" + url);

            URL getUrl = new URL(url);
            connection = (HttpURLConnection) getUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(this.httpTimeOut);
            connection.setReadTimeout(this.httpTimeOut);
            connection.setDoOutput(false);
            connection.setDoInput(true);
            int code = connection.getResponseCode();
            LogUtil.i("URL_BLOCK_CHAIN code = " + code);

            switch (code) {
                case 200:
                    resultString = jsonResult(connection);
                    break;
                case 404:
                    resultString = "{\"errorCode\": 404}";
                    break;
                case 400:
                    resultString = "{\"errorCode\": 400}";
                    break;
                case 500:
                    resultString = "{\"errorCode\": 500}";
                    break;
                default:
                    resultString = "{\"errorCode\":" + code + "}";
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
            }
        }
        return resultString;
    }

    public String jsonResult(HttpURLConnection conn) throws Exception {
        String jsonResult;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
            jsonResult = sb.toString();

            return jsonResult;
        } catch (IOException e) {
            LogUtil.i("jsonResult error=" + e.getMessage());
            throw e;
        }
    }

    public int doPost(String Url, String params) {
        String resultString;
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        int code = -1;
        String urlParameters = "{\"hex\":\"" + params + "\"}";
        LogUtil.i("doPost para=" + urlParameters);
        try {
            String url = Url;
            LogUtil.i("doPost url=" + url);
            URL getUrl = new URL(url);
            connection = (HttpURLConnection) getUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(this.httpTimeOut);
            connection.setReadTimeout(this.httpTimeOut);
            connection.setDoOutput(false);
            connection.setDoInput(true);

            OutputStream out = connection.getOutputStream();// 获得一个输出流,向服务器写数据
            out.write(urlParameters.getBytes());
            out.flush();
            out.close();

            code = connection.getResponseCode();
            LogUtil.i("doPost code:" + code + ";" + connection.getResponseMessage());
            inputStream = connection.getInputStream();
            resultString = readString(inputStream);
            LogUtil.i("do post resultString=" + resultString);
//            inputStream = connection.getInputStream();
//            String resultString = readString(inputStream);
//            LogUtil.i("doPost resultString =" + resultString);
            switch (code) {
                case 200:
                    break;
                case 201:
                    break;
                case 404:
                    resultString = "{\"errorCode\": 404}";
                    break;
                case 500:
                    resultString = "{\"errorCode\": 500}";
                    break;
            }

        } catch (Exception e) {
            LogUtil.i("doPost error=" + e.getMessage() + ";" + connection.getResponseMessage());
            e.printStackTrace();
            Crashlytics.logException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return code;
        }
    }


    /**
     * Author : ShihYi  for call WebAPI
     * Date : 2015/9/9
     * Parsing ContentValues to Http Post format
     */
    private String getQuery(ContentValues params) {

        StringBuilder result = new StringBuilder();
        boolean first = true;
        String encode = "UTF-8";
        Set<String> keys = params.keySet();

        for (String key : keys) {

            if (first)
                first = false;
            else
                result.append("&");

            try {
                result.append(URLEncoder.encode(key, encode));
                result.append("=");
                result.append(URLEncoder.encode(params.getAsString(key), encode));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return result.toString();
    }

    private static String readString(InputStream is) {
        StringBuffer rst = new StringBuffer();
        byte[] buffer = new byte[1048576];
        int var6;

        try {
            LogUtil.i("api length=" + is.read(buffer));
            while ((var6 = is.read(buffer)) > 0) {
                for (int e = 0; e < var6; ++e) {
                    rst.append((char) buffer[e]);
                }
            }
        } catch (IOException var5) {
            var5.printStackTrace();
//            throw  var5;
        }

        return rst.toString();
    }

    interface RequestCallBack {
        void Success(String result);
    }

    class RequestParams {
        private Map<String, String> params = new HashMap<>();

        public void add(String key, String value) {
            params.put(key, value);
        }

        public String getUrl() {
            if (params.isEmpty()) {
                return "";
            }
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                builder.append(key).append("=").append(value).append("&");
            }
            builder.deleteCharAt(builder.length() - 1);
            return builder.toString();
        }
    }
}
