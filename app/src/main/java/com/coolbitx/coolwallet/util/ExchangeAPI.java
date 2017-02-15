package com.coolbitx.coolwallet.util;

import android.content.Context;
import android.os.AsyncTask;

import com.coolbitx.coolwallet.DataBase.DatabaseHelper;
import com.coolbitx.coolwallet.callback.APIResultCallback;
import com.coolbitx.coolwallet.entity.Constant;
import com.coolbitx.coolwallet.entity.XchsSync;
import com.coolbitx.coolwallet.entity.dbAddress;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.httpRequest.XchsNetWork;
import com.coolbitx.coolwallet.ui.Fragment.FragMainActivity;
import com.google.firebase.iid.FirebaseInstanceId;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;

/**
 * Created by ShihYi on 2016/6/7.
 */
public class ExchangeAPI {

    APIResultCallback apiResultCallback;
    String[] mResponse;
    String exchangeToken;
    CmdManager cmdManager;
    boolean initResult;
    Context mContext;
    String CWID;
    APIResultCallback mAPIResultCallback;

    public ExchangeAPI(Context context, CmdManager cmdManager) {
        this.cmdManager = cmdManager;
        this.mContext = context;
        this.CWID = new String(PublicPun.hexStringToByteArray(PublicPun.card.getCardId()));
//        this.orderID=orderID;
    }

