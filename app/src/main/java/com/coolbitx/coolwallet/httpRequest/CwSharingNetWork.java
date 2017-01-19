package com.coolbitx.coolwallet.httpRequest;

import com.snscity.egdwlib.utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by ShihYi on 2016/12/9.
 */
public class CwSharingNetWork {
    private int httpTimeOut = 30000;
    private int httpReadTimeout = 10000;
    private String CWSToken= "AejmNc7vNr2brhKfR3E3uYGyr7W9Nahf4RXZAK7ypcZ";

    public JSONObject makeHttpPut(String parUrl, String requestBody) {
        String response = "";
        HttpURLConnection conn = null;

        try {
            URL url = new URL(parUrl);
            //URL url = new URL("http", "60.250.111.124", 80, "/iBadgeTrial/api/iBadgeService_Device.php");
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("X-CWS-Token", this.CWSToken);
            conn.setRequestMethod("PUT");

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

//            String dataLenStr = Integer.toString(requestBody.getBytes().length);
//            conn.setRequestProperty("Content-Length", dataLenStr);
//            conn.setFixedLengthStreamingMode(requestBody.getBytes().length);
            conn.setReadTimeout(this.httpReadTimeout);
            conn.setConnectTimeout(this.httpTimeOut);

            OutputStream os = conn.getOutputStream();
            os.write(requestBody.getBytes());
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                String errString = "";
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                errString+="{";
                while ((line = br.readLine()) != null) {
                    errString += line;
                }
                errString+="}";
                response = errString;
            }
            LogUtil.d("address sharing code = " + responseCode + ":" + response);

        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("makeHttpPost Error: " + e.toString());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return  parsingToJson(response);
    }

    public JSONObject makeHttpPost(String parUrl, String data) {
        String response = "";
        HttpURLConnection conn = null;

        try {
            URL url = new URL(parUrl);
            //URL url = new URL("http", "60.250.111.124", 80, "/iBadgeTrial/api/iBadgeService_Device.php");
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("X-CWS-Token", this.CWSToken);
            conn.setRequestMethod("POST");

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            String dataLenStr = Integer.toString(data.getBytes().length);
            conn.setRequestProperty("Content-Length", dataLenStr);
            conn.setFixedLengthStreamingMode(data.getBytes().length);
            conn.setReadTimeout(this.httpReadTimeout);
            conn.setConnectTimeout(this.httpTimeOut);

            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                String errString = "";
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                while ((line = br.readLine()) != null) {
                    errString += line;
                }
                response = errString;
            }
            LogUtil.d("address sharing code = " + responseCode + ":" + response);

        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("makeHttpPost Error: " + e.toString());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return  parsingToJson(response);
    }


    public JSONObject makeHttpGet(String parUrl, String data) {
        String response = "";
        HttpURLConnection conn = null;
        try {
            URL url = new URL(parUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("X-CWS-Token", this.CWSToken);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(false);
            conn.setReadTimeout(this.httpReadTimeout);
            conn.setConnectTimeout(this.httpTimeOut);

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                String errString = "";
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                while ((line = br.readLine()) != null) {
                    errString += line;
                }
                response = errString;
            }
            LogUtil.d("address sharing code = " + responseCode + ":" + response);

        } catch (Exception e) {
            LogUtil.e("makeHttpGet Error: " + e.toString());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return parsingToJson(response);
    }

    private JSONObject parsingToJson(String parsingStr){
        // try parse the string to a JSON object
        JSONObject jObj = null;
        try {
            jObj = new JSONObject(parsingStr);
        } catch (JSONException e) {
            LogUtil.d("JSON Parser getJSONFromUrl Error parsing data " + e.toString());
        }
        // return JSON String
        return jObj;
    }

}

