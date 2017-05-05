package com.coolbitx.coolwallet.util;

import android.content.Context;
import android.os.AsyncTask;

import com.coolbitx.coolwallet.DataBase.DatabaseHelper;
import com.coolbitx.coolwallet.bean.CWAccountKeyInfo;
import com.coolbitx.coolwallet.bean.TrxBlks;
import com.coolbitx.coolwallet.bean.XchsSync;
import com.coolbitx.coolwallet.bean.dbAddress;
import com.coolbitx.coolwallet.callback.APIResultCallback;
import com.coolbitx.coolwallet.callback.XchsSyncCallback;
import com.coolbitx.coolwallet.general.BtcUrl;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.httpRequest.XchsNetWork;
import com.google.firebase.iid.FirebaseInstanceId;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.ByteUtil;
import com.snscity.egdwlib.utils.LogUtil;

import org.json.JSONArray;
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
    ArrayList<XchsSync> listXchsSync;
    XchsSync mXchsSync;

    public ExchangeAPI(Context context, CmdManager cmdManager) {
        this.cmdManager = cmdManager;
        this.mContext = context;
        this.CWID = new String(PublicPun.hexStringToByteArray(PublicPun.card.getCardId()));
//        this.orderID=orderID;
    }

    public void exchangeLogin(final APIResultCallback mAPIResultCallback) {
        this.mAPIResultCallback = mAPIResultCallback;
//        String mFirebaseToken = FirebaseInstanceId.getInstance().getToken();
        exchangeToken = FirebaseInstanceId.getInstance().getToken();
        LogUtil.d("FCM InstanceID token: " + exchangeToken);

        //1.Create Session:get [Challenge](session key) from Server
        LogUtil.d("getSrvInitSession CWID=" + CWID);
        getSrvInitSession(new APIResultCallback() {
            @Override
            public void success(String[] msg) {
                byte[] srvChlng = PublicPun.hexStringToByteArray(msg[0]);
                LogUtil.d("srvChlng=" + PublicPun.byte2HexString(srvChlng));
                //2.transfer [challenge] to the card and get [SRVRSP] [SECHLNG]
                cmdManager.XchsSessionInit(srvChlng, new CmdResultCallback() {
                    @Override
                    public void onSuccess(int status, byte[] outputData) {
                        if ((status + 65536) == 0x9000) {
                            LogUtil.d("getSrvInitSession:" + PublicPun.byte2HexString(outputData));
                            byte[] seResponse = new byte[16];
                            byte[] seChlng = new byte[16];

                            System.arraycopy(outputData, 0, seResponse, 0, 16);
                            System.arraycopy(outputData, 16, seChlng, 0, 16);

                            String seResp = PublicPun.byte2HexStringNoBlank(seResponse);
                            String seChallenge = PublicPun.byte2HexStringNoBlank(seChlng);

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
//                                                ArrayList<XchsSync> lisXchsSync =querySyncData();
                                                querySyncData(new XchsSyncCallback() {
                                                    @Override
                                                    public void onSuccess(ArrayList<XchsSync> listXchsSync) {
                                                        ArrayList<XchsSync> lisXchsSync = listXchsSync;
                                                        if (lisXchsSync.size() != 0) {
                                                            LogUtil.e("ready to getExchangeSync");
                                                            String SyncData = createSyncJson(lisXchsSync, exchangeToken);
                                                            getExchangeSync(CWID, SyncData, new APIResultCallback() {
                                                                @Override
                                                                public void success(String[] msg) {
                                                                    LogUtil.d("getExchangeSync ok " + msg[0]);
                                                                    mAPIResultCallback.success(msg);
                                                                }

                                                                @Override
                                                                public void fail(String msg) {
                                                                    LogUtil.d("getExchangeSync failed:" + msg);
                                                                    //exchangeSite Logout()
                                                                    mAPIResultCallback.fail(msg);
                                                                }
                                                            });
                                                        } else {
                                                            LogUtil.e("lisXchsSync no data found:");
                                                        }
                                                    }
                                                });

                                            }
                                        }
                                    });
                                }

                                @Override
                                public void fail(String msg) {
                                    LogUtil.d("getSessionEstablish failed:" + msg);
                                    mAPIResultCallback.fail(msg);
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
                mAPIResultCallback.fail("getExchangeSync failed:" + msg);
            }
        });
    }

    /**
     * 生成sync json
     */


    public void querySyncData(final XchsSyncCallback mXchsSyncCallback) {
        listXchsSync = new ArrayList<XchsSync>();

        ArrayList<CWAccountKeyInfo> cwList =
                DatabaseHelper.queryAccountKeyInfo(mContext, -1);

//        while(cwList.size() != ACCOUNT_CNT*2){
//
//        }

        for (CWAccountKeyInfo cw : cwList) {

            ArrayList<dbAddress> listAddress
                    = DatabaseHelper.queryAddress(mContext, cw.getAccId(), cw.getKcid());

            if (cw.getKcid() == 0) {
                mXchsSync = new XchsSync();
                mXchsSync.setAccID_ext(cw.getAccId());
                mXchsSync.setKeyPointer_ext(cw.getKcid());
                mXchsSync.setAccPub_ext(cw.getPublicKey());
                mXchsSync.setAccChain_ext(cw.getChainCode());
                mXchsSync.setAddNum_ext(listAddress.size());

            } else {
                mXchsSync.setAccID_int(cw.getAccId());
                mXchsSync.setKeyPointer_int(cw.getKcid());
                mXchsSync.setAccPub_int(cw.getPublicKey());
                mXchsSync.setAccChain_int(cw.getChainCode());
                mXchsSync.setAddNum_int(listAddress.size());

                listXchsSync.add(mXchsSync);
            }
        }
        mXchsSyncCallback.onSuccess(listXchsSync);

//        int i = 0;
//        while (i < ACCOUNT_CNT) {
//            final int accountId = i;
//            int j = 0;
//            while (j <= 1) {
//                final int kcid = j;
//
//                cmdManager.hdwQueryAccountKeyInfo(Constant.CwHdwAccountKeyInfoPubKeyAndChainCd,
//                        j,
//                        i,
//                        0,
//                        new CmdResultCallback() {
//                            @Override
//                            public void onSuccess(int status, byte[] outputData) {
//                                if ((status + 65536) == 0x9000) {
//                                    if (outputData != null) {
//
//                                        ArrayList<dbAddress> listAddress
//                                                = DatabaseHelper.queryAddress(mContext, accountId, kcid);
//
//                                        byte[] publicKeyBytes = new byte[64];
//                                        byte[] chainCodeBytes = new byte[32];
//                                        int length = outputData.length;
//                                        byte[] extendPub = new byte[33];
//                                        if (length >= 96) {
//
//                                            for (int i = 0; i < 64; i++) {
//                                                publicKeyBytes[i] = outputData[i];
//                                            }
//                                            for (int j = 64; j < 96; j++) {
//                                                chainCodeBytes[j - 64] = outputData[j];
//                                            }
//                                            //最後兩個字元一起
//
//                                            int mFirstKey = Integer.parseInt(PublicPun.byte2HexString(publicKeyBytes[63]), 16);
//
//                                            //format last charactors
//                                            if (mFirstKey % 2 == 0) {
//                                                extendPub[0] = 02;
//                                            } else {
//                                                extendPub[0] = 03;
//                                            }
//                                            for (int a = 0; a < 32; a++) {
//                                                extendPub[a + 1] = publicKeyBytes[a];
//                                            }
//
//                                            if (kcid == 0) {
//                                                mXchsSync = new XchsSync();
//                                                mXchsSync.setAccID_ext(accountId);
//                                                mXchsSync.setKeyPointer_ext(kcid);
//                                                mXchsSync.setAccPub_ext(LogUtil.byte2HexStringNoBlank(extendPub));
//                                                mXchsSync.setAccChain_ext(LogUtil.byte2HexStringNoBlank(chainCodeBytes));
//                                                mXchsSync.setAddNum_ext(listAddress.size());
//
//                                            } else {
//                                                mXchsSync.setAccID_int(accountId);
//                                                mXchsSync.setKeyPointer_int(kcid);
//                                                mXchsSync.setAccPub_int(LogUtil.byte2HexStringNoBlank(extendPub));
//                                                mXchsSync.setAccChain_int(LogUtil.byte2HexStringNoBlank(chainCodeBytes));
//                                                mXchsSync.setAddNum_int(listAddress.size());
//
//                                                listXchsSync.add(mXchsSync);
//                                            }
//
//                                            if (listXchsSync.size() == ACCOUNT_CNT) {
//                                                mXchsSyncCallback.onSuccess(listXchsSync);
//                                            }
//                                        }
//                                    }
//                                } else {
//                                }
//                            }
//                        });
//                j++;
//            }
//            i++;
//        }
    }

    private String createSyncJson(ArrayList<XchsSync> listXchsSync, String exchangeToken) {

        JSONStringer jsonStringer = new JSONStringer();

        try {
            jsonStringer.object();  //代表{
            jsonStringer.key("devType").value("android");
            jsonStringer.key("token").value(exchangeToken);
            jsonStringer.key("accounts");   //代表array key
            jsonStringer.array();    //代表[
            for (int x = 0; x < listXchsSync.size(); x++) {
                //Query accountKeyInfo's addr's Num & publicKey & chaincode
                jsonStringer.object();
                jsonStringer.key("id").value(listXchsSync.get(x).getAccID_ext());
                jsonStringer.key("extn");
                jsonStringer.object();
                jsonStringer.key("num").value(listXchsSync.get(x).getAddNum_ext());
                jsonStringer.key("pub").value(listXchsSync.get(x).getAccPub_ext());
                jsonStringer.key("chaincode").value(listXchsSync.get(x).getAccChain_ext());
                jsonStringer.endObject();
                jsonStringer.key("intn");
                jsonStringer.object();
                jsonStringer.key("num").value(listXchsSync.get(x).getAddNum_int());
                jsonStringer.key("pub").value(listXchsSync.get(x).getAccPub_int());
                jsonStringer.key("chaincode").value(listXchsSync.get(x).getAccChain_int());
                jsonStringer.endObject();
                jsonStringer.endObject();
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
                return new XchsNetWork().makeHttpRequestLogout(url);
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
                return new XchsNetWork().makeHttpRequestInit(url, param[0]);
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
        final String failedlMsg = "Failed to establish session.";
        //{ “response”:”xxxxxxxx”, “challenge”:”xxxxxxxxx”}
        String postData = "{\"response\":\"" + seResp + "\", \"challenge\":\"" + seChallenge + "\"}";

        new AsyncTask<String, Integer, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... param) {
                String url = "http://xsm.coolbitx.com:8080/api/res/cw/session/" + CWID;
                return new XchsNetWork().makeHttpRequestPost(url, param[0]);
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
        final String failedlMsg = "Exchange Sync failed.";
        String postData = syncStr;

        new AsyncTask<String, Integer, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... param) {
                String url = "http://xsm.coolbitx.com:8080/api/res/cw/" + CWID;
                return new XchsNetWork().makeHttpRequestPost(url, param[0]);
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    try {
                        String response = result.getString("response");
                        LogUtil.e("response=" + response);
                        if (response.equals("ok")) {
                            mResponse[0] = response;
                            apiResultCallback.success(mResponse);
                        } else {
                            apiResultCallback.fail(failedlMsg);
                        }
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
                return new XchsNetWork().makeHttpRequestGet(url, param[0]);
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

    public void getUnclarifyOrder(final APIResultCallback apiResultCallback) {
        this.apiResultCallback = apiResultCallback;
        this.mResponse = new String[1];//challenge
        final String failedlMsg = "getUnclarifyOrder fail.";
        String postData = "";

        new AsyncTask<String, Integer, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... param) {
                String url = "http://xsm.coolbitx.com:8080/api/res/cw/unclarify/" + CWID;
                return new XchsNetWork().makeHttpRequestGet(url, param[0]);
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
                return new XchsNetWork().makeHttpRequestGet(url, param[0]);
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
                LogUtil.d("doExWriteOKToken url=" + url);
                return new XchsNetWork().makeHttpRequestPost(url, param[0]);
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
                LogUtil.d("getExUnBlock url=" + url);
                return new XchsNetWork().makeHttpRequestGet(url, param[0]);
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
    public void cancelOrder(final String orderId, final APIResultCallback apiResultCallback) {
        this.apiResultCallback = apiResultCallback;
        this.mResponse = new String[1];//challenge
        final String failedlMsg = "cancelOrder fail.";
        String postData = "";
        LogUtil.d("cancelOrder in");
        new AsyncTask<String, Integer, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... param) {
                String url = "http://xsm.coolbitx.com:8080/api/res/cw/order/" + orderId;
                LogUtil.d("cancelOrder url=" + url);
                return new XchsNetWork().makeHttpDelete(url);
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    try {
                        LogUtil.d("cancelOrder =" + result);
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
                return new XchsNetWork().makeHttpRequestGet(url, param[0]);
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
    public void doExGetTrxPrepareBlocks(final String orderId, ArrayList<TrxBlks> mLisTrxBlks, final APIResultCallback apiResultCallback) {
        this.apiResultCallback = apiResultCallback;
        this.mResponse = new String[1];//challenge
        final String failedlMsg = "doExGetTrxPrepareBlocks fail.";
        String inputData = createXchsTrxJson(mLisTrxBlks);
        new AsyncTask<String, Integer, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... param) {
                String url = "http://xsm.coolbitx.com:8080/api/res/cw/trxblks/" + orderId;
//                XchsNetWork jParser = new XchsNetWork();
                return new XchsNetWork().makeHttpRequestPost(url, param[0]);
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    try {
                        LogUtil.d("doExGetTrxPrepareBlocks response=" + result);
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
        }.execute(inputData);
    }

    /**
     * Call BlockChain API to get the latest transaction ID.
     *
     * @param addr
     * @param apiResultCallback
     */

    public void getBlockChainRawAddress(final String addr, final APIResultCallback apiResultCallback) {

        final String failedlMsg = "getBlockChainRawAddress failed.";
        String postData = null;
        this.mResponse = new String[1];
        new AsyncTask<String, Integer, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... param) {
                String url = BtcUrl.URL_BLOCKCHAIN_RAW_ADDRESS + addr;
                return new XchsNetWork().doGetRawAddress(url);
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    try {
                        JSONArray jsonArrayTxs = result.getJSONArray("txs");
                        JSONObject jsonObjectTxs = jsonArrayTxs.getJSONObject(0);//latest
                        LogUtil.d("getBlockChainRawAddress response=" + jsonObjectTxs.toString());
                        String response = jsonObjectTxs.getString("hash");
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


    public void doTrxSubmit(final String orderId, final String trxId, final String trxReceipt, String uid, String nonce, final APIResultCallback apiResultCallback) {
        this.apiResultCallback = apiResultCallback;
        this.mResponse = new String[1];//challenge
        final String failedlMsg = "doTrxSubmit fail.";
        String postData = "{\"bcTrxId\":\"" + trxId + "\", \"trxReceipt\":\"" + trxReceipt +
                "\", \"uid\":\"" + uid + "\", \"nonce\":\"" + nonce + "\"}";

        new AsyncTask<String, Integer, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... param) {
                String url = "http://xsm.coolbitx.com:8080/api/res/cw/trx/" + orderId;
                XchsNetWork jParser = new XchsNetWork();
                return jParser.makeHttpRequestPost(url, param[0]);
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    try {
                        LogUtil.d("doTrxSubmit response=" + result);
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

    private String createXchsTrxJson(ArrayList<TrxBlks> mLisTrxBlks) {

        JSONStringer jsonStringer = new JSONStringer();

        try {
            jsonStringer.object();  //代表{
            jsonStringer.key("changeKid").value(mLisTrxBlks.get(0).getChangeKid());
            jsonStringer.key("blks");
            jsonStringer.array();    //代表[

            for (int i = 0; i < mLisTrxBlks.size(); i++) {
                jsonStringer.object();
                jsonStringer.key("idx").value(i + 1);//from 1 begin
                jsonStringer.key("blk").value(getBlkValue(mLisTrxBlks.get(i)));
                jsonStringer.endObject();
            }

            jsonStringer.endArray();
            jsonStringer.endObject();

            LogUtil.d("丟交易所 jsonString=" + jsonStringer.toString());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jsonStringer.toString();
    }

    private String getBlkValue(TrxBlks mTrxBlks) {
        String result = "";

//        byte[] trxHandle = mTrxBlks.getTrxHandle();
        byte[] accBytes = ByteUtil.intToByteLittle(mTrxBlks.getAccid(), 4);//4 bytes, little-endian
        byte kcidBytes = (byte) mTrxBlks.getKcid();
        byte[] kidBytes = ByteUtil.intToByteLittle(mTrxBlks.getKid(), 4);//4 bytes, little-endian
        byte[] out1Addr = mTrxBlks.getOut1Addr();
        byte[] out2Addr = mTrxBlks.getOut2Addr();
        byte[] sigMtrl = mTrxBlks.getSigmtrl();
        byte[] inputData;

//        [ACCID] [KCID] [KID]
//        [OUT1ADDR] [OUT2ADDR] [SIGMTRL]
//        byte[] inputData = new byte[4 + 4 + 1 + 4 + 25 + 25 + 32];
//        if (out2Addr.length == 0) {
//            inputData = new byte[4 + 1 + 4 + 25 + 32];
//        } else {
        inputData = new byte[4 + 1 + 4 + 25 + 25 + 32];
//        }

        LogUtil.e("input=" + inputData.length);

        //(來源陣列，起始索引值，目的陣列，起始索引值，複製長度)

        System.arraycopy(accBytes, 0, inputData, 0, accBytes.length);
        LogUtil.e("input add acc=" + PublicPun.byte2HexStringNoBlank(accBytes) + "\n" + PublicPun.byte2HexStringNoBlank(inputData));

        inputData[4] = kcidBytes;
        LogUtil.e("input add kcid=" + kcidBytes + "\n" + PublicPun.byte2HexStringNoBlank(inputData));

        System.arraycopy(kidBytes, 0, inputData, accBytes.length + 1, kidBytes.length);
        LogUtil.e("input add kid=" + PublicPun.byte2HexStringNoBlank(kidBytes) + "\n" + PublicPun.byte2HexStringNoBlank(inputData));

        System.arraycopy(out1Addr, 0, inputData, accBytes.length + 1 + kidBytes.length, out1Addr.length);
        LogUtil.e("input add out1Addr=" + PublicPun.byte2HexStringNoBlank(out1Addr) + "\n" + PublicPun.byte2HexStringNoBlank(inputData));

        if (out2Addr.length != 0) {
            LogUtil.e("有找零");
            System.arraycopy(out2Addr, 0, inputData, 4 + 1 + 4 + 25, out2Addr.length);
            LogUtil.e("input add out2Addr=" + PublicPun.byte2HexStringNoBlank(inputData));

            System.arraycopy(sigMtrl, 0, inputData, 4 + 1 + 4 + 25 + 25, 32);
            LogUtil.e("input add sigMtrl=" + PublicPun.byte2HexStringNoBlank(inputData));
        } else {
            LogUtil.e("沒有找零,OUT2給OUT1");
            System.arraycopy(out1Addr, 0, inputData, 4 + 1 + 4 + 25, out1Addr.length);
            LogUtil.e("input add out2Addr=" + PublicPun.byte2HexStringNoBlank(inputData));

            System.arraycopy(sigMtrl, 0, inputData, 4 + 1 + 4 + 25 + 25, 32);
            LogUtil.e("input add sigMtrl=" + PublicPun.byte2HexStringNoBlank(inputData));
        }


        result = PublicPun.byte2HexStringNoBlank(inputData);

        LogUtil.e("blkTrx inputData=" + result);

        return result;
    }
}