    public boolean exchangeLogin(APIResultCallback mAPIResultCallback) {
        initResult = false;
        this.mAPIResultCallback = mAPIResultCallback;

        exchangeToken = FirebaseInstanceId.getInstance().getToken();
        LogUtil.d("FCM InstanceID token: " + exchangeToken);

        //1.Create Session:get [Challenge] from Server
        LogUtil.d("getSrvInitSession CWID=" + CWID);
        getSrvInitSession(new APIResultCallback() {
            @Override
            public void success(String[] msg) {
                byte[] srvChlng = PublicPun.hexStringToByteArray(msg[0]);
                LogUtil.d("srvChlng=" + PublicPun.byte2HexString(srvChlng));
                //2.transfer [challenge] to the card and get [SERESP] [SECHLNG]
                cmdManager.XchsSessionInit(srvChlng, new CmdResultCallback() {
                    @Override
                    public void onSuccess(int status, byte[] outputData) {
                        if ((status + 65536) == 0x9000) {
                            LogUtil.d("getSrvInitSession:" + PublicPun.byte2HexString(outputData));
                            byte[] seResponse = new byte[16];
                            byte[] seChlng = new byte[16];

                            System.arraycopy(outputData, 0, seResponse, 0, 16);
                            System.arraycopy(outputData, 16, seChlng, 0, 16);

                            String seResp = LogUtil.byte2HexStringNoBlank(seResponse);
                            String seChallenge = LogUtil.byte2HexStringNoBlank(seChlng);

                            LogUtil.d("getSrvInitSession-seResponse:" + seResp);
                            LogUtil.d("getSrvInitSession-seChlng:" + seChallenge);

                            getSessionEstablish(CWID, seResp, seChallenge, new APIResultCallback() {
                                @Override
                                public void success(String[] msg) {
                                    LogUtil.d("getSessionEstablish=" + msg[0]);

                                    // 卡片se_xchs_session_estb
                                    byte[] svrResp = PublicPun.hexStringToByteArray(msg[0]);
                                    cmdManager.XchsSessionEstablish(svrResp, new CmdResultCallback() {
                                        @Override
                                        public void onSuccess(int status, byte[] outputData) {
                                            //collect api sync data
                                            if ((status + 65536) == 0x9000) {
                                                ArrayList<XchsSync> lisXchsSync = querySyncData();
                                                if (lisXchsSync.size() != 0) {
                                                    getExchangeSync(CWID, createSyncJson(lisXchsSync, exchangeToken), new APIResultCallback() {
                                                        @Override
                                                        public void success(String[] msg) {
                                                            LogUtil.d("getExchangeSync ok " + msg[0]);
                                                            initResult = true;
                                                        }

                                                        @Override
                                                        public void fail(String msg) {
                                                            LogUtil.d("getExchangeSync failed:" + msg);
                                                            //exchangeSite Logout()
                                                        }
                                                    });
                                                } else {
                                                    LogUtil.e("lisXchsSync no data found:");
                                                }
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void fail(String msg) {
                                    LogUtil.d("getSessionEstablish failed:" + msg);
                                    //exchangeSite Logout()
                                }
                            });
                        } else {
                            cmdManager.getError(new CmdResultCallback() {
                                @Override
                                public void onSuccess(int status, byte[] outputData) {
                                    LogUtil.e("getError :" + PublicPun.byte2HexString(outputData));
                                }
                            });
                        }
                    }
                });
            }

            @Override
            public void fail(String msg) {
                LogUtil.d("getSrvInitSession=" + msg);
            }
        });

        return initResult;
    }


    /**
     * 生成json
     */
    boolean[] isNotAccountReady;
    boolean[] isNotKeyReady;
    ArrayList<dbAddress> listAddress;
    final byte[] cwHdwAccountInfo =
            new byte[]{Constant.CwAddressKeyChainExternal, Constant.CwAddressKeyChainInternal};


    public ArrayList<XchsSync> querySyncData() {
        // TODO Auto-generated method stub
        //final int accountId, final int kid, final byte kcId
//        querySyncData
        isNotAccountReady = new boolean[FragMainActivity.ACCOUNT_CNT];
        isNotKeyReady = new boolean[2];

        final XchsSync mXchsSync = new XchsSync();
        final ArrayList<XchsSync> listXchsSync = new ArrayList<XchsSync>();

        int i = 0;
        while (i < FragMainActivity.ACCOUNT_CNT && !isNotAccountReady[i]) {
            LogUtil.d("XCHS acc=" + String.valueOf(i));
            final int accountId = i;
            isNotAccountReady[i] = true;
            listAddress = new ArrayList<dbAddress>();
            int j = 0;
            while (j <= 1 && !isNotKeyReady[j]) {// && !isNotKeyReady[j]
                final int kcid = j;
                isNotKeyReady[j] = true;
                listAddress = DatabaseHelper.queryAddress(mContext, i, j);
//                LogUtil.d("XCHS acc=" + String.valueOf(i) + ";kcid=" + kcid);//+"
                LogUtil.d("XCHS kcid=" + kcid);//+"



                cmdManager.hdwQueryAccountKeyInfo(Constant.CwHdwAccountKeyInfoPubKeyAndChainCd,
                        j,
                        i,
                        0,
                        new CmdResultCallback() {
                            @Override
                            public void onSuccess(int status, byte[] outputData) {
                                if ((status + 65536) == 0x9000) {
                                    if (outputData != null) {
                                        byte[] publicKeyBytes = new byte[64];
                                        byte[] chainCodeBytes = new byte[32];
                                        int length = outputData.length;
                                        byte[] extendPub = new byte[33];
                                        if (length >= 96) {

                                            for (int i = 0; i < 64; i++) {
                                                publicKeyBytes[i] = outputData[i];
                                            }
                                            for (int j = 64; j < 96; j++) {
                                                chainCodeBytes[j - 64] = outputData[j];
                                            }
                                            //最後兩個字元一起


                                            int mFirstKey = Integer.parseInt(PublicPun.byte2HexString(publicKeyBytes[63]), 16);

                                            //format last charactors
                                            if (mFirstKey % 2 == 0) {
                                                extendPub[0] = 02;
                                            } else {
                                                extendPub[0] = 03;
                                            }
                                            for (int a = 0; a < 32; a++) {
                                                extendPub[a + 1] = publicKeyBytes[a];
                                            }

                                            mXchsSync.setAccID(accountId);
                                            mXchsSync.setKeyPointer(kcid);
                                            mXchsSync.setAccPub(LogUtil.byte2HexStringNoBlank(extendPub));
                                            mXchsSync.setAccChain(LogUtil.byte2HexStringNoBlank(chainCodeBytes));
                                            mXchsSync.setAddNum(listAddress.size());

                                            LogUtil.d("XCHS account=" + accountId + " ;kcid=" + kcid + " ;num=" + listAddress.size()
                                                    + " ;建地址的public key=" + LogUtil.byte2HexString(publicKeyBytes)
                                                    + " ;建地址的chainCodeBytes=" + LogUtil.byte2HexString(chainCodeBytes));
                                            listXchsSync.add(mXchsSync);

                                            isNotAccountReady[accountId] = false;
                                            isNotKeyReady[kcid] = false;
                                        }
                                    }
                                } else {
                                }
                            }
                        });

            }

        }
        LogUtil.d("交易所Sync 數量 =" + listXchsSync.size());
        return listXchsSync;
    }

    public String createSyncJson(ArrayList<XchsSync> listXchsSync, String exchangeToken) {

        JSONStringer jsonStringer = new JSONStringer();

        try {
            jsonStringer.object();  //代表{
            jsonStringer.key("devType").value("android");
            jsonStringer.key("token").value(exchangeToken);
            jsonStringer.key("accounts");   //代表array key
            jsonStringer.array();    //代表[

            for (int x = 0; x < FragMainActivity.ACCOUNT_CNT; x++) {
                //Query accountKeyInfo's addr's Num & publicKey & chaincode

                if (listXchsSync.get(x).getKeyPointer() == 0) {
                    jsonStringer.object();
                    jsonStringer.key("id").value(x);
                    jsonStringer.key("extn");
                    jsonStringer.object();
                    jsonStringer.key("num").value(listXchsSync.get(x).getAddNum());
//                jsonStringer.key("pub").value("035f21fb5476e6a3f3cb176eb44a9af3edabf0deca50032e82fed02fa556d1337b");
                    jsonStringer.key("pub").value(listXchsSync.get(x).getAccPub());
//                jsonStringer.key("chaincode").value("54dc901c4001ae7cf41a8b451e1f5db911df60c5353047d26c4a4c4325cee859");
                    jsonStringer.key("chaincode").value(listXchsSync.get(x).getAccChain());

                } else {
                    jsonStringer.endObject();
                    jsonStringer.key("intn");
                    jsonStringer.object();
                    jsonStringer.key("num").value(listXchsSync.get(x).getAddNum());
//                    jsonStringer.key("pub").value("035f21fb5476e6a3f3cb176eb44a9af3edabf0deca50032e82fed02fa556d1337b");
                    jsonStringer.key("pub").value(listXchsSync.get(x).getAccPub());
//                    jsonStringer.key("chaincode").value("54dc901c4001ae7cf41a8b451e1f5db911df60c5353047d26c4a4c4325cee859");
                    jsonStringer.key("chaincode").value(listXchsSync.get(x).getAccChain());
                    jsonStringer.endObject();
                    jsonStringer.endObject();
                }

            }
            jsonStringer.endArray();
            jsonStringer.endObject();
            LogUtil.d("交易所 jsonString=" + jsonStringer.toString());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jsonStringer.toString();
    }

    public void exchangeLogOut(final APIResultCallback apiResultCallback) {
        this.apiResultCallback = apiResultCallback;
        this.mResponse = new String[1];//challenge
        final String failedlMsg = "Card init session fail.";
        String httpData = null;
        new AsyncTask<String, Integer, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... param) {
                String url = "http://xsm.coolbitx.com:8080/api/res/cw/session/logout/";
                LogUtil.d("exchangeLogOut url=" + url);
                XchsNetWork mXchsNetWork = new XchsNetWork();
                return mXchsNetWork.makeHttpRequestLogout(url);
            }

            @Override
            protected void onPostExecute(JSONObject result) {

                if (result != null) {
                    try {
                        String rsp = result.getString("response");
                        mResponse[0] = rsp;
                        apiResultCallback.success(mResponse);

                    } catch (JSONException e) {
                        apiResultCallback.fail(failedlMsg + ":" + e.toString());
                    }
                } else {
                    apiResultCallback.fail(failedlMsg);
                }
            }
        }.execute();
    }

    public void getSrvInitSession(final APIResultCallback apiResultCallback) {
        this.apiResultCallback = apiResultCallback;
        this.mResponse = new String[1];//challenge
        final String failedlMsg = "Card init session fail.";
        String httpData = null;
        new AsyncTask<String, Integer, JSONObject>() {


            @Override
            protected JSONObject doInBackground(String... param) {
                String url = "http://xsm.coolbitx.com:8080/api/res/cw/session/" + CWID;
                LogUtil.d("getSrvInitSession url=" + url);
                XchsNetWork jParser = new XchsNetWork();
                return jParser.makeHttpRequestInit(url, param[0]);
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    try {
                        String challenge = result.getString("challenge");
                        mResponse[0] = challenge;
                        apiResultCallback.success(mResponse);

                    } catch (JSONException e) {
                        apiResultCallback.fail(failedlMsg + ":" + e.toString());
                    }
                } else {
                    apiResultCallback.fail(failedlMsg);
                }
            }
        }.execute(httpData);
    }


    public void getSessionEstablish(final String CWID, String seResp, String seChallenge, final APIResultCallback apiResultCallback) {
        this.apiResultCallback = apiResultCallback;
        this.mResponse = new String[1];//challenge
        final String failedlMsg = "Session Established fail.";
        //{ “response”:”xxxxxxxx”, “challenge”:”xxxxxxxxx”}
        String postData = "{\"response\":\"" + seResp + "\", \"challenge\":\"" + seChallenge + "\"}";

        new AsyncTask<String, Integer, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... param) {
                String url = "http://xsm.coolbitx.com:8080/api/res/cw/session/" + CWID;
                XchsNetWork jParser = new XchsNetWork();
                return jParser.makeHttpRequestPost(url, param[0]);
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    try {
                        String response = result.getString("response");
                        mResponse[0] = response;
                        apiResultCallback.success(mResponse);

                    } catch (JSONException e) {
                        apiResultCallback.fail(failedlMsg + ":" + e.toString());
                    }
                } else {
                    apiResultCallback.fail(failedlMsg);
                }
            }
        }.execute(postData);
    }

