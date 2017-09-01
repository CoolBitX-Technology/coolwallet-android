package com.coolbitx.coolwallet.httpRequest;

import android.text.TextUtils;
import com.snscity.egdwlib.utils.LogUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class XchsNetWork {

    public static final String COOKIES_HEADER = "Set-Cookie";
    public static java.net.CookieManager mCookieManager = new java.net.CookieManager();

    public XchsNetWork() {
    }


    public JSONObject doGetRawAddress( String mUrl) {
        String response = "";
        HttpURLConnection conn = null;
        try {
            URL url = new URL(mUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(false);
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(30000);
            int responseCode = conn.getResponseCode();

            LogUtil.d("doGetRawAddress responseCode=" + String.valueOf(responseCode));

            if (responseCode == HttpsURLConnection.HTTP_OK||responseCode==HttpsURLConnection.HTTP_ACCEPTED||responseCode==HttpsURLConnection.HTTP_CREATED) {

                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                response = "{\"response\":" + conn.getResponseMessage() + "}";
                LogUtil.d("makeHttpRequestGet JSON Parser errString: " + response);
            }
        } catch (Exception e) {
            LogUtil.d("getSrvInitSession JSON Parser makeHttpRequest Error: " + e.toString());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        // try parse the string to a JSON object
        JSONObject jObj = null;
        try {
            jObj = new JSONObject(response);
        } catch (JSONException e) {
            LogUtil.d("JSON Parser getJSONFromUrl Error parsing data " + e.toString());
        }
        // return JSON String
        return jObj;
    }


    public JSONObject makeHttpRequestInit(String temp_url, String data) {
        String response = "";
        HttpURLConnection conn = null;
        try {
            URL url = new URL(temp_url);
            LogUtil.d("makeHttpRequestInit url=" + temp_url);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(false);
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(30000);
            int responseCode = conn.getResponseCode();

            LogUtil.d("getSrvInitSession responseCode=" + String.valueOf(responseCode));

            if (responseCode == HttpsURLConnection.HTTP_OK||responseCode==HttpsURLConnection.HTTP_ACCEPTED||responseCode==HttpsURLConnection.HTTP_CREATED) {


                Map<String, List<String>> headerFields = conn.getHeaderFields();
                List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

                LogUtil.d("getSrvInitSession cookiesHeader=" + cookiesHeader);

                if (cookiesHeader != null) {
                    for (String cookie : cookiesHeader) {
                        LogUtil.i("Login cookie :" + cookie);
                        mCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                    }
                }

                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
//                String errString = "";
//                String line;
//                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
//                while ((line = br.readLine()) != null) {
//                    errString += line;
//                }
                response = "{\"response\":" + conn.getResponseMessage() + "}";
                LogUtil.d("makeHttpRequestGet JSON Parser errString: " + response);
            }
        } catch (Exception e) {
            LogUtil.d("getSrvInitSession JSON Parser makeHttpRequest Error: " + e.toString());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        // try parse the string to a JSON object
        JSONObject jObj = null;
        try {
            jObj = new JSONObject(response);
        } catch (JSONException e) {
            LogUtil.d("JSON Parser getJSONFromUrl Error parsing data " + e.toString());
        }
        // return JSON String
        return jObj;
    }

    public JSONObject makeHttpRequestPost(String temp_url, String data) {
        String response = "";
        HttpURLConnection conn = null;
        try {
            URL url = new URL(temp_url);
            LogUtil.d("makeHttpRequestPost url=" + temp_url);
            conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(false);

            String dataLenStr = Integer.toString(data.getBytes().length);
            conn.setRequestProperty("Content-Length", dataLenStr);
            conn.setFixedLengthStreamingMode(data.getBytes().length);
            LogUtil.d("makeHttpRequestPost data=" + data);

            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);

            LogUtil.i("makeHttpRequestPost-----cookie: " + mCookieManager.getCookieStore().getCookies());
            conn.setRequestProperty("Cookie",
                    TextUtils.join(";", mCookieManager.getCookieStore().getCookies()));

            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            LogUtil.d("makeHttpRequestPost responseCode= " + responseCode);
            if (responseCode == HttpsURLConnection.HTTP_OK||responseCode==HttpsURLConnection.HTTP_ACCEPTED||responseCode==HttpsURLConnection.HTTP_CREATED) {

                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                if ((line = br.readLine()) != null) {
                    response += line;
                } else {
                    response = "{\"response\":\"" + "ok" + "\"}";
                }
            } else {
//                String errString = "";
//                String line;
//                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
//                while ((line = br.readLine()) != null) {
//                    errString += line;
//                }
                response = "{\"response\":" + conn.getResponseMessage() + "}";
                LogUtil.d("makeHttpRequestGet JSON Parser errString: " + response);
            }
            LogUtil.d("makeHttpRequestPost: " + response);
        } catch (Exception e) {
            LogUtil.d("JSON Parser makeHttpRequest Error: " + e.toString());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        // try parse the string to a JSON object
        JSONObject jObj = null;
        try {
            jObj = new JSONObject(response);
        } catch (JSONException e) {
            LogUtil.d("JSON Parser getJSONFromUrl Error parsing data " + e.toString());
        }
        // return JSON String
        return jObj;
    }

    public JSONObject makeHttpRequestLogout(String temp_url) {
        String response = "";
        HttpURLConnection conn = null;
        try {
            URL url = new URL(temp_url);
            LogUtil.d("makeHttpRequestLogout url=" + temp_url);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(false);
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(30000);
            LogUtil.i("makeHttpRequestGet-----cookie: " + mCookieManager.getCookieStore().getCookies());
            conn.setRequestProperty("Cookie",
                    TextUtils.join(";", mCookieManager.getCookieStore().getCookies()));

            int responseCode = conn.getResponseCode();

            LogUtil.d("makeHttpRequestGet responseCode=" + String.valueOf(responseCode));

            if (responseCode == HttpsURLConnection.HTTP_OK||responseCode==HttpsURLConnection.HTTP_ACCEPTED||responseCode==HttpsURLConnection.HTTP_CREATED) {


                Map<String, List<String>> headerFields = conn.getHeaderFields();
                List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

                LogUtil.d("makeHttpRequestGet cookiesHeader=" + cookiesHeader);

                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
                if (!jsonObjectType(response)) {
                    LogUtil.d("jSon object轉jSon array");
                    //return jSon array
                    response = "{\"response\":success}";
                }

            } else {
                response = "{\"response\":" + conn.getResponseMessage() + "}";
            }
        } catch (Exception e) {
            LogUtil.d("makeHttpRequestGet JSON Parser makeHttpRequest Error: " + e.toString());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        // try parse the string to a JSON object
        JSONObject jObj = null;
        try {
            jObj = new JSONObject(response);
        } catch (JSONException e) {
            LogUtil.d("makeHttpRequestGet JSON Parser getJSONFromUrl Error parsing data " + e.toString());
        }
        // return JSON String
        return jObj;
    }

    public JSONObject makeHttpRequestGet(String temp_url, String data) {
        String response = "";
        HttpURLConnection conn = null;
        try {
            URL url = new URL(temp_url);
            LogUtil.d("makeHttpRequestGet url=" + temp_url);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(false);
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(30000);
            LogUtil.i("makeHttpRequestGet-----cookie: " + mCookieManager.getCookieStore().getCookies());
            conn.setRequestProperty("Cookie",
                    TextUtils.join(";", mCookieManager.getCookieStore().getCookies()));

            int responseCode = conn.getResponseCode();

            LogUtil.d("makeHttpRequestGet responseCode=" + String.valueOf(responseCode));

            if (responseCode == HttpsURLConnection.HTTP_OK||responseCode==HttpsURLConnection.HTTP_ACCEPTED||responseCode==HttpsURLConnection.HTTP_CREATED) {


                Map<String, List<String>> headerFields = conn.getHeaderFields();
                List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

                LogUtil.d("makeHttpRequestGet cookiesHeader=" + cookiesHeader);

                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
                if (!jsonObjectType(response)) {
                    LogUtil.d("jSon object轉jSon array");
                    //return jSon array
                    response = "{\"response\":" + response + "}";
                }

            } else {
//                String errString = "";
//                String line;
//                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
//                while ((line = br.readLine()) != null) {
//                    errString += line;
//                }

                response = "{\"response\":" + conn.getResponseMessage() + "}";

                LogUtil.d("makeHttpRequestGet JSON Parser errString: " + conn.getResponseMessage());
            }
        } catch (Exception e) {
            LogUtil.d("makeHttpRequestGet JSON Parser makeHttpRequest Error: " + e.toString());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        // try parse the string to a JSON object
        JSONObject jObj = null;
        try {
            jObj = new JSONObject(response);
        } catch (JSONException e) {
            LogUtil.d("makeHttpRequestGet JSON Parser getJSONFromUrl Error parsing data " + e.toString());
        }
        // return JSON String
        return jObj;
    }

    public JSONObject makeHttpDelete(String temp_url) {
        String response = "";
        HttpURLConnection conn = null;
        try {
            URL url = new URL(temp_url);
            LogUtil.d("makeHttpDelete url=" + temp_url);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setDoInput(true);
            conn.setDoOutput(false);
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(30000);
            LogUtil.i("makeHttpDelete-----cookie: " + mCookieManager.getCookieStore().getCookies());
            conn.setRequestProperty("Cookie",
                    TextUtils.join(";", mCookieManager.getCookieStore().getCookies()));

            int responseCode = conn.getResponseCode();

            LogUtil.d("makeHttpDelete responseCode=" + String.valueOf(responseCode));

            if (responseCode == HttpsURLConnection.HTTP_OK||responseCode==HttpsURLConnection.HTTP_ACCEPTED||responseCode==HttpsURLConnection.HTTP_CREATED) {

                Map<String, List<String>> headerFields = conn.getHeaderFields();
                List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

                LogUtil.d("makeHttpDelete cookiesHeader=" + cookiesHeader);

                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                if ((line = br.readLine()) != null) {
                    response += line;
                } else {
                    response = "{\"status\":\"" + "ok" + "\"}";
                }
                if (!jsonObjectType(response)) {
                    LogUtil.d("jSon object轉jSon array");
                    //return jSon array
                    response = "{\"response\":" + response + "}";
                }

            } else {
//                String errString = "";
//                String line;
//                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
//                while ((line = br.readLine()) != null) {
//                    errString += line;
//                }
                response = "{\"response\":" + conn.getResponseMessage() + "}";
                LogUtil.d("makeHttpRequestGet JSON Parser errString: " + response);
            }
        } catch (Exception e) {
            LogUtil.d("makeHttpRequestGet JSON Parser makeHttpRequest Error: " + e.toString());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        // try parse the string to a JSON object
        JSONObject jObj = null;
        try {
            jObj = new JSONObject(response);
        } catch (JSONException e) {
            LogUtil.d("makeHttpRequestGet JSON Parser getJSONFromUrl Error parsing data " + e.toString());
        }
        // return JSON String
        return jObj;
    }


    private boolean jsonObjectType(String jsonStr) {
        boolean mJsonObj = false;
        String data = jsonStr;
        try {
            Object json = new JSONTokener(data).nextValue();
            if (json instanceof JSONObject) {
                //是object類型
                mJsonObj = true;
            } else if (json instanceof JSONArray) {
                //是array類型
                mJsonObj = false;
            }
        } catch (JSONException e) {
            LogUtil.d("jsonObjectType exception = " + e.toString());
        }
        return mJsonObj;
    }
}
