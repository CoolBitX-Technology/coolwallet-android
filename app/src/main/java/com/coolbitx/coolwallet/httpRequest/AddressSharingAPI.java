package com.coolbitx.coolwallet.httpRequest;

import android.content.Context;
import android.os.AsyncTask;

import com.coolbitx.coolwallet.callback.APIResultCallback;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ShihYi on 2016/6/7.
 */
public class AddressSharingAPI {

    APIResultCallback apiResultCallback;
    String[] mResponse;
    CmdManager cmdManager;
    Context mContext;
    private static String ADDRESS_SHARING_DOMAIN_URL = "https://cws.coolbitx.com/res";
    private static String URL_SHARING = "/sharing";

    public AddressSharingAPI(Context context, CmdManager cmdManager) {
        this.cmdManager = cmdManager;
        this.mContext = context;
    }

    public AddressSharingAPI(Context context) {
        this.mContext = context;
    }

    public void StartSharing(final String cwid, final APIResultCallback apiResultCallback) {
        this.apiResultCallback = apiResultCallback;
        this.mResponse = new String[1];//challenge
        final String failedlMsg = "API [Sharing] failed:";

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"cwid\":\"" + cwid + "\"");
        sb.append("}");
        String httpData = sb.toString();
        LogUtil.d("httpData=" + httpData);

        new AsyncTask<String, Integer, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... param) {
                String url = ADDRESS_SHARING_DOMAIN_URL + URL_SHARING;
                CwSharingNetWork cwSharingNW = new CwSharingNetWork();
                return cwSharingNW.makeHttpPut(url, param[0]);
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    try {
                        String hostid = result.getString("hostid");
                        mResponse[0] = hostid;
                        apiResultCallback.success(mResponse);

                    } catch (JSONException e) {
                        apiResultCallback.fail(failedlMsg + e.toString());
                    }
                } else {
                    apiResultCallback.fail(failedlMsg + " No response");
                }
            }
        }.execute(httpData);
    }

    public void GenRspForChallenge(final String subUrl, final APIResultCallback apiResultCallback) {
        this.apiResultCallback = apiResultCallback;
        this.mResponse = new String[1];//loginInfo
        final String failedlMsg = "API [Response] failed:";
        String httpData = "";//No need body

        new AsyncTask<String, Integer, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... param) {
                String url = ADDRESS_SHARING_DOMAIN_URL + URL_SHARING + subUrl;
                LogUtil.d("Resp url =" + url);
                CwSharingNetWork cwSharingNW = new CwSharingNetWork();
                return cwSharingNW.makeHttpGet(url, param[0]);
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    try {
                        String hostid = result.getString("response");
                        mResponse[0] = hostid;
                        apiResultCallback.success(mResponse);

                    } catch (JSONException e) {
                        apiResultCallback.fail(failedlMsg + e.toString());
                    }
                } else {
                    apiResultCallback.fail(failedlMsg + " No response");
                }
            }
        }.execute(httpData);
    }

    public void ShareAddress(final String cwid, String CWIDinfo,String KeyInfo, final APIResultCallback apiResultCallback) {
        this.apiResultCallback = apiResultCallback;
        this.mResponse = new String[1];//loginInfo
        final String failedlMsg = "API [Response] failed:";
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"cwidinfo\":\"" + CWIDinfo + "\",");
        sb.append("\"keyInfo\":\"" + KeyInfo + "\"");
        sb.append("}");
        String httpData = sb.toString();
        LogUtil.d("httpData=" + httpData);

        new AsyncTask<String, Integer, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... param) {
                String url = ADDRESS_SHARING_DOMAIN_URL + URL_SHARING +"/"+cwid;
                LogUtil.d("ShareAddress url =" + url);
                CwSharingNetWork cwSharingNW = new CwSharingNetWork();
                return cwSharingNW.makeHttpPost(url, param[0]);
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    try {
                        String hostid = result.getString("RefNum");
                        mResponse[0] = hostid;
                        apiResultCallback.success(mResponse);

                    } catch (JSONException e) {
                        apiResultCallback.fail(failedlMsg + e.toString());
                    }
                } else {
                    apiResultCallback.fail(failedlMsg + " No response");
                }
            }
        }.execute(httpData);
    }
}
