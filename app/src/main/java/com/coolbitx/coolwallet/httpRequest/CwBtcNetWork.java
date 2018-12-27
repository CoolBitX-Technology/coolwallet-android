package com.coolbitx.coolwallet.httpRequest;

import android.content.ContentValues;

import com.coolbitx.coolwallet.general.BtcUrl;
import com.crashlytics.android.Crashlytics;
import com.snscity.egdwlib.utils.LogUtil;

import java.io.BufferedReader;
import java.io.DataOutputStream;
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

import static com.coolbitx.coolwallet.general.BtcUrl.URL_BLOCKCHAIN_UNSPENT;

/**
 * Created by wmgs_01 on 15/9/25.
 */
public class CwBtcNetWork {

    private int httpTimeOut = 60000;

    public String doGet(ContentValues cv, String extraUrl, String option) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        String resultString = "";
        String url = "";
        String addr = cv.getAsString("addresses");
        if (addr == null) {
            addr = "";
        }
        try {
            url = extraUrl + addr + option;

            if (extraUrl.equals(BtcUrl.URL_BLICKCHAIN_TXS_MULTIADDR)) {
                if (option == null) {
                    url = BtcUrl.URL_BLOCKCHAIN_SERVER_SITE + extraUrl + cv.getAsString("addresses");
                } else {
                    url = BtcUrl.URL_BLOCKCHAIN_SERVER_SITE + "multiaddr?offset=" + option + "&active=" + cv.getAsString("addresses");
                }
            }

            LogUtil.i("URL地址 = " + url);

            URL getUrl = new URL(url);
            connection = (HttpURLConnection) getUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(this.httpTimeOut);
            connection.setReadTimeout(this.httpTimeOut);
            connection.setDoOutput(false);
            connection.setDoInput(true);
            int code = connection.getResponseCode();
            LogUtil.i("http response code = " + code);

            if (code == HttpURLConnection.HTTP_OK) {
                resultString = jsonResult(connection);
            } else {
                resultString = "{\"errorCode\": " + code + "}";
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

    public int doPost(String Url, String params, boolean isNode) {
        String resultString;
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        int code = -1;
        String urlParameters;
        String rspMsg="";

        if (isNode) {
            urlParameters = "{" + " \"rawtx\":" + "\"" + params + "\"" + "}";
        } else {
            urlParameters = "tx=" + params;
        }
        try {
            String url = Url;
            LogUtil.d("doPost url=" + url + "  params:" + urlParameters);
            Crashlytics.log("Params of sending failed:"+urlParameters);
            URL getUrl = new URL(url);
            connection = (HttpURLConnection) getUrl.openConnection();

            connection.setRequestMethod("POST");
            connection.setConnectTimeout(this.httpTimeOut);
            connection.setReadTimeout(this.httpTimeOut);
            connection.setDoOutput(true);
            connection.setDoInput(true);

            if (isNode) {
                connection.setRequestProperty("Content-Type", "application/json");
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(urlParameters.getBytes());
                outputStream.flush();
                outputStream.close();
            } else {
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.write(urlParameters.getBytes());
                wr.flush();
                wr.close();
            }

            code = connection.getResponseCode();
            rspMsg = connection.getResponseMessage();
            inputStream = connection.getInputStream();
            resultString = readString(inputStream);
            LogUtil.d("do post resultString=" + resultString);

        } catch (Exception e) {
            LogUtil.d("doPost error=" + e.getMessage() + " / code: "+code +" / msg: "+ connection.getResponseMessage());
            Crashlytics.logException(new Throwable("doPost error=" + e.getMessage()
                    +" / code: "+connection.getResponseCode()
                    +" / msg: " + rspMsg));
            e.printStackTrace();
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