    public void getExchangeSync(final String CWID, String syncStr, final APIResultCallback apiResultCallback) {
        this.apiResultCallback = apiResultCallback;
        this.mResponse = new String[1];//challenge
        final String failedlMsg = "ExchangeSync fail.";
        String postData = syncStr;

        new AsyncTask<String, Integer, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... param) {
                String url = "http://xsm.coolbitx.com:8080/api/res/cw/" + CWID;
                XchsNetWork jParser = new XchsNetWork();
                return jParser.makeHttpRequestPost(url, param[0]);
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    try {
                        String response = result.getString("response");
                        mResponse[0] = response;
                        apiResultCallback.success(mResponse);

                    } catch (JSONException e) {
                        apiResultCallback.fail(failedlMsg + ":" + e.toString());
                    }
                } else {
                    apiResultCallback.fail(failedlMsg);
                }
            }
        }.execute(postData);
    }

    public void getPendingOrder(final APIResultCallback apiResultCallback) {
        this.apiResultCallback = apiResultCallback;
        this.mResponse = new String[1];//challenge
        final String failedlMsg = "getPendingOrder fail.";
        String postData = "";

        new AsyncTask<String, Integer, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... param) {
                String url = "http://xsm.coolbitx.com:8080/api/res/cw/pending/" + CWID;
                XchsNetWork jParser = new XchsNetWork();
                return jParser.makeHttpRequestGet(url, param[0]);
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    try {
                        LogUtil.d("getPendingOrder=" + result);
                        String response = result.toString();
                        mResponse[0] = response;
                        apiResultCallback.success(mResponse);


                    } catch (Exception e) {
                        apiResultCallback.fail(failedlMsg + ":" + e.toString());
                    }
                } else {
                    apiResultCallback.fail(failedlMsg);
                }
            }
        }.execute(postData);
    }

    public void getUnclarifyOrder(final String CWID, final APIResultCallback apiResultCallback) {
        this.apiResultCallback = apiResultCallback;
        this.mResponse = new String[1];//challenge
        final String failedlMsg = "getUnclarifyOrder fail.";
        String postData = "";

        new AsyncTask<String, Integer, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... param) {
                String url = "http://xsm.coolbitx.com:8080/api/res/cw/unclarify/" + CWID;
                XchsNetWork jParser = new XchsNetWork();
                return jParser.makeHttpRequestGet(url, param[0]);
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    try {
                        LogUtil.d("getUnclarifyOrder=" + result);
                        String response = result.toString();
                        mResponse[0] = response;
                        apiResultCallback.success(mResponse);

                    } catch (Exception e) {
                        apiResultCallback.fail(failedlMsg + ":" + e.toString());
                    }
                } else {
                    apiResultCallback.fail(failedlMsg);
                }
            }
        }.execute(postData);
    }

    //ExRequestOrderBlock
    public void getExRequestOrderBlock(final String hexOrder, final String blockOtp, final APIResultCallback apiResultCallback) {
        this.apiResultCallback = apiResultCallback;
        this.mResponse = new String[1];//challenge
        final String failedlMsg = "getExRequestOrderBlock fail.";
        String postData = "";

        new AsyncTask<String, Integer, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... param) {
                String url = "http://xsm.coolbitx.com:8080/api/res/cw/order/" + hexOrder + "/" + blockOtp;
                XchsNetWork jParser = new XchsNetWork();
                return jParser.makeHttpRequestGet(url, param[0]);
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    try {
                        LogUtil.d("getExRequestOrderBlock=" + result);
                        String response = result.getString("block_btc");
                        mResponse[0] = response;
                        apiResultCallback.success(mResponse);

                    } catch (Exception e) {
                        apiResultCallback.fail(failedlMsg + ":" + e.toString());
                    }
                } else {
                    apiResultCallback.fail(failedlMsg);
                }
            }
        }.execute(postData);
    }

    //ExWriteOKToken  //(orderId, okToken,unblockToken
    public void doExWriteOKToken(final String hexOrder, final String okToken, final String unblockToken, final APIResultCallback apiResultCallback) {
        this.apiResultCallback = apiResultCallback;
        this.mResponse = new String[1];//challenge
        final String failedlMsg = "getExRequestOrderBlock fail.";
        //(With: {"okToken": "a0a1a2a3","unblockToken": "b0b1b2b3"})
        String postData = "{\"okToken\":\"" + okToken + "\", \"unblockToken\":\"" + unblockToken + "\"}";

        new AsyncTask<String, Integer, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... param) {
                String url = "http://xsm.coolbitx.com:8080/api/res/cw/oktoken/" + hexOrder;
                XchsNetWork jParser = new XchsNetWork();
                return jParser.makeHttpRequestPost(url, param[0]);
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    try {
                        LogUtil.d("getExRequestOrderBlock=" + result);
                        String response = result.getString("response");
                        mResponse[0] = response;
                        apiResultCallback.success(mResponse);

                    } catch (Exception e) {
                        apiResultCallback.fail(failedlMsg + ":" + e.toString());
                    }
                } else {
                    apiResultCallback.fail(failedlMsg);
                }
            }
        }.execute(postData);
    }

    //ExRequestOrderBlock
    public void getExUnBlock(final String orderId, final APIResultCallback apiResultCallback) {
        this.apiResultCallback = apiResultCallback;
        this.mResponse = new String[5];//challenge
        final String failedlMsg = "getExUnBlock fail.";
        String postData = "";

        new AsyncTask<String, Integer, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... param) {
                String url = "http://xsm.coolbitx.com:8080/api/res/cw/unblock/" + orderId;
                XchsNetWork jParser = new XchsNetWork();
                return jParser.makeHttpRequestGet(url, param[0]);
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    try {
                        LogUtil.d("getExUnBlock =" + result);
//                        {"orderId":"15847930","okToken":"abc123","unblockTkn":"abc123",
////                                "mac":"dbe57d18f1c176606f40361a11c755ed655804a319d7b7120cdb1e729786d5dd"}
                        mResponse[0] = result.getString("orderId");
                        mResponse[1] = result.getString("okToken");
                        mResponse[2] = result.getString("unblockTkn");
                        mResponse[3] = result.getString("mac");
                        mResponse[4] = result.getString("nonce");


                        apiResultCallback.success(mResponse);

                    } catch (Exception e) {
                        apiResultCallback.fail(failedlMsg + ":" + e.toString());
                    }
                } else {
                    apiResultCallback.fail(failedlMsg);
                }
            }
        }.execute(postData);
    }


    //deleteBlockOrder
    public void delExBlock(final String orderId, final APIResultCallback apiResultCallback) {
        this.apiResultCallback = apiResultCallback;
        this.mResponse = new String[1];//challenge
        final String failedlMsg = "delExBlock fail.";
        String postData = "";
        LogUtil.d("delExBlock in");
        new AsyncTask<String, Integer, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... param) {
                String url = "http://xsm.coolbitx.com:8080/api/res/cw/order/" + orderId;
                XchsNetWork jParser = new XchsNetWork();
                return jParser.makeHttpDelete(url);
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    try {
                        LogUtil.d("delExBlock =" + result);
                        mResponse[0] = result.getString("response");
                        apiResultCallback.success(mResponse);
                    } catch (Exception e) {
                        apiResultCallback.fail(failedlMsg + ":" + e.toString());
                    }
                } else {
                    apiResultCallback.fail(failedlMsg);
                }
            }
        }.execute(postData);
    }


    //ExGetTrxPrepareBlocks
    public void doExGetTrxInfo(final String orderId, final APIResultCallback apiResultCallback) {
        this.apiResultCallback = apiResultCallback;
        this.mResponse = new String[1];//challenge
        final String failedlMsg = "doExGetTrxInfo fail.";
        String postData = "";

        new AsyncTask<String, Integer, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... param) {
                LogUtil.d("doExGetTrxInfo order ID=" + orderId);
                String url = "http://xsm.coolbitx.com:8080/api/res/cw/trxinfo/" + orderId;
                XchsNetWork jParser = new XchsNetWork();
                return jParser.makeHttpRequestGet(url, param[0]);
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    try {
                        LogUtil.d("doExGetTrxInfo=" + result);
                        String response = result.getString("loginblk");
                        mResponse[0] = response;
                        apiResultCallback.success(mResponse);

                    } catch (Exception e) {
                        apiResultCallback.fail(failedlMsg + " EXCEPTION:" + e.toString());
                    }
                } else {
                    apiResultCallback.fail(failedlMsg);
                }
            }
        }.execute(postData);
    }

    //ExGetTrxPrepareBlocks
    public void doExGetTrxPrepareBlocks(String inputData, final String orderID, final APIResultCallback apiResultCallback) {
        this.apiResultCallback = apiResultCallback;
        this.mResponse = new String[1];//challenge
        final String failedlMsg = "doExGetTrxPrepareBlocks fail.";
        String postData = "";

        new AsyncTask<String, Integer, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... param) {
                String url = "http://xsm.coolbitx.com:8080/api/res/cw/trxblks/" + orderID;
                XchsNetWork jParser = new XchsNetWork();
                return jParser.makeHttpRequestPost(url, param[0]);
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    try {
                        LogUtil.d("doExGetTrxPrepareBlocks=" + result);
                        String response = result.getString("block_btc");
                        mResponse[0] = response;
                        apiResultCallback.success(mResponse);

                    } catch (Exception e) {
                        apiResultCallback.fail(failedlMsg + ":" + e.toString());
                    }
                } else {
                    apiResultCallback.fail(failedlMsg);
                }
            }
        }.execute(postData);
    }

}
